package org.samo_lego.healthcare.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.healthcare.HealthCare.config;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixinCast_Preferences implements HealthbarPreferences {

    private HealthbarStyle healthbarStyle = config.defaultStyle;
    private boolean enabled = config.enabledByDefault;
    private boolean showType = config.showType;
    private boolean alwaysVisible = config.alwaysVisibleDefault;

    private int customFullChar = 9632; // '■'
    private int customEmptyChar = 9633; // '□'
    private int customLength = 10;

    @Override
    public HealthbarStyle getHealthbarStyle() {
        return this.healthbarStyle;
    }

    @Override
    public void setHealthbarStyle(HealthbarStyle healthbarStyle) {
        this.healthbarStyle = healthbarStyle;
    }

    @Override
    public MutableText getHealthbarText(float health, float maxHealth) {
        if(health < 0.0F) {
            health = 0.0F;
        }
        if(maxHealth <= 0.0F) {
            maxHealth = 1.0F;
        }
        if(health > maxHealth) {
            maxHealth = health;
        }

        String first, second;
        switch(this.healthbarStyle) {
            case SKYBLOCK -> {
                // String.format("%.2f", health) for rounding
                first = String.format("%.2f", health);
                second = String.valueOf((int) Math.ceil(maxHealth));

                // We return it here because of custom formatting
                return new LiteralText(first)
                        .formatted(health > maxHealth / 2 ? Formatting.GREEN : Formatting.YELLOW)
                        .append(new LiteralText("/")
                                .formatted(Formatting.WHITE))
                        .append(new LiteralText(second)
                                .formatted(Formatting.GREEN))
                        .append(new LiteralText(String.valueOf((char) 10084)) // ❤
                                .formatted(Formatting.RED));
            }
            case NUMBER -> {
                // Number
                // String.format("%.2f", health) for rounding
                first = String.format("%.2f", health);
                second = "/" + maxHealth;
            }
            case PERCENTAGE -> {
                first = String.format("%.2f", health * 100.0F / maxHealth).concat("%");
                second = "";
            }
            default -> {
                int heartCount, fullHearts;
                char full, empty;
                if(this.healthbarStyle.equals(HealthbarStyle.LINES)) {
                    heartCount = maxHealth < 20 ? (int) Math.ceil(maxHealth) : 20;

                    empty = '|';
                    full = '|';
                } else { // Hearts
                    // We ceil the number to not show 0 hearts if entity has like 0.2f health
                    int length = this.healthbarStyle == HealthbarStyle.CUSTOM ? customLength : 10;
                    heartCount = maxHealth < length ? (int) Math.ceil(maxHealth) : length;

                    full = (char) (this.healthbarStyle.equals(HealthbarStyle.HEARTS) ? 9829 : this.customFullChar); // ♥ or custom
                    empty = (char) (this.healthbarStyle.equals(HealthbarStyle.HEARTS) ? 9825 : this.customEmptyChar); // ♡ or custom
                }

                // Hearts that should be colored red
                fullHearts = (int) Math.ceil(health * heartCount / maxHealth);

                first = new String(new char[fullHearts]).replace('\0', full);
                second = new String(new char[heartCount - fullHearts]).replace('\0', empty);
            }
        }
        return new LiteralText(first)
                .formatted(Formatting.RED) /*health > maxHealth / 3 ? (health > maxHealth * 1.5F ? Formatting.YELLOW : Formatting.GOLD) : */
                .append(new LiteralText(second).formatted(Formatting.GRAY));
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
    public void setShowEntityType(boolean showEntityType) {
       this.showType = showEntityType;
    }

    @Override
    public boolean showEntityType() {
        return this.showType;
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
        if(length >= 0) // Don't allow negative length
            this.customLength = length;
    }

    @Override
    public int getCustomLength() {
        return this.customLength;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
        NbtCompound healthbar = new NbtCompound();
        healthbar.putString("Style", this.healthbarStyle.toString());
        healthbar.putBoolean("Enabled", this.enabled);
        healthbar.putBoolean("ShowType", this.showType);
        healthbar.putBoolean("AlwaysVisible", this.alwaysVisible);
        if(this.healthbarStyle.equals(HealthbarStyle.CUSTOM)) {
            healthbar.putInt("CustomFullChar", this.customFullChar);
            healthbar.putInt("CustomEmptyChar", this.customEmptyChar);
            healthbar.putInt("Length", this.customLength);
        }
        tag.put("Healthbar", healthbar);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
        if(tag.contains("Healthbar")) {
            NbtCompound healthbar = tag.getCompound("Healthbar");
            this.healthbarStyle = HealthbarStyle.valueOf(healthbar.getString("Style"));
            this.enabled = healthbar.getBoolean("Enabled");
            this.alwaysVisible = healthbar.getBoolean("AlwaysVisible");
            this.showType = healthbar.getBoolean("ShowType");

            if(this.healthbarStyle.equals(HealthbarStyle.CUSTOM)) {
                this.customFullChar = healthbar.getInt("CustomFullChar");
                this.customEmptyChar = healthbar.getInt("CustomEmptyChar");
                this.customLength = healthbar.getInt("Length");
            }
        }
    }
}
