package org.samo_lego.healthcare.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventHandler implements ServerPlayerEvents.CopyFrom {

    /**
     * Called when player data is copied to a new player.
     *
     * @param oldPlayer the old player
     * @param newPlayer the new player
     * @param alive     whether the old player is still alive
     */
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {

    }
}
