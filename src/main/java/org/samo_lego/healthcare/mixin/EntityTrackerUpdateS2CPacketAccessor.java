package org.samo_lego.healthcare.mixin;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientboundSetEntityDataPacket.class)
public interface EntityTrackerUpdateS2CPacketAccessor {

    @Accessor("packedItems")
    List<SynchedEntityData.DataItem<?>> getPackedItems();

    @Mutable
    @Accessor("packedItems")
    void setPackedItems(List<SynchedEntityData.DataItem<?>> packedItems);

    @Accessor("id")
    int getId();

    @Mutable
    @Accessor("id")
    void setId(int id);
}
