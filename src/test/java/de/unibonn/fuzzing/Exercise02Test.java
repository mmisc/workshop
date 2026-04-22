package de.unibonn.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import de.unibonn.fuzzing.exercise02.EnvParser;

/**
 * Exercise 02 - Write your first fuzz target.
 *
 * This class is empty on purpose. Your task:
 *
 *   1. Add a method annotated with @FuzzTest.
 *   2. Call EnvParser.parse() with fuzzed input.
 *
 * Start with the simplest possible signature: a single String or byte[]
 * parameter. Once that runs, upgrade to a FuzzedDataProvider (see the
 * Jazzer docs or Exercise 01 for an example).
 *
 * Run with:
 *
 *   JAZZER_FUZZ=1 mvn -Dtest=Exercise02Test test
 *
 * If you see "No tests were executed", your @FuzzTest method is not
 * being picked up; check the annotation import and the method signature.
 */
class Exercise02Test {

    // TODO: add an @FuzzTest method that calls EnvParser.parse(...)

}
