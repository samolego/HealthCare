package org.samo_lego.healthcare.compatibility;

import net.fabricmc.fabric.mixin.object.builder.DefaultAttributeRegistryAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MobLevel {
    /**
     * Calculates the level of a mob.
     * <a href="https://github.com/Globox1997/Nameplate/blob/9aa8367fc4041639e8b42635be4e2e09cb4415f5/src/main/java/net/nameplate/util/NameplateTracker.java#L25C1-L37C6">Formula from Nameplate</a>
     * @param mob
     * @return
     */
    public static int getLevel(LivingEntity mob) {
        int multiplier = 10;

        return (int) (multiplier * mob.getAttributeBaseValue(Attributes.MAX_HEALTH)
                / Math.abs(DefaultAttributeRegistryAccessor.getRegistry().get(mob.getType()).getBaseValue(Attributes.MAX_HEALTH))) - multiplier + 1;
    }
}
