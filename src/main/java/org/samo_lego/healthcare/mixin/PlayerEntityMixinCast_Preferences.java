package org.samo_lego.healthcare.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixinCast_Preferences implements HealthbarPreferences {

    private Enum<HealthbarStyle> healthbarStyle = HealthbarStyle.PERCENTAGE;
    private boolean enabled = true;
    private boolean alwaysVisible = false;

    private int customFullChar = 9632; // '■'
    private int customEmptyChar = 9633; // '□'
    private int customLength = 10;

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
        if(this.healthbarStyle.equals(HealthbarStyle.NUMBER)) {
            // Number
            // * 100 / 100 for rounding
            first = String.valueOf((float) Math.round(health * 100.0F) / 100.0F);
            second = "/" + maxHealth;
        } else if(HealthbarStyle.PERCENTAGE.equals(this.healthbarStyle)) {
            first = String.valueOf(Math.round(health * 100.0F / maxHealth)).concat("%");
            second = "";
        } else {
            int heartCount, fullHearts;
            char full, empty;
            if(this.healthbarStyle.equals(HealthbarStyle.LINES)) {
                heartCount = maxHealth < 20 ? (int) maxHealth : 20;
                fullHearts = (int) Math.ceil(health * heartCount / maxHealth);

                empty = '|';
                full = '|';
            } else { // Hearts
                // We ceil the number to not show 0 hearts if entity has like 0.2f health
                int length = this.healthbarStyle == HealthbarStyle.CUSTOM ? customLength : 10;
                heartCount = maxHealth < length ? (int) maxHealth : length;
                fullHearts = (int) Math.ceil(health * heartCount / maxHealth);

                full = (char) (this.healthbarStyle.equals(HealthbarStyle.HEARTS) ? 9829 : this.customFullChar); // ♥ or custom
                empty = (char) (this.healthbarStyle.equals(HealthbarStyle.HEARTS) ? 9825 : this.customEmptyChar); // ♡ or custom
            }

            first = new String(new char[fullHearts]).replace('\0', full);
            second = new String(new char[heartCount - fullHearts]).replace('\0', empty);
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

    @Override
    public void setAlwaysVisible(boolean alwaysVisible) {
        this.alwaysVisible = alwaysVisible;
    }

    @Override
    public boolean isAlwaysVisible() {
        return this.alwaysVisible;
    }

    @Override
    public void setCustomEmptyChar(int customEmptyChar) {
        this.customEmptyChar = customEmptyChar;
    }

    @Override
    public int getCustomEmptyChar() {
        return this.customEmptyChar;
    }

    @Override
    public void setCustomFullChar(int customFullChar) {
        this.customFullChar = customFullChar;
    }

    @Override
    public int getCustomFullChar() {
        return this.customFullChar;
    }

    @Override
    public void setCustomLength(int length) {
        this.customLength = length;
    }

    @Override
    public int getCustomLength() {
        return this.customLength;
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        CompoundTag healthbar = new CompoundTag();
        healthbar.putString("Style", this.healthbarStyle.toString());
        healthbar.putBoolean("Enabled", this.enabled);
        if(this.healthbarStyle.equals(HealthbarStyle.CUSTOM)) {
            healthbar.putInt("CustomFullChar", this.customFullChar);
            healthbar.putInt("CustomEmptyChar", this.customEmptyChar);
            healthbar.putInt("Length", this.customLength);
        }
        tag.put("Healthbar", healthbar);
    }

    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if(tag.contains("Healthbar")) {
            CompoundTag healthbar = tag.getCompound("Healthbar");
            this.healthbarStyle = HealthbarStyle.valueOf(healthbar.getString("Style"));
            this.enabled = healthbar.getBoolean("Enabled");

            if(this.healthbarStyle.equals(HealthbarStyle.CUSTOM)) {
                this.customFullChar = healthbar.getInt("CustomFullChar");
                this.customEmptyChar = healthbar.getInt("CustomEmptyChar");
                this.customLength = healthbar.getInt("Length");
            }
        }
    }
}
