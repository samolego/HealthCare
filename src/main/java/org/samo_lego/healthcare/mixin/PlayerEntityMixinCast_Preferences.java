package org.samo_lego.healthcare.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.samo_lego.healthcare.healthbar.HealthbarStyle;
import org.samo_lego.healthcare.healthbar.PlayerHealthbar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixinCast_Preferences implements HealthbarPreferences {

    @Unique
    private final PlayerHealthbar hc_healthbar = new PlayerHealthbar();


    @Override
    public MutableComponent healthcare_createHealthbarText(float health, float maxHealth) {
        if (health < 0.0F) {
            health = 0.0F;
        }
        if (maxHealth <= 0.0F) {
            maxHealth = 1.0F;
        }
        if (health > maxHealth) {
            maxHealth = health;
        }

        final var hb = this.hc_healthbar;
        if (hb.healthbarStyle == HealthbarStyle.CUSTOM) {
            return HealthbarStyle.getCustomHealthbarText(hb.customEmptyChar, hb.customFullChar, hb.customLength, health, maxHealth);
        }

        return hb.healthbarStyle.getText(health, maxHealth);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        CompoundTag healthbar = new CompoundTag();
        this.hc_healthbar.toTag(healthbar);
        tag.put("Healthbar", healthbar);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Healthbar")) {
            CompoundTag healthbar = tag.getCompound("Healthbar");
            this.hc_healthbar.fromTag(healthbar);
        }
    }

    @Override
    public PlayerHealthbar healthcare_healthcarePrefs() {
        return this.hc_healthbar;
    }
}
