package de.unibonn.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.DictionaryFile;
import com.code_intelligence.jazzer.junit.FuzzTest;

import de.unibonn.fuzzing.exercise05.QueryEngine;

/**
 * Exercise 05 - Dictionaries.
 *
 * QueryEngine.execute() dispatches on SQL-like keywords (SELECT,
 * INSERT, UPDATE, DELETE, CREATE TABLE). Without help, Jazzer has
 * to guess these keywords byte-by-byte from random mutations, which
 * takes a while. Inputs that almost match are given a bonus by the
 * value profile via hooks on String#startsWith, but for short
 * queries from a blank slate, it is still slow.
 *
 * Your task:
 *
 *   1. Run the test below for 30 seconds and note the final `cov`.
 *   2. Enable the dictionary (uncomment the @DictionaryFile line
 *      below) and run again for 30 seconds.
 *   3. Compare the two `cov` values. The dictionary file lives at
 *      src/test/resources/de/unibonn/fuzzing/exercise05.dict
 *
 * Run with:
 *
 *   JAZZER_FUZZ=1 mvn -Dtest=Exercise05Test test
 */
class Exercise05Test {

    @FuzzTest(maxDuration = "1m")
    // Uncomment the next line for the second run:
    // @DictionaryFile(resourcePath = "de/unibonn/fuzzing/exercise05.dict")
    void fuzzQueryEngine(FuzzedDataProvider data) {
        String query = data.consumeRemainingAsString();
        try {
            QueryEngine.execute(query);
        } catch (RuntimeException expected) {
            // Ignore parse-level runtime exceptions from bad queries.
            // (Real findings come from unhandled errors like AIOOBE.)
        }
    }
}
