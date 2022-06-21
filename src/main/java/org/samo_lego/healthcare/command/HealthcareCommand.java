package org.samo_lego.healthcare.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.samo_lego.healthcare.config.HealthConfig;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.healthcare.HealthCare.CONFIG_FILE;
import static org.samo_lego.healthcare.HealthCare.config;

public class HealthcareCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        LiteralCommandNode<CommandSourceStack> healthcareNode = dispatcher.register(literal("healthcare")
                .requires(src -> Permissions.check(src, "healthcare.config", 4)));

        LiteralCommandNode<CommandSourceStack> configNode = literal("config")
                .then(literal("reload")
                        .requires(src -> Permissions.check(src, "healthcare.config.reload", 4))
                        .executes(HealthcareCommand::reloadConfig)
                )
                .build();

        LiteralCommandNode<CommandSourceStack> editNode = literal("edit")
                .requires(src -> Permissions.check(src, "healthcare.config.edit", 4))
                .build();

        config.generateCommand(editNode);

        configNode.addChild(editNode);
        healthcareNode.addChild(configNode);
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        HealthConfig newConfig = HealthConfig.loadConfigFile(CONFIG_FILE);
        config.reload(newConfig);

        ctx.getSource().sendSuccess(
                Component.translatable("healthcare.config_reload_success").withStyle(ChatFormatting.GREEN),
                false
        );
        return 1;
    }
}
