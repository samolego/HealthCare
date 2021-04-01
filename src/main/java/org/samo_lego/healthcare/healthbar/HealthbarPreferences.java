package org.samo_lego.healthcare.healthbar;

public interface HealthbarPreferences {
    Enum<HealthbarStyles> getHealthbarStyle();
    void setHealthbarStyle(Enum<HealthbarStyles> healthbarStyle);

    String getHealth(float health, float maxHealth);

    void setEnabled(boolean enabled);
    boolean isEnabled();
}
