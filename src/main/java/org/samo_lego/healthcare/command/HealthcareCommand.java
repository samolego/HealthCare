package org.samo_lego.healthcare.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.samo_lego.healthcare.config.HealthConfig;
import org.samo_lego.healthcare.permission.PermissionHelper;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.healthcare.HealthCare.CONFIG_FILE;
import static org.samo_lego.healthcare.HealthCare.LUCKPERMS_LOADED;
import static org.samo_lego.healthcare.HealthCare.config;

public class HealthcareCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        LiteralCommandNode<CommandSourceStack> healthcareNode = dispatcher.register(literal("healthcare")
                .requires(src -> LUCKPERMS_LOADED ? PermissionHelper.checkPermission(src, config.perms.healthcare_config, 4) : src.hasPermission(4)));

        LiteralCommandNode<CommandSourceStack> configNode = literal("config")
                .then(literal("reload")
                        .requires(src -> LUCKPERMS_LOADED ? PermissionHelper.checkPermission(src, config.perms.healthcare_config_reload, 4) : src.hasPermission(4))
                        .executes(HealthcareCommand::reloadConfig)
                )
                .build();

        LiteralCommandNode<CommandSourceStack> editNode = literal("edit")
                .requires(src -> LUCKPERMS_LOADED ? PermissionHelper.checkPermission(src, config.perms.healthcare_config_edit, 4) : src.hasPermission(4))
                .build();

        config.generateCommand(editNode);

        configNode.addChild(editNode);
        healthcareNode.addChild(configNode);
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        HealthConfig newConfig = HealthConfig.loadConfigFile(CONFIG_FILE);
        config.reload(newConfig);

        ctx.getSource().sendSuccess(
                Component.literal(config.lang.configReloaded).withStyle(ChatFormatting.GREEN),
                false
        );
        return 0;
    }
}
