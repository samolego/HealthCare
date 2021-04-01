package org.samo_lego.healthcare.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import org.samo_lego.healthcare.healthbar.HealthbarStyles;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixinCast_Preferences implements HealthbarPreferences {

    private Enum<HealthbarStyles> healthbarStyle = HealthbarStyles.PERCENTAGE;
    private boolean enabled;

    @Override
    public Enum<HealthbarStyles> getHealthbarStyle() {
        return this.healthbarStyle;
    }

    @Override
    public void setHealthbarStyle(Enum<HealthbarStyles> healthbarStyle) {
        this.healthbarStyle = healthbarStyle;
    }

    @Override
    public String getHealth(float health, float maxHealth) {

        String formattedHealth;
        if(HealthbarStyles.HEARTS.equals(this.healthbarStyle)) {
            int fullHearts = (int) (health * 10 / maxHealth);
            formattedHealth = new String(new char[fullHearts]).replace('\0', (char) 2764).concat(new String(new char[10 - fullHearts]).replace('\0', (char) 2661));
        } else if(HealthbarStyles.PERCENTAGE.equals(this.healthbarStyle)) {
            formattedHealth = String.valueOf(Math.round(health * 100.0F / maxHealth)).concat("%");
        } else {
            // Number
            formattedHealth = String.valueOf(health).concat("/").concat(String.valueOf(maxHealth));
        }

        return formattedHealth;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {

    }
}
