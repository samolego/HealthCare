package org.samo_lego.healthcare.compatibility;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.entity.Entity;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymerCompat {
    public static boolean isPolymerEntity(Entity entity, PacketContext context) {
        return entity instanceof PolymerEntity poly && poly.getPolymerEntityType(context) != entity.getType();
    }
}
