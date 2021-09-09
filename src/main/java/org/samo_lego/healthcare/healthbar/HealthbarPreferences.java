package org.samo_lego.healthcare.healthbar;

import net.minecraft.text.MutableText;

public interface HealthbarPreferences {
    /**
     * Gets style of the healthbar.
     * @return healthbar style
     */
    Enum<HealthbarStyle> getHealthbarStyle();
    void setHealthbarStyle(Enum<HealthbarStyle> healthbarStyle);

    /**
     * Gets the health text from current health and max health depending on HealthbarStyle.
     * @param health current health
     * @param maxHealth max health
     * @return formatted mutable text with health info
     */
    MutableText getHealthbarText(float health, float maxHealth);

    void setEnabled(boolean enabled);
    boolean isEnabled();

    /**
     * Sets whether to show entity type next to hearts as well.
     * @param showEntityType whether to include entity type in name or not.
     */
    void setShowEntityType(boolean showEntityType);
    boolean showEntityType();

    void setAlwaysVisible(boolean alwaysVisible);
    boolean isAlwaysVisible();

    void setCustomEmptyChar(int customEmptyChar);
    int getCustomEmptyChar();

    void setCustomFullChar(int customFullChar);
    int getCustomFullChar();

    void setCustomLength(int length);
    int getCustomLength();

    enum HealthbarStyle {
        PERCENTAGE,
        HEARTS,
        NUMBER,
        LINES,
        SKYBLOCK, // see https://github.com/samolego/HealthCare/issues/2
        CUSTOM
    }
}
