package org.samo_lego.healthcare.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.samo_lego.healthcare.config.HealthConfig;
import org.samo_lego.healthcare.permission.PermissionHelper;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.healthcare.HealthCare.*;

public class HealthcareCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralCommandNode<ServerCommandSource> healthcareNode = dispatcher.register(literal("healthcare")
                .requires(src -> LUCKPERMS_LOADED ? PermissionHelper.checkPermission(src, config.perms.healthcare_config, 4) : src.hasPermissionLevel(4)));

        LiteralCommandNode<ServerCommandSource> configNode = literal("config")
                .then(literal("reload")
                        .requires(src -> LUCKPERMS_LOADED ? PermissionHelper.checkPermission(src, config.perms.healthcare_config_reload, 4) : src.hasPermissionLevel(4))
                        .executes(HealthcareCommand::reloadConfig)
                )
                .build();

        LiteralCommandNode<ServerCommandSource> editNode = literal("edit")
                .requires(src -> LUCKPERMS_LOADED ? PermissionHelper.checkPermission(src, config.perms.healthcare_config_edit, 4) : src.hasPermissionLevel(4))
                .build();

        config.generateCommand(editNode);

        configNode.addChild(editNode);
        healthcareNode.addChild(configNode);
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> ctx) {
        HealthConfig newConfig = HealthConfig.loadConfigFile(CONFIG_FILE);
        config.reload(newConfig);

        ctx.getSource().sendFeedback(
                new LiteralText(config.lang.configReloaded).formatted(Formatting.GREEN),
                false
        );
        return 0;
    }
}
