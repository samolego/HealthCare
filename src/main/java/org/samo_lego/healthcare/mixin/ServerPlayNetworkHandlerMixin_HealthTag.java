package org.samo_lego.healthcare.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
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
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.spongepowered.asm.mixin.Final;
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
public class ServerPlayNetworkHandlerMixin_HealthTag {
    @Shadow
    public ServerPlayer player;

    @Shadow
    @Final
    public Connection connection;

    /**
     * Dummy handler used for creating modified
     * packets with health.
     * This is used instead of real entity datatracker since
     * we don't want its values to get marked
     * as non-dirty upon sending the fake packet.
     */
    @Unique
    private final SynchedEntityData healthcare$dummyTracker = new SynchedEntityData(null);

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
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"
            ),
            cancellable = true
    )
    private void onPacketSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof ClientboundSetEntityDataPacket) {
            int id = ((EntityTrackerUpdateS2CPacketAccessor) packet).getId();
            Entity entity = this.player.getLevel().getEntity(id);

            if (
                    entity instanceof LivingEntity living &&
                            ((HealthbarPreferences) this.player).isEnabled() &&
                            !(entity instanceof Player) &&
                            !config.blacklistedEntities.contains(Registry.ENTITY_TYPE.getKey(entity.getType()).toString()) &&
                            !entity.isInvisibleTo(player)
            ) {
                List<SynchedEntityData.DataItem<?>> trackedValues = new ArrayList<>(((EntityTrackerUpdateS2CPacketAccessor) packet).getPackedItems());

                // Removing current custom name
                trackedValues.removeIf(value -> value.getAccessor().getId() == 2);

                // Ensure name is visible only if mob is not too far away
                boolean visible = (entity.distanceTo(player) < config.activationRange || entity.isCustomNameVisible()) && ((HealthbarPreferences) player).isAlwaysVisible();
                SynchedEntityData.DataItem<Boolean> visibleTag = new SynchedEntityData.DataItem<>(EntityAccessor.getNAME_VISIBLE(), visible);

                float health = living.getHealth();
                float maxHealth = living.getMaxHealth();

                // @SpaceClouds42 saved me here, `.copy()` after getting custom name is essential!
                MutableComponent name;
                if (entity.hasCustomName())
                    name = entity.getCustomName().copy().append(" ");
                else if (((HealthbarPreferences) player).showEntityType())
                    name = Component.translatable(entity.getType().getDescriptionId()).append(" ");
                else
                    name = Component.literal("");

                MutableComponent healthbar = ((HealthbarPreferences) this.player).getHealthbarText(health, maxHealth);
                SynchedEntityData.DataItem<Optional<Component>> healthTag = new SynchedEntityData.DataItem<>(EntityAccessor.getCUSTOM_NAME(), Optional.of(name.append(healthbar)));

                Collections.addAll(trackedValues, visibleTag, healthTag);

                // Create a new packet in order to not mess with other network handlers
                // since same packet object is sent to every player
                ClientboundSetEntityDataPacket trackerUpdatePacket = new ClientboundSetEntityDataPacket(id, this.healthcare$dummyTracker, false);
                EntityTrackerUpdateS2CPacketAccessor accessor = (EntityTrackerUpdateS2CPacketAccessor) trackerUpdatePacket;

                accessor.setId(id);
                accessor.setPackedItems(trackedValues);

                this.connection.send(trackerUpdatePacket, listener);
                ci.cancel(); // cancel the original packet going out
            }
        }
    }
}
