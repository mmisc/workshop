package fuzzing.exercise07;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exercise 07: A file reader with a classic path traversal bug.
 *
 * readTemplate() is meant to return the contents of a template file
 * from a fixed templates directory. It trusts the caller-supplied
 * name without canonicalization, so an input like "../../etc/passwd"
 * escapes the intended directory.
 *
 * Jazzer's built-in FilePathTraversal sanitizer detects when a file
 * outside a whitelisted path is opened. The test class in
 * Exercise07Test configures that sanitizer.
 *
 * After you see the finding, apply the fix suggested in the comment
 * at the bottom of this class and re-run. Jazzer should then report
 * no finding.
 */
public final class TemplateReader {

    private static final Path TEMPLATES_DIR =
            Paths.get(System.getProperty("java.io.tmpdir"), "workshop-templates");

    private TemplateReader() {
        // utility class
    }

    /**
     * Read a template file by name from the templates directory.
     *
     * @param name a file name supplied by the caller
     * @return the file contents as a String, or empty if missing
     */
    public static String readTemplate(String name) throws IOException {
        if (name == null || name.isEmpty()) {
            return "";
        }
        // Bug: naive path join. No canonicalization, no containment
        // check, so "../" segments escape TEMPLATES_DIR.
        Path target = TEMPLATES_DIR.resolve(name);
        if (!Files.exists(target)) {
            return "";
        }
        return Files.readString(target);
    }

    /*
     * FIX (apply after you have seen Jazzer's finding):
     *
     *   Path base = TEMPLATES_DIR.toAbsolutePath().normalize();
     *   Path target = base.resolve(name).toAbsolutePath().normalize();
     *   if (!target.startsWith(base)) {
     *       throw new IllegalArgumentException("path escapes templates dir");
     *   }
     *   if (!Files.exists(target)) return "";
     *   return Files.readString(target);
     */
}
