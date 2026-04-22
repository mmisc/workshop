package de.unibonn.fuzzing.exercise01;

/**
 * Exercise 01: A tiny alias resolver.
 *
 * Parses strings of the form "alias:target" and returns either the
 * uppercased target (for the "admin" alias) or the alias itself.
 *
 * This class is used by the worked example in Exercise 01 and contains
 * at least one planted bug that Jazzer should find within seconds.
 * Do not fix it; the point is to observe Jazzer's output.
 */
public final class AliasResolver {

    private AliasResolver() {
        // utility class
    }

    public static String resolve(String input) {
        if (input == null) {
            return "";
        }
        String[] parts = input.split(":");
        if (parts[0].equals("admin")) {
            // Planted bug: when input is exactly "admin" (no colon),
            // parts.length == 1 and parts[1] throws
            // ArrayIndexOutOfBoundsException.
            return parts[1].toUpperCase();
        }
        return parts[0];
    }
}
