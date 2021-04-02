package org.samo_lego.healthcare.healthbar;

import net.minecraft.text.MutableText;

public interface HealthbarPreferences {
    Enum<HealthbarStyle> getHealthbarStyle();
    void setHealthbarStyle(Enum<HealthbarStyle> healthbarStyle);

    MutableText getHealth(float health, float maxHealth);

    void setEnabled(boolean enabled);
    boolean isEnabled();
}
