package org.samo_lego.healthcare.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.MessageArgument.message;
import static org.samo_lego.healthcare.HealthCare.MODID;
import static org.samo_lego.healthcare.HealthCare.config;

public class HealthbarCommand {
    private static final SuggestionProvider<CommandSourceStack> HEALTHBAR_STYLES;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        dispatcher.register(literal("healthbar")
                .requires(src -> Permissions.check(src, "healthcare.healthbar", true))
                .then(literal("toggle")
                        .requires(src -> Permissions.check(src, "healthcare.healthbar.toggle", true))
                        .executes(HealthbarCommand::toggleHealthBar))
                .then(literal("edit")
                        .requires(src -> Permissions.check(src, "healthcare.healthbar.edit", true))
                        .then(literal("style")
                                .requires(src -> Permissions.check(src, "healthcare.healthbar.edit.style", true))
                                .then(argument("style", word())
                                        .suggests(HEALTHBAR_STYLES)
                                        .executes(HealthbarCommand::editHealthbarStyle)
                                )
                        )
                        .then(literal("showEntityType")
                                .requires(src -> Permissions.check(src, "healthcare.healthbar.edit.show_entity_type", true))
                                .then(argument("allow entity type", BoolArgumentType.bool())
                                        .executes(HealthbarCommand::toggleEntityType)
                                )
                        )
                        .then(literal("alwaysVisible")
                                .requires(src -> Permissions.check(src, "healthcare.healthbar.edit.visibility", true))
                                .then(argument("visibility", BoolArgumentType.bool())
                                        .executes(HealthbarCommand::changeVisibility)
                                )
                        )
                        .then(literal("custom")
                                .requires(src -> Permissions.check(src, "healthcare.healthbar.edit.custom", true))
                            .then(literal("healthbarLength")
                                    .requires(src -> Permissions.check(src, "healthcare.healthbar.edit.custom.length", true))
                                .then(argument("length", integer(1, config.maxHealthbarLength))
                                    .executes(HealthbarCommand::editHealthbarLength)
                                )
                            )
                            .then(literal("fullSymbol")
                                    .requires(src -> Permissions.check(src, "healthcare.healthbar.edit.custom.symbol", true))
                                    .then(argument("symbol", message())
                                            .executes(ctx -> setSymbol(ctx, true))
                                    )
                            )
                            .then(literal("emptySymbol")
                                    .requires(src -> Permissions.check(src, "healthcare.healthbar.edit.custom.symbol", true))
                                    .then(argument("symbol", message())
                                            .executes(ctx -> setSymbol(ctx, false))
                                    )
                            )
                        )
                )
        );
    }

    private static int toggleEntityType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        boolean allowEntityType = BoolArgumentType.getBool(context, "allow entity type");

        preferences.setShowEntityType(allowEntityType);

        context.getSource().sendSuccess(
                Component.literal(String.format(config.lang.toggledType, allowEntityType))
                        .withStyle(ChatFormatting.GREEN)
                        .append("\n")
                        .append(Component.literal(config.lang.reloadRequired).withStyle(ChatFormatting.GOLD)),
                false
        );
        return 1;
    }

    private static int editHealthbarLength(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        int length = IntegerArgumentType.getInteger(context, "length");
        preferences.setCustomLength(length);

        context.getSource().sendSuccess(
                Component.literal(String.format(config.lang.customLengthSet, length))
                        .withStyle(ChatFormatting.GREEN)
                        .append("\n")
                        .append(Component.literal(config.lang.reloadRequired).withStyle(ChatFormatting.GOLD)),
                false
        );
        if (!preferences.getHealthbarStyle().equals(HealthbarPreferences.HealthbarStyle.CUSTOM)) {
            context.getSource().sendSuccess(
                    Component.literal(config.lang.useCustomStyle).withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        return 1;
    }

    private static int setSymbol(CommandContext<CommandSourceStack> context, boolean full) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        char symbol = MessageArgument.getMessage(context, "symbol").getString().toCharArray()[0];

        if (full)
            preferences.setCustomFullChar(symbol);
        else
            preferences.setCustomEmptyChar(symbol);

        context.getSource().sendSuccess(
                Component.literal(String.format(config.lang.customSymbolSet, full ? "Full" : "Empty", symbol))
                        .withStyle(ChatFormatting.GREEN)
                        .append("\n")
                        .append(Component.literal(config.lang.reloadRequired).withStyle(ChatFormatting.GOLD)),
                false
        );
        if (!preferences.getHealthbarStyle().equals(HealthbarPreferences.HealthbarStyle.CUSTOM)) {
            context.getSource().sendSuccess(
                    Component.literal(config.lang.useCustomStyle).withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        return 1;
    }

    private static int changeVisibility(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        boolean alwaysVisible = BoolArgumentType.getBool(context, "visibility");

        preferences.setAlwaysVisible(alwaysVisible);

        context.getSource().sendSuccess(
                Component.literal(String.format(config.lang.visibilitySet, alwaysVisible))
                        .withStyle(ChatFormatting.GREEN)
                        .append("\n")
                        .append(Component.literal(config.lang.reloadRequired).withStyle(ChatFormatting.GOLD)),
                false
        );
        return 1;
    }

    private static int editHealthbarStyle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        var style = HealthbarPreferences.HealthbarStyle.valueOf(StringArgumentType.getString(context, "style"));

        preferences.setHealthbarStyle(style);

        context.getSource().sendSuccess(
                Component.literal(String.format(config.lang.styleSet, style))
                        .withStyle(ChatFormatting.GREEN)
                        .append("\n")
                        .append(Component.literal(config.lang.reloadRequired).withStyle(ChatFormatting.GOLD)),
                false
        );
        return 1;
    }

    private static int toggleHealthBar(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        preferences.setEnabled(!preferences.isEnabled());

        context.getSource().sendSuccess(
                Component.literal(preferences.isEnabled() ? config.lang.healthbarEnabled : config.lang.healthbarDisabled)
                        .withStyle(ChatFormatting.GREEN)
                        .append("\n")
                        .append(Component.literal(config.lang.reloadRequired).withStyle(ChatFormatting.GOLD)),
                false
        );

        return 1;
    }

    static {
        HEALTHBAR_STYLES = SuggestionProviders.register(
                new ResourceLocation(MODID, "healthbar_styles"),
                (context, builder) ->
                        SharedSuggestionProvider.suggest(Stream.of(HealthbarPreferences.HealthbarStyle.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
