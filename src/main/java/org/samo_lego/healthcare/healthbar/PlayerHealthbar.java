package org.samo_lego.healthcare.healthbar;

import net.minecraft.nbt.CompoundTag;

import static org.samo_lego.healthcare.HealthCare.config;

public class PlayerHealthbar {
    public HealthbarStyle healthbarStyle;
    public boolean enabled;
    public boolean showType;
    public boolean alwaysVisible;

    public char customFullChar;
    public char customEmptyChar;
    public int customLength;

    public PlayerHealthbar() {
        this.healthbarStyle = config.defaultStyle;
        this.enabled = config.enabledByDefault;
        this.showType = config.showType;
        this.alwaysVisible = config.alwaysVisibleDefault;
        this.customFullChar = '■';
        this.customEmptyChar = '□';
        this.customLength = 10;
    }

    public void toTag(CompoundTag tag) {
        tag.putString("Style", this.healthbarStyle.name());
        tag.putBoolean("Enabled", this.enabled);
        tag.putBoolean("ShowType", this.showType);
        tag.putBoolean("AlwaysVisible", this.alwaysVisible);

        if (this.healthbarStyle == HealthbarStyle.CUSTOM) {
            tag.putInt("FullChar", this.customFullChar);
            tag.putInt("EmptyChar", this.customEmptyChar);
            tag.putInt("Length", this.customLength);
        }
    }

    public void fromTag(CompoundTag tag) {
        this.healthbarStyle = HealthbarStyle.valueOf(tag.getString("Style").orElse(this.healthbarStyle.name()));
        this.enabled = tag.getBoolean("Enabled").orElse(this.enabled);
        this.showType = tag.getBoolean("ShowType").orElse(this.showType);
        this.alwaysVisible = tag.getBoolean("AlwaysVisible").orElse(this.alwaysVisible);

        if (this.healthbarStyle == HealthbarStyle.CUSTOM) {
            this.customFullChar = tag.getInt("FullChar").map(integer -> (char) integer.intValue()).orElse(this.customFullChar);
            this.customEmptyChar = tag.getInt("EmptyChar").map(integer -> (char) integer.intValue()).orElse(this.customEmptyChar);
            this.customLength = tag.getInt("Length").orElse(this.customLength);
        }
    }
}
