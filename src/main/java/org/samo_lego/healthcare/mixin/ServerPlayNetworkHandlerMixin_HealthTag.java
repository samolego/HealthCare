package org.samo_lego.healthcare.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;
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

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_HealthTag {
    @Shadow public ServerPlayerEntity player;

    @Shadow @Final public ClientConnection connection;

    /**
     * Dummy handler used for creating modified
     * packets with health.
     * This is used instead of real entity datatracker since
     * we don't want its values to get marked
     * as non-dirty upon sending the fake packet.
     */
    @Unique
    private final DataTracker healthcare$dummyTracker = new DataTracker(null);

    /**
     * Gets the current packet being sent and modifies
     * it accordingly to player's preferences in the packet is
     * an {@link EntityTrackerUpdateS2CPacket}.
     *
     * @param packet packet being sent
     * @param listener
     * @param ci
     */
    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"
            ),
            cancellable = true
    )
    private void onPacketSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if(packet instanceof EntityTrackerUpdateS2CPacket) {
            int id = ((EntityTrackerUpdateS2CPacketAccessor) packet).getId();
            Entity entity = this.player.getServerWorld().getEntityById(id);

            if(
                    entity instanceof LivingEntity living &&
                    ((HealthbarPreferences) this.player).isEnabled() &&
                    !(entity instanceof PlayerEntity) &&
                    !config.blacklistedEntities.contains(Registry.ENTITY_TYPE.getId(entity.getType()).toString())
            ) {
                List<DataTracker.Entry<?>> trackedValues = new ArrayList<>(((EntityTrackerUpdateS2CPacketAccessor) packet).getTrackedValues());

                // Removing current custom name
                trackedValues.removeIf(value -> value.getData().getId() == 2);

                // Ensure name is visible only if mob is not too far away
                boolean visible = (entity.distanceTo(player) < config.activationRange || entity.isCustomNameVisible()) && ((HealthbarPreferences) player).isAlwaysVisible();
                DataTracker.Entry<Boolean> visibleTag = new DataTracker.Entry<>(EntityAccessor.getNAME_VISIBLE(), visible);

                float health = living.getHealth();
                float maxHealth = living.getMaxHealth();

                // @SpaceClouds42 saved me here, `.copy()` after getting custom name is essential!
                MutableText name = entity.hasCustomName() ? entity.getCustomName().copy() : new TranslatableText(entity.getType().getTranslationKey());

                Text healthbar = ((HealthbarPreferences) this.player).getHealthbarText(health, maxHealth);
                DataTracker.Entry<Optional<Text>> healthTag = new DataTracker.Entry<>(EntityAccessor.getCUSTOM_NAME(), Optional.of(name.append(" ").append(healthbar)));

                Collections.addAll(trackedValues, visibleTag, healthTag);

                // Create a new packet in order to not mess with other network handlers
                // since same packet object is sent to every player
                EntityTrackerUpdateS2CPacket trackerUpdatePacket = new EntityTrackerUpdateS2CPacket(id, this.healthcare$dummyTracker, false);
                EntityTrackerUpdateS2CPacketAccessor accessor = (EntityTrackerUpdateS2CPacketAccessor) trackerUpdatePacket;

                accessor.setId(id);
                accessor.setTrackedValues(trackedValues);

                this.connection.send(trackerUpdatePacket, listener);
                ci.cancel(); // cancel the original packet going out
            }
        }
    }
}
