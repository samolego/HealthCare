package org.samo_lego.healthcare.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
import org.samo_lego.healthcare.permission.PermissionHelper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.MessageArgument.message;
import static org.samo_lego.healthcare.HealthCare.LUCKPERMS_LOADED;
import static org.samo_lego.healthcare.HealthCare.MODID;
import static org.samo_lego.healthcare.HealthCare.config;

public class HealthbarCommand {
    private static final SuggestionProvider<CommandSourceStack> HEALTHBAR_STYLES;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        dispatcher.register(literal("healthbar")
                .then(literal("toggle").executes(HealthbarCommand::toggleHealthBar))
                .then(literal("preferences")
                        .then(literal("style")
                                .then(argument("style", word())
                                        .suggests(HEALTHBAR_STYLES)
                                        .executes(HealthbarCommand::editHealthbarStyle)
                                )
                        )
                        .then(literal("showEntityType")
                                .then(argument("allow entity type", BoolArgumentType.bool())
                                        .executes(HealthbarCommand::toggleEntityType)
                                )
                        )
                        .then(literal("alwaysVisible")
                                .then(argument("visibility", BoolArgumentType.bool())
                                        .executes(HealthbarCommand::changeVisibility)
                                )
                        )
                        .then(literal("custom")
                            .then(literal("healthbarLength")
                                .then(argument("length", integer(1, config.maxHealthbarLength))
                                    .executes(HealthbarCommand::editHealthbarLength)
                                )
                            )
                            .then(literal("fullSymbol")
                                    .then(argument("symbol", message())
                                            .executes(ctx -> setSymbol(ctx, true))
                                    )
                            )
                            .then(literal("emptySymbol")
                                    .then(argument("symbol", message())
                                            .executes(ctx -> setSymbol(ctx, false))
                                    )
                            )
                        )
                )
        );
    }

    private static int toggleEntityType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_edit_showEntityType, 0)) {
            context.getSource().sendFailure(Component.translatable("commands.help.failed").withStyle(ChatFormatting.RED));
            return -1;
        }

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
        return 0;
    }

    private static int editHealthbarLength(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_edit_custom_length, 0)) {
            context.getSource().sendFailure(Component.translatable("commands.help.failed").withStyle(ChatFormatting.RED));
            return -1;
        }

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

        return 0;
    }

    private static int setSymbol(CommandContext<CommandSourceStack> context, boolean full) throws CommandSyntaxException {
        if (LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), full ? config.perms.healthbar_edit_custom_symbols_full : config.perms.healthbar_edit_custom_symbols_empty, 0)) {
            context.getSource().sendFailure(Component.translatable("commands.help.failed").withStyle(ChatFormatting.RED));
            return -1;
        }

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

        return 0;
    }

    private static int changeVisibility(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_edit_visibility, 0)) {
            context.getSource().sendFailure(Component.translatable("commands.help.failed").withStyle(ChatFormatting.RED));
            return -1;
        }

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
        return 0;
    }

    private static int editHealthbarStyle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_edit_style, 0)) {
            context.getSource().sendFailure(Component.translatable("commands.help.failed").withStyle(ChatFormatting.RED));
            return -1;
        }

        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        HealthbarPreferences.HealthbarStyle style = HealthbarPreferences.HealthbarStyle.valueOf(StringArgumentType.getString(context, "style"));

        preferences.setHealthbarStyle(style);

        context.getSource().sendSuccess(
                Component.literal(String.format(config.lang.styleSet, style))
                        .withStyle(ChatFormatting.GREEN)
                        .append("\n")
                        .append(Component.literal(config.lang.reloadRequired).withStyle(ChatFormatting.GOLD)),
                false
        );
        return 0;
    }

    private static int toggleHealthBar(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_toggle, 0)) {
            context.getSource().sendFailure(Component.translatable("commands.help.failed").withStyle(ChatFormatting.RED));
            return -1;
        }

        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        preferences.setEnabled(!preferences.isEnabled());

        context.getSource().sendSuccess(
                Component.literal(preferences.isEnabled() ? config.lang.healthbarEnabled : config.lang.healthbarDisabled)
                        .withStyle(ChatFormatting.GREEN)
                        .append("\n")
                        .append(Component.literal(config.lang.reloadRequired).withStyle(ChatFormatting.GOLD)),
                false
        );

        return 0;
    }

    static {
        HEALTHBAR_STYLES = SuggestionProviders.register(
                new ResourceLocation(MODID, "healthbar_styles"),
                (context, builder) ->
                        SharedSuggestionProvider.suggest(Stream.of(HealthbarPreferences.HealthbarStyle.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
