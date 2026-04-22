package de.unibonn.fuzzing.exercise03;

/**
 * Exercise 03: A small numeric request processor.
 *
 * Takes a comma-separated "a,b" string, parses both as integers, and
 * returns a / b. Contains bugs that are easy to trigger with fuzzing
 * IF the fuzz target is written well.
 *
 * Do not modify this class. The exercise is about the test class
 * (Exercise03Test), which contains a deliberately bad fuzz target.
 */
public final class RequestProcessor {

    private RequestProcessor() {
        // utility class
    }

    public static int compute(String input) {
        if (input == null) {
            return 0;
        }
        String[] parts = input.split(",");
        if (parts.length < 2) {
            return -1;
        }
        int a = Integer.parseInt(parts[0].trim());
        int b = Integer.parseInt(parts[1].trim());
        // Bug: ArithmeticException on b == 0.
        return a / b;
    }
}
