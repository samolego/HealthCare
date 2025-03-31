package org.samo_lego.healthcare.compatibility;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import xyz.nucleoid.packettweaker.PacketContext;

public class Polymer {
    public static boolean isPolymerPlayerEntity(Entity entity, PacketContext context) {
        return entity instanceof PolymerEntity poly && poly.getPolymerEntityType(context) == EntityType.PLAYER;
        
    }
}
