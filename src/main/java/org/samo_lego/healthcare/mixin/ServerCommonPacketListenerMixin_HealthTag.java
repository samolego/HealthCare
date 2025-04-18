package org.samo_lego.healthcare.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.healthcare.HealthCare;
import org.samo_lego.healthcare.compatibility.MobLevel;
import org.samo_lego.healthcare.compatibility.PolymerCompat;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.samo_lego.healthcare.HealthCare.config;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerMixin_HealthTag {

    @Shadow
    public abstract void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener);

    @Unique
    private boolean hc_skipCheck;

    /**
     * Gets the current packet being sent and modifies
     * it accordingly to player's preferences if it is
     * an {@link ClientboundSetEntityDataPacket}.
     *
     * @param sendPacket packet being sent
     */
    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V"
            ),
            cancellable = true
    )
    private void onPacketSend(Packet<?> sendPacket, PacketSendListener listener, CallbackInfo ci) {
        Packet<?> mutatedPacket = null;

        if (hc_skipCheck || !(this instanceof AServerGamePacketListenerImpl gamePacketListener)) {
            return;
        }


        var player = gamePacketListener.getPlayer();

        if (sendPacket instanceof ClientboundSetEntityDataPacket dataPacket) {
            mutatedPacket = tryMutatePacket(dataPacket, player);
        } else if (sendPacket instanceof ClientboundBundlePacket bundledPackets) {
            var packets = new ArrayList<Packet<? super ClientGamePacketListener>>();
            boolean doMutate = false;

            for (var p : bundledPackets.subPackets()) {
                if (p instanceof ClientboundSetEntityDataPacket dataPacket && (dataPacket = tryMutatePacket(dataPacket, player)) != null) {
                    doMutate = true;
                    packets.add(dataPacket);
                } else {
                    packets.add(p);
                }
            }

            if (doMutate) {
                mutatedPacket = new ClientboundBundlePacket(packets);
            }
        }

        if (mutatedPacket != null) {
            this.hc_skipCheck = true;
            this.send(mutatedPacket, listener);
            this.hc_skipCheck = false;
            ci.cancel(); // cancel the original packet going out
        }
    }

    /**
     * @return A new packet, or null if the original needed not be mutated.
     */
    @Unique
    private @Nullable ClientboundSetEntityDataPacket tryMutatePacket(ClientboundSetEntityDataPacket packet, ServerPlayer player) {
        int id = packet.id();
        Entity entity = player.level().getEntity(id);
        final var hb = ((HealthbarPreferences) player).healthcare_healthcarePrefs();

        if (!hb.enabled
                || !(entity instanceof LivingEntity living)
                || entity instanceof Player
                // If entity is a custom polymer entity, we disable the packet tweaker,
                // since entity might be an e.g. block display entity, which doesn't support custom name field in packet.
                || HealthCare.POLYMER_LOADED && PolymerCompat.isPolymerEntity(entity, PacketContext.get())
                || config.isEntityBlacklisted(entity)
                || entity.isInvisibleTo(player)
        ) {
            return null;
        }

        var trackedValues = new ArrayList<>(packet.packedItems());

        // Removing current custom name
        var customName = trackedValues.stream().filter(value -> value.id() == 2).findFirst();

        // Ensure name is visible only if mob is not too far away
        boolean visible = (entity.distanceTo(player) < config.activationRange || entity.isCustomNameVisible()) && hb.alwaysVisible;
        var visibleTag = SynchedEntityData.DataValue.create(AEntity.getNAME_VISIBLE(), visible);

        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();

        MutableComponent name = Component.literal("");
        
        // Add mob level stats
        if (config.mobLevels.showMobLevel) {
            var mobLevel = MobLevel.getLevel(living);
            name.append(Component.literal(String.format(config.mobLevels.mobLevelText, mobLevel)).withStyle(ChatFormatting.YELLOW)).append(" ");
        }
        
        if (customName.isPresent() && ((Optional<Component>) customName.get().value()).isPresent()) {
            name.append(((Optional<Component>) customName.get().value()).get().copy().append(" "));
        } else if (entity.hasCustomName()) {
            // @SpaceClouds42 saved me here, `.copy()` after getting custom name is essential!
            name.append(entity.getCustomName().copy().append(" "));
        } else if (hb.showType) {
            name.append(Component.translatable(entity.getType().getDescriptionId()).append(" "));
        }

        var healthbar = ((HealthbarPreferences) player).healthcare_createHealthbarText(health, maxHealth);
        var healthTag = SynchedEntityData.DataValue.create(AEntity.getCUSTOM_NAME(), Optional.of(name.append(healthbar)));

        Collections.addAll(trackedValues, visibleTag, healthTag);

        // Create a new packet in order to not mess with other network handlers
        // since same packet object is sent to every player
        return new ClientboundSetEntityDataPacket(id, trackedValues);
    }
}
