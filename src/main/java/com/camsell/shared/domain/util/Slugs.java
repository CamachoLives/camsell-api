package com.camsell.shared.domain.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Genera identificadores legibles y estables para URLs publicas. */
public final class Slugs {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-+)|(-+$)");

    private Slugs() {
    }

    public static String of(String... parts) {
        String joined = String.join("-", parts);
        String normalized = Normalizer.normalize(joined, Normalizer.Form.NFD);
        String withoutAccents = DIACRITICS.matcher(normalized).replaceAll("");
        String lowered = withoutAccents.toLowerCase(Locale.ROOT);
        String dashed = NON_ALPHANUMERIC.matcher(lowered).replaceAll("-");
        return EDGE_DASHES.matcher(dashed).replaceAll("");
    }
}
