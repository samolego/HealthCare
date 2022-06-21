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
        this.healthbarStyle = HealthbarStyle.valueOf(tag.getString("Style"));
        this.enabled = tag.getBoolean("Enabled");
        this.showType = tag.getBoolean("ShowType");
        this.alwaysVisible = tag.getBoolean("AlwaysVisible");

        if (this.healthbarStyle == HealthbarStyle.CUSTOM) {
            this.customFullChar = (char) tag.getInt("FullChar");
            this.customEmptyChar = (char) tag.getInt("EmptyChar");
            this.customLength = tag.getInt("Length");
        }
    }
}
