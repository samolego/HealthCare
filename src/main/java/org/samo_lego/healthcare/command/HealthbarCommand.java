package org.samo_lego.healthcare.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.samo_lego.healthcare.healthbar.HealthbarPreferences;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.MessageArgumentType.message;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.healthcare.HealthCare.MODID;

public class HealthbarCommand {
    private static final SuggestionProvider<ServerCommandSource> HEALTHBAR_STYLES;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("healthbar")
                .then(literal("toggle").executes(HealthbarCommand::toggleHealthBar))
                .then(literal("edit")
                        .then(literal("style")
                                .then(argument("style", word())
                                        .suggests(HEALTHBAR_STYLES)
                                        .executes(HealthbarCommand::editHealthbarStyle)
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
                        .then(literal("alwaysVisible")
                            .then(argument("visibility", BoolArgumentType.bool())
                                .executes(HealthbarCommand::changeVisibility)
                            )
                        )
                )
        );
    }

    private static int setSymbol(CommandContext<ServerCommandSource> context, boolean full) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        char symbol = MessageArgumentType.getMessage(context, "symbol").getString().toCharArray()[0];

        if(full)
            preferences.setCustomFullChar(symbol);
        else
            preferences.setCustomEmptyChar(symbol);

        return 0;
    }

    private static int changeVisibility(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        boolean alwaysVisible = BoolArgumentType.getBool(context, "visibility");

        preferences.setAlwaysVisible(alwaysVisible);
        return 0;
    }

    private static int editHealthbarStyle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        Enum<HealthbarPreferences.HealthbarStyle> style = HealthbarPreferences.HealthbarStyle.valueOf(StringArgumentType.getString(context, "style"));

        preferences.setHealthbarStyle(style);
        return 0;
    }

    private static int toggleHealthBar(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        HealthbarPreferences preferences = (HealthbarPreferences) context.getSource().getPlayer();
        preferences.setEnabled(!preferences.isEnabled());
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
