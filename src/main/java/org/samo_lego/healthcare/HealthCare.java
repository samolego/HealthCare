package org.samo_lego.healthcare;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.samo_lego.healthcare.command.HealthbarCommand;

public class HealthCare implements ModInitializer {
	public static final String MODID = "healthcare";
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(HealthbarCommand::register);
	}
}
