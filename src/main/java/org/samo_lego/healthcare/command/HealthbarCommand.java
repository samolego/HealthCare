package org.samo_lego.healthcare.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;
import org.samo_lego.healthcare.permission.PermissionHelper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.healthcare.HealthCare.*;

public class HealthbarCommand {
    private static final SuggestionProvider<ServerCommandSource> HEALTHBAR_STYLES;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
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

    private static int toggleEntityType(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_edit_showEntityType, 0)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        boolean allowEntityType = BoolArgumentType.getBool(context, "allow entity type");

        preferences.setShowEntityType(allowEntityType);

        context.getSource().sendFeedback(
                new LiteralText(String.format(config.lang.toggledType, allowEntityType))
                        .formatted(Formatting.GREEN)
                        .append("\n")
                        .append(new LiteralText(config.lang.reloadRequired).formatted(Formatting.GOLD)),
                false
        );
        return 0;
    }

    private static int editHealthbarLength(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_edit_custom_length, 0)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        int length = IntegerArgumentType.getInteger(context, "length");
        preferences.setCustomLength(length);

        context.getSource().sendFeedback(
                new LiteralText(String.format(config.lang.customLengthSet, length))
                    .formatted(Formatting.GREEN)
                    .append("\n")
                    .append(new LiteralText(config.lang.reloadRequired).formatted(Formatting.GOLD)),
                false
        );
        if(!preferences.getHealthbarStyle().equals(HealthbarPreferences.HealthbarStyle.CUSTOM)) {
            context.getSource().sendFeedback(
                    new LiteralText(config.lang.useCustomStyle).formatted(Formatting.GOLD),
                    false
            );
        }

        return 0;
    }

    private static int setSymbol(CommandContext<ServerCommandSource> context, boolean full) throws CommandSyntaxException {
        if(LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), full ? config.perms.healthbar_edit_custom_symbols_full : config.perms.healthbar_edit_custom_symbols_empty, 0)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        char symbol = MessageArgumentType.getMessage(context, "symbol").getString().toCharArray()[0];

        if(full)
            preferences.setCustomFullChar(symbol);
        else
            preferences.setCustomEmptyChar(symbol);

        context.getSource().sendFeedback(
                new LiteralText(String.format(config.lang.customSymbolSet, full ? "Full" : "Empty", symbol))
                    .formatted(Formatting.GREEN)
                    .append("\n")
                    .append(new LiteralText(config.lang.reloadRequired).formatted(Formatting.GOLD)),
                false
        );
        if(!preferences.getHealthbarStyle().equals(HealthbarPreferences.HealthbarStyle.CUSTOM)) {
            context.getSource().sendFeedback(
                    new LiteralText(config.lang.useCustomStyle).formatted(Formatting.GOLD),
                    false
            );
        }

        return 0;
    }

    private static int changeVisibility(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_edit_visibility, 0)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        boolean alwaysVisible = BoolArgumentType.getBool(context, "visibility");

        preferences.setAlwaysVisible(alwaysVisible);

        context.getSource().sendFeedback(
                new LiteralText(String.format(config.lang.visibilitySet, alwaysVisible))
                    .formatted(Formatting.GREEN)
                    .append("\n")
                    .append(new LiteralText(config.lang.reloadRequired).formatted(Formatting.GOLD)),
                false
        );
        return 0;
    }

    private static int editHealthbarStyle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_edit_style, 0)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        HealthbarPreferences.HealthbarStyle style = HealthbarPreferences.HealthbarStyle.valueOf(StringArgumentType.getString(context, "style"));

        preferences.setHealthbarStyle(style);

        context.getSource().sendFeedback(
                new LiteralText(String.format(config.lang.styleSet, style))
                    .formatted(Formatting.GREEN)
                    .append("\n")
                    .append(new LiteralText(config.lang.reloadRequired).formatted(Formatting.GOLD)),
                false
        );
        return 0;
    }

    private static int toggleHealthBar(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(LUCKPERMS_LOADED && !PermissionHelper.checkPermission(context.getSource(), config.perms.healthbar_toggle, 0)) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        preferences.setEnabled(!preferences.isEnabled());

        context.getSource().sendFeedback(
                new LiteralText(preferences.isEnabled() ? config.lang.healthbarEnabled : config.lang.healthbarDisabled)
                    .formatted(Formatting.GREEN)
                    .append("\n")
                    .append(new LiteralText(config.lang.reloadRequired).formatted(Formatting.GOLD)),
                false
        );

        return 0;
    }

    static {
        HEALTHBAR_STYLES = SuggestionProviders.register(
                new Identifier(MODID, "healthbar_styles"),
                (context, builder) ->
                        CommandSource.suggestMatching(Stream.of(HealthbarPreferences.HealthbarStyle.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
