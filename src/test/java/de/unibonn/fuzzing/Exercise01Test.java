package de.unibonn.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import de.unibonn.fuzzing.exercise01.AliasResolver;

/**
 * Exercise 01 - Your first Jazzer run.
 *
 * This test is already complete. Run it and observe the output:
 *
 *   JAZZER_FUZZ=1 mvn -Dtest=Exercise01Test test
 *
 * Expected behaviour: Jazzer explores the input space for ~30–60 seconds,
 * then finds a StringIndexOutOfBoundsException and writes a crash file.
 * Watch the status lines while it runs — that is Checkpoint 1.
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
        // Artificial slowdown so exec/s stays in a readable range and
        // participants can watch several status lines before the crash.
    }
}
