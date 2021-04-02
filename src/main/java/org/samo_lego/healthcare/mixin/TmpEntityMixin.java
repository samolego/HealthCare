package org.samo_lego.healthcare.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class TmpEntityMixin {
    @Inject(method = "setCustomName(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onName(Text name, CallbackInfo ci) {
        System.out.println(name.getString() + " is a new name for " + ((Entity) (Object) this).getCustomName());
    }
}
