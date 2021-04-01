package org.samo_lego.healthcare.mixin;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
            if(entity instanceof LivingEntity && ((HealthbarPreferences) this.player).isEnabled()) {
                // Removing current custom name
                trackedValues.removeIf(value -> value.getData().getId() == 2);

                DataTracker.Entry<Optional<Text>> healthTag = new DataTracker.Entry<>(EntityAccessor.getCUSTOM_NAME(), Optional.of(new LiteralText("")));
                DataTracker.Entry<Boolean> visibleTag = new DataTracker.Entry<>(EntityAccessor.getNAME_VISIBLE(), true);

                LivingEntity living = (LivingEntity) entity;
                float health = living.getHealth();
                float maxHealth = living.getMaxHealth();

                MutableText name = living.hasCustomName() ? (MutableText) living.getCustomName() : new TranslatableText(living.getType().getTranslationKey());

                String healthbar = ((HealthbarPreferences) this.player).getHealth(health, maxHealth);
                healthTag.set(Optional.of(name.append(" ").append(new LiteralText(healthbar))));

                Collections.addAll(trackedValues,visibleTag, healthTag);
                ((EntityTrackerUpdateS2CPacketAccessor) packet).setTrackedValues(trackedValues);
            }
        }
    }
}
