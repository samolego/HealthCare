package org.samo_lego.healthcare.healthbar;

import net.minecraft.network.chat.MutableComponent;

public interface HealthbarPreferences {
    PlayerHealthbar healthcare_healthcarePrefs();

    MutableComponent healthcare_createHealthbarText(float health, float maxHealth);
}
