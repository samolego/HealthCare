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
    MutableText getHealth(float health, float maxHealth);

    void setEnabled(boolean enabled);
    boolean isEnabled();

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
        CUSTOM
    }
}
