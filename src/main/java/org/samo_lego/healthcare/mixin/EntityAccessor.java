package org.samo_lego.healthcare.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("NAME_VISIBLE")
    static TrackedData<Boolean> getNAME_VISIBLE() {
        throw new AssertionError();
    }
    @Accessor("CUSTOM_NAME")
    static TrackedData<Optional<Text>> getCUSTOM_NAME() {
        throw new AssertionError();
    }
}
