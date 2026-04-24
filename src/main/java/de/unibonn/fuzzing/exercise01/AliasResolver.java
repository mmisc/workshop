package de.unibonn.fuzzing.exercise01;

/**
 * Exercise 01: A simple route-alias resolver.
 *
 * Parses strings of the form "PRIORITY:ROUTE" where PRIORITY is 1-9
 * and ROUTE is a path optionally containing a template variable in {braces}.
 * When a template variable is present, returns its first character as the alias key.
 *
 * This class contains a planted bug that Jazzer should find within about a minute.
 * Do not fix it; the goal is to observe Jazzer discovering it step by step.
 */
public final class AliasResolver {

    private AliasResolver() {}

    public static String resolve(String rule) {
        if (rule == null) return "";

        int sep = rule.indexOf(':');
        if (sep <= 0) return "";

        int priority;
        try {
            priority = Integer.parseInt(rule.substring(0, sep));
        } catch (NumberFormatException e) {
            return "";
        }
        if (priority < 1 || priority > 9) return "";

        String route = rule.substring(sep + 1);
        if (!route.startsWith("/")) return "";

        // Extract optional template variable from routes like /user/{name}/view.
        int open = route.indexOf('{');
        if (open >= 0) {
            int close = route.indexOf('}', open + 1);
            if (close > open) {
                String varName = route.substring(open + 1, close);
                // Bug: no check for empty variable name — throws
                // StringIndexOutOfBoundsException on routes like "/{}"
                return priority + ":" + varName.charAt(0);
            }
        }
        return priority + ":" + route;
    }
}
