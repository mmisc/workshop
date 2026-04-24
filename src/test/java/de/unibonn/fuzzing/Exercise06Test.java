package de.unibonn.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import de.unibonn.fuzzing.exercise06.QueueNameResolver;

import org.springframework.expression.ExpressionException;

/**
 * Exercise 06 - Instrumentation scope.
 *
 * QueueNameResolver.resolve() delegates most of its work to the
 * SBB Spring Cloud Stream Binder (com.solace.**) and Spring
 * Expression (org.springframework.expression.**). By default,
 * Jazzer instruments BOTH your code AND all third-party libraries
 * on the classpath (only JDK internals are excluded).
 *
 * That sounds good, but it has a cost:
 *
 *   - More instrumented classes = more coverage signal to mutate
 *     against, BUT also more irrelevant signal (library internals
 *     the fuzzer cannot meaningfully steer).
 *   - More instrumented classes = slower per-iteration throughput.
 *
 * Your task:
 *
 *   1. Run with default instrumentation scope for 30 seconds:
 *
 *        JAZZER_FUZZ=1 mvn -Dtest=Exercise06Test test
 *
 *      Record `cov` and `exec/s`.
 *
 *   2. Narrow the scope to only your own code, then re-run:
 *
 *        JAZZER_FUZZ=1 mvn -Dtest=Exercise06Test \
 *            -Djazzer.instrumentation_includes='de.unibonn.fuzzing.**' \
 *            test
 *
 *      Record `cov` and `exec/s` again.
 *
 *   3. Compare. Which went up? Which went down? When would you
 *      want each configuration?
 *
 * Note: the instrumentation_includes flag takes a colon-separated
 * list of glob patterns. For the realistic case where you DO want
 * the target library instrumented but NOT its logging and metrics
 * dependencies, you might pass something like:
 *
 *     de.unibonn.fuzzing.**:com.solace.**
 *
 * Try that as a third data point if you have time.
 */
class Exercise06Test {

    @FuzzTest(maxDuration = "1m")
    void fuzzQueueNameResolver(FuzzedDataProvider data) {
        String groupName = data.consumeString(20);
        String expression = data.consumeRemainingAsString();
        try {
            QueueNameResolver.resolve(groupName, expression);
        } catch (ExpressionException expected) {
            // SpEL parse/eval errors are not findings for us here.
        } catch (ClassCastException expected) {
            // Expressions that return non-String values cause this
            // via the toString() path; not our target bug.
        }
    }
}
