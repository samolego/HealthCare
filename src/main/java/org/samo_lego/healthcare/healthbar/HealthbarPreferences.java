package org.samo_lego.healthcare.healthbar;

import net.minecraft.network.chat.MutableComponent;

public interface HealthbarPreferences {
    PlayerHealthbar healthcarePrefs();

    MutableComponent createHealthbarText(float health, float maxHealth);
}
