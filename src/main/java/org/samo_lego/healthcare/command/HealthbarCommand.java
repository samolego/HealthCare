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
import org.samo_lego.healthcare.healthbar.HealthbarStyle;

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

        preferences.healthcare_healthcarePrefs().showType = allowEntityType;

        context.getSource().sendSuccess(() ->
                        Component.translatable("healthcare.healthbar.toggledType", Component.literal(String.valueOf(allowEntityType)))
                                .withStyle(ChatFormatting.GREEN)
                                .append("\n")
                                .append(Component.translatable("healthcare.healthbar.reloadRequired").withStyle(ChatFormatting.GOLD)),
                false
        );
        return 1;
    }

    private static int editHealthbarLength(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        int length = IntegerArgumentType.getInteger(context, "length");
        preferences.healthcare_healthcarePrefs().customLength = length;

        context.getSource().sendSuccess(() ->
                        Component.translatable("healthcare.healthbar.customLengthSet", Component.literal(String.valueOf(length)))
                                .withStyle(ChatFormatting.GREEN)
                                .append("\n")
                                .append(Component.translatable("healthcare.healthbar.reloadRequired").withStyle(ChatFormatting.GOLD)),
                false
        );
        if (preferences.healthcare_healthcarePrefs().healthbarStyle != HealthbarStyle.CUSTOM) {
            context.getSource().sendSuccess(() ->
                            Component.translatable("healthcare.healthbar.useCustomStyle").withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        return 1;
    }

    private static int setSymbol(CommandContext<CommandSourceStack> context, boolean full) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        char symbol = MessageArgument.getMessage(context, "symbol").getString().toCharArray()[0];

        if (full) {
            preferences.healthcare_healthcarePrefs().customFullChar = symbol;
        } else {
            preferences.healthcare_healthcarePrefs().customEmptyChar = symbol;
        }

        context.getSource().sendSuccess(() ->
                        Component.translatable("healthcare.healthbar.customSymbolSet", Component.literal(full ? "Full" : "Empty"), Component.literal(String.valueOf(symbol)))
                                .withStyle(ChatFormatting.GREEN)
                                .append("\n")
                                .append(Component.translatable("healthcare.healthbar.reloadRequired").withStyle(ChatFormatting.GOLD)),
                false
        );
        if (preferences.healthcare_healthcarePrefs().healthbarStyle != HealthbarStyle.CUSTOM) {
            context.getSource().sendSuccess(() ->
                            Component.translatable("healthcare.healthbar.useCustomStyle").withStyle(ChatFormatting.GOLD),
                    false
            );
        }

        return 1;
    }

    private static int changeVisibility(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        boolean alwaysVisible = BoolArgumentType.getBool(context, "visibility");

        preferences.healthcare_healthcarePrefs().alwaysVisible = alwaysVisible;

        context.getSource().sendSuccess(() ->
                        Component.translatable("healthcare.healthbar.visibilitySet", Component.literal(String.valueOf(alwaysVisible)))
                                .withStyle(ChatFormatting.GREEN)
                                .append("\n")
                                .append(Component.translatable("healthcare.healthbar.reloadRequired").withStyle(ChatFormatting.GOLD)),
                false
        );
        return 1;
    }

    private static int editHealthbarStyle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        var style = HealthbarStyle.valueOf(StringArgumentType.getString(context, "style"));

        preferences.healthcare_healthcarePrefs().healthbarStyle = style;

        context.getSource().sendSuccess(() ->
                        Component.translatable("healthcare.healthbar.styleSet", Component.literal(style.toString()))
                                .withStyle(ChatFormatting.GREEN)
                                .append("\n")
                                .append(Component.translatable("healthcare.healthbar.reloadRequired").withStyle(ChatFormatting.GOLD)),
                false
        );
        return 1;
    }

    private static int toggleHealthBar(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayerOrException();
        boolean enabled = !preferences.healthcare_healthcarePrefs().enabled;
        preferences.healthcare_healthcarePrefs().enabled = enabled;

        context.getSource()
                .sendSuccess(() ->
                        Component.translatable(enabled ? "healthcare.healthbar.healthbarEnabled" : "healthcare.healthbar.healthbarDisabled")
                                .withStyle(ChatFormatting.GREEN)
                                .append("\n")
                                .append(Component.translatable("healthcare.healthbar.reloadRequired").withStyle(ChatFormatting.GOLD)),
                false
        );

        return 1;
    }

    static {
        HEALTHBAR_STYLES = SuggestionProviders.register(
                ResourceLocation.fromNamespaceAndPath(MODID, "healthbar_styles"),
                (context, builder) ->
                        SharedSuggestionProvider.suggest(Stream.of(HealthbarStyle.values()).map(Enum::name).collect(Collectors.toList()), builder)
        );
    }
}
