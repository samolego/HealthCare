package org.samo_lego.healthcare.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;

public class PlayerDataEvent implements ServerPlayerEvents.CopyFrom {

    /**
     * Called when player data is copied to a new player.
     *
     * @param oldPlayer the old player
     * @param newPlayer the new player
     * @param alive     whether the old player is still alive
     */
    @Override
    public void copyFromPlayer(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        final var healthbar = ((HealthbarPreferences) oldPlayer).healthcarePrefs();

        CompoundTag tag = new CompoundTag();
        healthbar.toTag(tag);

        ((HealthbarPreferences) newPlayer).healthcarePrefs().fromTag(tag);
    }
}
