package org.samo_lego.healthcare.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.samo_lego.healthcare.HealthCare.config;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin_HealthTag {
    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener);

    @Unique
    private boolean hc_skipCheck;

    /**
     * Dummy handler used for creating modified
     * packets with health.
     * This is used instead of real entity datatracker since
     * we don't want its values to get marked
     * as non-dirty upon sending the fake packet.
     */
    @Unique
    private final SynchedEntityData hc_dummyTracker = new SynchedEntityData(null);

    /**
     * Gets the current packet being sent and modifies
     * it accordingly to player's preferences if it is
     * an {@link ClientboundSetEntityDataPacket}.
     *
     * @param packet   packet being sent
     * @param listener
     * @param ci
     */
    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"
            ),
            cancellable = true
    )
    private void onPacketSend(Packet<?> sendPacket, PacketSendListener listener, CallbackInfo ci) {
        Packet<?> mutatedPacket = null;

        if (hc_skipCheck)
            return;

        if (sendPacket instanceof ClientboundSetEntityDataPacket dataPacket)
            mutatedPacket = TryMutatePacket(dataPacket);
        else if (sendPacket instanceof ClientboundBundlePacket bundledPackets){
            List<Packet<ClientGamePacketListener>> packets = new ArrayList<>();
            boolean doMutate = false;

            for (var p : bundledPackets.subPackets()){
                if (p instanceof ClientboundSetEntityDataPacket dataPacket && (p=TryMutatePacket(dataPacket)) != null)
                    doMutate = true;
                packets.add(p);
            }

            if (doMutate)
                mutatedPacket = new ClientboundBundlePacket(packets);
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
    private @Nullable ClientboundSetEntityDataPacket TryMutatePacket(ClientboundSetEntityDataPacket packet) {
        int id = packet.id();
        Entity entity = this.player.getLevel().getEntity(id);
        final var hb = ((HealthbarPreferences) this.player).healthcarePrefs();

        if (!hb.enabled
        || !(entity instanceof LivingEntity living)
        || entity instanceof Player
        || config.blacklistedEntities.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString())
        || entity.isInvisibleTo(player)
        ) {
            return null;
        }

        var trackedValues = new ArrayList<>(packet.packedItems());

        // Removing current custom name
        var customName = trackedValues.stream().filter(value -> value.id() == 2).findFirst();

        // Ensure name is visible only if mob is not too far away
        boolean visible = (entity.distanceTo(player) < config.activationRange || entity.isCustomNameVisible()) && hb.alwaysVisible;
        var visibleTag = SynchedEntityData.DataValue.create(EntityAccessor.getNAME_VISIBLE(), visible);

        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();

        MutableComponent name = Component.empty();
        if (customName.isPresent() && ((Optional<Component>) customName.get().value()).isPresent()) {
            name = ((Optional<Component>) customName.get().value()).get().copy().append(" ");
        } else if (entity.hasCustomName()) {
            // @SpaceClouds42 saved me here, `.copy()` after getting custom name is essential!
            name = entity.getCustomName().copy().append(" ");
        } else if (hb.showType) {
            name = Component.translatable(entity.getType().getDescriptionId()).append(" ");
        }

        var healthbar = ((HealthbarPreferences) this.player).createHealthbarText(health, maxHealth);
        var healthTag = SynchedEntityData.DataValue.create(EntityAccessor.getCUSTOM_NAME(), Optional.of(name.append(healthbar)));

        Collections.addAll(trackedValues, visibleTag, healthTag);

        // Create a new packet in order to not mess with other network handlers
        // since same packet object is sent to every player
        return new ClientboundSetEntityDataPacket(id, trackedValues);
    }
}
