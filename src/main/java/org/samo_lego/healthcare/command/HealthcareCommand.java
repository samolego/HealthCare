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
import static org.samo_lego.healthcare.HealthCare.MODID;
import static org.samo_lego.healthcare.HealthCare.config;

public class HealthcareCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        config.generateReloadableConfigCommand(MODID, dispatcher, () -> HealthConfig.loadConfigFile(CONFIG_FILE));
    }
}
