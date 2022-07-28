package org.samo_lego.healthcare.mixin;

import net.minecraft.core.Registry;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
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
    private void onPacketSend(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundSetEntityDataPacket && !this.hc_skipCheck) {
            int id = ((EntityTrackerUpdateS2CPacketAccessor) packet).getId();
            Entity entity = this.player.getLevel().getEntity(id);
            final var hb = ((HealthbarPreferences) this.player).healthcarePrefs();

            if (entity instanceof LivingEntity living &&
                    hb.enabled &&
                    !(entity instanceof Player) &&
                    !config.blacklistedEntities.contains(Registry.ENTITY_TYPE.getKey(entity.getType()).toString()) &&
                    !entity.isInvisibleTo(player)) {

                var trackedValues = new ArrayList<>(((EntityTrackerUpdateS2CPacketAccessor) packet).getPackedItems());

                // Removing current custom name
                var customName = trackedValues.stream().filter(value -> value.getAccessor().getId() == 2).findFirst();

                // Ensure name is visible only if mob is not too far away
                boolean visible = (entity.distanceTo(player) < config.activationRange || entity.isCustomNameVisible()) && hb.alwaysVisible;
                var visibleTag = new SynchedEntityData.DataItem<>(EntityAccessor.getNAME_VISIBLE(), visible);

                float health = living.getHealth();
                float maxHealth = living.getMaxHealth();

                MutableComponent name = Component.empty();
                if (customName.isPresent() && ((Optional<Component>) customName.get().getValue()).isPresent()) {
                    name = ((Optional<Component>) customName.get().getValue()).get().copy().append(" ");
                } else if (entity.hasCustomName()) {
                    // @SpaceClouds42 saved me here, `.copy()` after getting custom name is essential!
                    name = entity.getCustomName().copy().append(" ");
                } else if (hb.showType) {
                    name = Component.translatable(entity.getType().getDescriptionId()).append(" ");
                }

                var healthbar = ((HealthbarPreferences) this.player).createHealthbarText(health, maxHealth);
                var healthTag = new SynchedEntityData.DataItem<>(EntityAccessor.getCUSTOM_NAME(), Optional.of(name.append(healthbar)));

                Collections.addAll(trackedValues, visibleTag, healthTag);

                // Create a new packet in order to not mess with other network handlers
                // since same packet object is sent to every player
                var trackerUpdatePacket = new ClientboundSetEntityDataPacket(id, this.hc_dummyTracker, false);
                var accessor = (EntityTrackerUpdateS2CPacketAccessor) trackerUpdatePacket;

                accessor.setId(id);
                accessor.setPackedItems(trackedValues);

                this.hc_skipCheck = true;
                this.send(trackerUpdatePacket, listener);
                this.hc_skipCheck = false;
                ci.cancel(); // cancel the original packet going out
            }
        }
    }
}
