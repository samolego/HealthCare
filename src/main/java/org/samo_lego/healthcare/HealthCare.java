package org.samo_lego.healthcare;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.samo_lego.healthcare.command.HealthbarCommand;
import org.samo_lego.healthcare.command.HealthcareCommand;
import org.samo_lego.healthcare.config.HealthConfig;
import org.samo_lego.healthcare.event.EventHandler;

import java.io.File;

public class HealthCare implements ModInitializer {
	public static final String MODID = "healthcare";

	public static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir() + "/health_config.json");

	public static boolean LUCKPERMS_LOADED;

	public static HealthConfig config;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(HealthbarCommand::register);
		CommandRegistrationCallback.EVENT.register(HealthcareCommand::register);

		EventHandler eventHandler = new EventHandler();
		ServerPlayerEvents.COPY_FROM.register(eventHandler);

		config = HealthConfig.loadConfigFile(CONFIG_FILE);
		LUCKPERMS_LOADED = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
	}
}
