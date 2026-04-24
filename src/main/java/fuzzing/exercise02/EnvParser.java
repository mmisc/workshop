package fuzzing.exercise02;

import java.util.HashMap;
import java.util.Map;

/**
 * Exercise 02: A line-oriented environment variable parser.
 *
 * Parses strings of the form:
 *
 *     KEY1=value1
 *     KEY2=value2
 *     # a comment
 *     PORT_NUM=8080
 *
 * Keys ending with "_NUM" are validated as integers.
 *
 * There are several bugs in this parser. You do not need to fix them;
 * your job is only to write a fuzz target that calls parse() with
 * fuzzed input and see what Jazzer finds.
 */
public final class EnvParser {

    private EnvParser() {
        // utility class
    }

    public static Map<String, String> parse(String input) {
        Map<String, String> result = new HashMap<>();
        if (input == null) {
            return result;
        }
        for (String line : input.split("\n")) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            // Bug: if the line has no '=', eq is -1 and substring
            // throws StringIndexOutOfBoundsException.
            String key = line.substring(0, eq);
            String value = line.substring(eq + 1);
            if (key.endsWith("_NUM")) {
                // Bug: malformed numbers throw NumberFormatException
                // and are not caught.
                result.put(key, String.valueOf(Integer.parseInt(value)));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
}
