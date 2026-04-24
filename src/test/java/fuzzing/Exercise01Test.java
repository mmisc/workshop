package fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import fuzzing.exercise01.AliasResolver;

/**
 * Exercise 01 - Your first Jazzer run.
 *
 * This test is already complete. Run it and observe the output:
 *
 *   JAZZER_FUZZ=1 mvn -Dtest=Exercise01Test test
 *
 * Expected behaviour: Jazzer finds an ArrayIndexOutOfBoundsException
 * within a few seconds and writes a crash file. Open the crash file
 * and inspect the stack trace.
 *
 * Goals for this checkpoint:
 *   - Get a working Jazzer run on your machine.
 *   - Read the libFuzzer-style status line (cov, ft, corp, exec/s, rss).
 *   - Observe where the crash file is written.
 */
class Exercise01Test {

    @FuzzTest(maxDuration = "1m")
    void fuzzAliasResolver(FuzzedDataProvider data) {
        String input = data.consumeRemainingAsString();
        AliasResolver.resolve(input);
    }
}
