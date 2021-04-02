package org.samo_lego.healthcare.mixin;

import com.google.common.collect.Lists;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin_HealthTag {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"
            )
    )
    private void onPacketSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if(packet instanceof EntityTrackerUpdateS2CPacket) {
            List<DataTracker.Entry<?>> trackedValues = ((EntityTrackerUpdateS2CPacketAccessor) packet).getTrackedValues();

            Entity entity = this.player.getServerWorld().getEntityById(((EntityTrackerUpdateS2CPacketAccessor) packet).getId());
            if(entity instanceof LivingEntity && ((HealthbarPreferences) this.player).isEnabled() && !(entity instanceof ServerPlayerEntity)) {
                // Removing current custom name
                trackedValues.removeIf(value -> value.getData().getId() == 2);

                // Ensure name is visible only if mob is not too far away
                boolean visible = entity.distanceTo(player) < 8.0F || entity.isCustomNameVisible();
                DataTracker.Entry<Boolean> visibleTag = new DataTracker.Entry<>(EntityAccessor.getNAME_VISIBLE(), visible);

                LivingEntity living = (LivingEntity) entity;
                float health = living.getHealth();
                float maxHealth = living.getMaxHealth();

                // @SpaceClouds42 saved me here, `.copy()` after getting custom name is essential!
                MutableText name = entity.hasCustomName() ? entity.getCustomName().copy() : new TranslatableText(entity.getType().getTranslationKey());

                Text healthbar = ((HealthbarPreferences) this.player).getHealth(health, maxHealth);
                DataTracker.Entry<Optional<Text>> healthTag = new DataTracker.Entry<>(EntityAccessor.getCUSTOM_NAME(), Optional.of(name.append(" ").append(healthbar)));

                Collections.addAll(trackedValues, visibleTag, healthTag);
                ((EntityTrackerUpdateS2CPacketAccessor) packet).setTrackedValues(trackedValues);
            }
        }
    }
}
