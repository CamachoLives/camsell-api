package com.camsell.shared.domain.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Genera identificadores legibles y estables para URLs publicas. */
public final class Slugs {

    /** Longitud maxima para evitar URLs que superen limites de servidores y bases de datos. */
    public static final int MAX_LENGTH = 100;

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern TRAILING_DASHES = Pattern.compile("-+$");

    private Slugs() {
    }

    public static String of(String... parts) {
        return of(MAX_LENGTH, parts);
    }

    /**
     * Genera un slug con longitud maxima configurable. Si el resultado supera
     * {@code maxLength}, se trunca en el ultimo guion para no cortar palabras a mitad.
     */
    public static String of(int maxLength, String... parts) {
        String joined = String.join("-", parts);
        String normalized = Normalizer.normalize(joined, Normalizer.Form.NFD);
        String withoutAccents = DIACRITICS.matcher(normalized).replaceAll("");
        String lowered = withoutAccents.toLowerCase(Locale.ROOT);
        String dashed = NON_ALPHANUMERIC.matcher(lowered).replaceAll("-");
        String trimmed = TRAILING_DASHES.matcher(dashed.stripLeading()).replaceAll("");

        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        // Trunca en el ultimo guion para no partir una palabra.
        String candidate = trimmed.substring(0, maxLength);
        int lastDash = candidate.lastIndexOf('-');
        return lastDash > 0 ? candidate.substring(0, lastDash) : candidate;
    }
}
