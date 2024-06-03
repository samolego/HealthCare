package org.samo_lego.healthcare.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(Entity.class)
public interface AEntity {
    @Accessor("DATA_CUSTOM_NAME_VISIBLE")
    static EntityDataAccessor<Boolean> getNAME_VISIBLE() {
        throw new AssertionError();
    }

    @Accessor("DATA_CUSTOM_NAME")
    static EntityDataAccessor<Optional<Component>> getCUSTOM_NAME() {
        throw new AssertionError();
    }
}
