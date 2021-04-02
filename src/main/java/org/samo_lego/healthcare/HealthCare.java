package org.samo_lego.healthcare;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.samo_lego.healthcare.command.HealthbarCommand;
import org.samo_lego.healthcare.event.EventHandler;

public class HealthCare implements ModInitializer {
	public static final String MODID = "healthcare";
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(HealthbarCommand::register);
		EventHandler eventHandler = new EventHandler();
		ServerPlayerEvents.COPY_FROM.register(eventHandler);
	}
}
