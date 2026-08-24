package it.university.survivor.view;

import it.university.survivor.model.Item;
import it.university.survivor.model.ModifierType;
import it.university.survivor.model.StatType;

import java.util.Locale;
import java.util.Objects;

final class ItemEffectFormatter {

    private ItemEffectFormatter() {
    }

    static String format(Item item) {
        Objects.requireNonNull(item, "Item must not be null");

        String statLabel = item.baseModifier().statType() == StatType.MAX_HEALTH
                ? "MAX HEALTH"
                : item.baseModifier().statType().name();
        return formatEffectiveValue(item) + " " + statLabel;
    }

    private static String formatEffectiveValue(Item item) {
        double effectiveValue = item.getEffectiveValue();
        ModifierType modifierType = item.baseModifier().modifierType();

        if (modifierType == ModifierType.PERCENTAGE) {
            return formatSignedCompact(effectiveValue * 100.0, 2) + "%";
        }
        if (item.baseModifier().statType() == StatType.COOLDOWN) {
            return formatSigned(effectiveValue, 2) + "s";
        }
        return formatSignedCompact(effectiveValue, 2);
    }

    private static String formatSignedCompact(
            double value,
            int maximumDecimalPlaces
    ) {
        String formatted = formatSigned(value, maximumDecimalPlaces);
        int endIndex = formatted.length();
        while (endIndex > 1 && formatted.charAt(endIndex - 1) == '0') {
            endIndex--;
        }
        if (formatted.charAt(endIndex - 1) == '.') {
            endIndex--;
        }
        return formatted.substring(0, endIndex);
    }

    private static String formatSigned(double value, int decimalPlaces) {
        String format = "%+." + decimalPlaces + "f";
        String formatted = String.format(Locale.ROOT, format, value);
        if (formatted.charAt(0) == '-' && isFormattedZero(formatted)) {
            return "+" + formatted.substring(1);
        }
        return formatted;
    }

    private static boolean isFormattedZero(String formatted) {
        for (int index = 1; index < formatted.length(); index++) {
            char character = formatted.charAt(index);
            if (character != '0' && character != '.') {
                return false;
            }
        }
        return true;
    }
}
