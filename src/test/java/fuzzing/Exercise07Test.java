package fuzzing;

import com.code_intelligence.jazzer.api.BugDetectors;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import fuzzing.exercise07.TemplateReader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Exercise 07 - Bug detectors (sanitizers) in action.
 *
 * Everything you have seen so far found bugs by waiting for an
 * uncaught exception. Jazzer can also find bugs via its bug
 * detectors (sometimes called sanitizers). Each detector encodes
 * knowledge about a class of security vulnerability:
 *
 *   - SqlInjection
 *   - OsCommandInjection
 *   - LdapInjection
 *   - ServerSideRequestForgery
 *   - FilePathTraversal   <-- we use this one
 *   - InsecureDeserialization
 *   - ... and more
 *
 * FilePathTraversal can be configured at runtime via the
 * BugDetectors API: you declare which directory is "allowed", and
 * Jazzer reports any file access outside that directory as a
 * FuzzerSecurityIssueHigh.
 *
 * This is MUCH safer than, say, an OS command injection demo:
 * even if the fuzzer produces a wild path, nothing harmful
 * happens. Files.exists() and Files.readString() on a bogus
 * path just return false or throw NoSuchFileException.
 *
 * Your task:
 *
 *   1. Run the test and read the finding:
 *
 *        JAZZER_FUZZ=1 mvn -Dtest=Exercise07Test test
 *
 *   2. Compare the output to what you saw in Exercise 01: how does
 *      a FuzzerSecurityIssue differ from a plain NPE or AIOOBE in
 *      the Jazzer output?
 *
 *   3. Apply the fix in TemplateReader.java (commented at the
 *      bottom of that file) and re-run. Confirm that Jazzer no
 *      longer reports a finding.
 */
class Exercise07Test {

    @FuzzTest(maxDuration = "1m")
    void fuzzTemplateReader(FuzzedDataProvider data) throws IOException {
        // Tell Jazzer that only files inside this directory are
        // considered "safe". Any Files.* access to a path outside
        // this directory becomes a reported finding.
        Path allowed =
                Paths.get(System.getProperty("java.io.tmpdir"), "workshop-templates")
                        .toAbsolutePath()
                        .normalize();
        BugDetectors.setFilePathTraversalAllowPath(p -> p.startsWith(allowed));

        String name = data.consumeRemainingAsString();
        TemplateReader.readTemplate(name);
    }
}
