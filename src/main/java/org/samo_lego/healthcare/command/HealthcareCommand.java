package org.samo_lego.healthcare.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.samo_lego.healthcare.config.HealthConfig;
import org.samo_lego.healthcare.permission.PermissionHelper;

import java.io.File;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.healthcare.HealthCare.LUCKPERMS_LOADED;
import static org.samo_lego.healthcare.HealthCare.config;

public class HealthcareCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("healthcare")
                .requires(src -> src.hasPermissionLevel(4) || LUCKPERMS_LOADED)
            .then(literal("reloadConfig").executes(HealthcareCommand::reloadConfig))
        );
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> ctx) {
        if(LUCKPERMS_LOADED && !PermissionHelper.checkPermission(ctx.getSource(), config.perms.healthcare_reloadConfig, 4)) {
            ctx.getSource().sendError(new LiteralText(config.lang.noPermission).formatted(Formatting.RED));
            return -1;
        }

        config = HealthConfig.loadConfigFile(new File(FabricLoader.getInstance().getConfigDir() + "/config.json"));

        ctx.getSource().sendFeedback(
                new LiteralText(config.lang.configReloaded).formatted(Formatting.GREEN),
                false
        );
        return 0;
    }
}