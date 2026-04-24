package fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import fuzzing.exercise03.RequestProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Exercise 03 - Refactor a bad fuzz target.
 *
 * This fuzz target COMPILES and RUNS but will find nothing useful
 * in any reasonable amount of time. It contains at least four
 * problems that are common mistakes in real fuzz targets.
 *
 * Your task:
 *
 *   1. Run it first and record the baseline (exec/s, cov, findings
 *      after 30 seconds). Write this into your worksheet.
 *   2. Identify the problems one at a time and fix them.
 *   3. After each fix, record how exec/s and cov change.
 *
 * Run with:
 *
 *   JAZZER_FUZZ=1 mvn -Dtest=Exercise03Test test
 *
 * Hint: there are four problems. Two of them affect exec/s
 * dramatically, one of them hides all findings, and one of them
 * makes iterations influence each other in subtle ways.
 */
class Exercise03Test {

    // Problem hint 1: this state accumulates across iterations.
    private static final List<String> seenInputs = new ArrayList<>();

    @FuzzTest(maxDuration = "1m")
    void fuzzBadTarget(FuzzedDataProvider data) throws IOException {
        String input = data.consumeRemainingAsString();

        // Problem hint 2: rejecting almost all fuzzer-generated inputs.
        if (input.length() < 20 || !input.startsWith("REQUEST:")) {
            return;
        }

        // Problem hint 3: synchronous disk I/O on every iteration.
        File tmp = File.createTempFile("fuzz", ".tmp");
        tmp.deleteOnExit();
        Files.writeString(tmp.toPath(), input);

        // Accumulate (problem hint 1 manifests here).
        seenInputs.add(input);

        // Problem hint 4: swallowing every exception hides all findings.
        try {
            RequestProcessor.compute(input.substring("REQUEST:".length()));
        } catch (Throwable t) {
            // Intentionally ignored. This is wrong.
        }
    }
}
