package org.samo_lego.healthcare.healthbar;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiFunction;

public enum HealthbarStyle {
    CUSTOM((health, maxHealth) -> Component.empty()),  // Handled differently
    HEARTS(HealthbarStyle::getHeartsText),
    LINES(HealthbarStyle::getLinesText),
    NUMBER(HealthbarStyle::getNumberText),
    PERCENTAGE(HealthbarStyle::getPercentageText),
    SKYBLOCK(HealthbarStyle::getSkyblockText);

    private final BiFunction<Float, Float, MutableComponent> textFunction;

    HealthbarStyle(BiFunction<Float, Float, MutableComponent> textFunction) {
        this.textFunction = textFunction;
    }

    public MutableComponent getText(Float health, Float maxHealth) {
        return textFunction.apply(health, maxHealth);
    }

    private static MutableComponent getHealthText(String first, String second) {
        return Component.literal(first)
                .withStyle(ChatFormatting.RED) /*health > maxHealth / 3 ? (health > maxHealth * 1.5F ? Formatting.YELLOW : Formatting.GOLD) : */
                .append(Component.literal(second).withStyle(ChatFormatting.GRAY));
    }


    private static MutableComponent getSkyblockText(Float health, Float maxHealth) {
        // see https://github.com/samolego/HealthCare/issues/2
        return Component.literal(String.format("%.2f", health))
                .withStyle(health > maxHealth / 2 ? ChatFormatting.GREEN : ChatFormatting.YELLOW)
                .append(Component.literal("/")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf((int) Math.ceil(maxHealth)))
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal("❤")
                        .withStyle(ChatFormatting.RED));
    }


    private static MutableComponent getLinesText(Float health, Float maxHealth) {
        return getCustomHealthbarText('|', '|', 20, health, maxHealth);

    }

    private static MutableComponent getHeartsText(Float health, Float maxHealth) {
        return getCustomHealthbarText('♡', '♥', 10, health, maxHealth);
    }

    private static MutableComponent getPercentageText(Float health, Float maxHealth) {
        return getHealthText(String.format("%.2f", health * 100.0F / maxHealth).concat("%"), "");
    }

    private static MutableComponent getNumberText(Float health, Float maxHealth) {
        var first = String.format("%.2f", health);
        var second = "/" + maxHealth;
        return getHealthText(first, second);
    }

    public static MutableComponent getCustomHealthbarText(char empty, char full, int length, float health, float maxHealth) {
        int heartCount = Math.min((int) Math.ceil(maxHealth), length);

        // Hearts that should be colored red
        var fullHearts = (int) Math.ceil(health * heartCount / maxHealth);

        var first = StringUtils.repeat(full, fullHearts);
        var second = StringUtils.repeat(empty, heartCount - fullHearts);
        return getHealthText(first, second);
    }
}
