package org.samo_lego.healthcare.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.samo_lego.healthcare.healthbar.HealthbarStyle;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixinCast_Preferences implements HealthbarPreferences {

    private Enum<HealthbarStyle> healthbarStyle = HealthbarStyle.PERCENTAGE;
    private boolean enabled = false;

    @Override
    public Enum<HealthbarStyle> getHealthbarStyle() {
        return this.healthbarStyle;
    }

    @Override
    public void setHealthbarStyle(Enum<HealthbarStyle> healthbarStyle) {
        this.healthbarStyle = healthbarStyle;
    }

    @Override
    public MutableText getHealth(float health, float maxHealth) {

        String first, second;
        if(HealthbarStyle.HEARTS.equals(this.healthbarStyle)) {
            // We ceil the number to not show 0 hearts if entity has like 0.2f health
            int heartCount = maxHealth < 10 ? (int) maxHealth : 10;
            int fullHearts = (int) Math.ceil(health * heartCount / maxHealth);

            first = new String(new char[fullHearts]).replace('\0', (char) 9829); // ♥
            second = new String(new char[heartCount - fullHearts]).replace('\0', (char) 9825); // ♡
        } else if(HealthbarStyle.PERCENTAGE.equals(this.healthbarStyle)) {
            first = String.valueOf(Math.round(health * 100.0F / maxHealth)).concat("%");
            second = "";
        } else {
            // Number
            // * 100 / 100 for rounding
            first = String.valueOf((float) Math.round(health * 100.0F) / 100.0F);
            second = "/" + maxHealth;
        }

        return new LiteralText(first).formatted(Formatting.RED).append(new LiteralText(second).formatted(Formatting.GRAY));
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        CompoundTag healthbar = new CompoundTag();
        healthbar.putString("Style", this.healthbarStyle.toString());
        healthbar.putBoolean("Enabled", this.enabled);
        tag.put("Healthbar", healthbar);
    }

    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if(tag.contains("Healthbar")) {
            CompoundTag healthbar = tag.getCompound("Healthbar");
            this.healthbarStyle = HealthbarStyle.valueOf(healthbar.getString("Style"));
            this.enabled = healthbar.getBoolean("Enabled");
        }
    }
}
