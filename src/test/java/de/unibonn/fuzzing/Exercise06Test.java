package de.unibonn.fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import de.unibonn.fuzzing.exercise06.QueueNameResolver;

/**
 * Exercise 06 - Instrumentation scope.
 *
 * QueueNameResolver.resolve() calls SolaceProvisioningUtil.getQueueNames(),
 * which evaluates two SpEL expressions internally and validates the
 * resulting queue names against Solace naming rules. Almost all the
 * executed code lives inside com.solace.** and
 * org.springframework.expression.** — NOT in this package.
 *
 * That makes this a good target to observe how
 * jazzer.instrumentation_includes trades coverage signal against throughput.
 *
 * Your task:
 *
 *   1. Run with default instrumentation scope for 30 seconds:
 *
 *        JAZZER_FUZZ=1 mvn -Dtest=Exercise06Test test
 *
 *      Record cov and exec/s.
 *
 *   2. Widen to also instrument the Solace binder, then re-run:
 *
 *        Linux/macOS:
 *          JAZZER_FUZZ=1 mvn -Dtest=Exercise06Test \
 *              -Djazzer.instrumentation_includes='de.unibonn.fuzzing.**:com.solace.**' \
 *              test
 *
 *        Windows (PowerShell):
 *          $env:JAZZER_FUZZ=1; mvn -Dtest=Exercise06Test `
 *              "-Djazzer.instrumentation_includes=de.unibonn.fuzzing.**;com.solace.**" `
 *              test
 *
 *      Record cov and exec/s again.
 *
 *   3. Compare. Which went up? Which went down? When would you want
 *      each configuration?
 *
 * Bonus — widen further to also instrument Spring Expression:
 *
 *        Linux/macOS:
 *          -Djazzer.instrumentation_includes=
 *            'de.unibonn.fuzzing.**:com.solace.**:org.springframework.expression.**'
 *
 *        Windows (PowerShell):
 *          "-Djazzer.instrumentation_includes=
 *            de.unibonn.fuzzing.**;com.solace.**;org.springframework.expression.**"
 */
class Exercise06Test {

    @FuzzTest(maxDuration = "1m")
    void fuzzQueueNameResolver(FuzzedDataProvider data) {
        String destination       = data.consumeString(30);
        String groupName         = data.consumeString(20);
        boolean isAnonymous      = data.consumeBoolean();
        String queueNameExpr     = data.consumeString(50);
        String errorQueueExpr    = data.consumeRemainingAsString();
        try {
            QueueNameResolver.resolve(
                    destination, groupName, queueNameExpr, errorQueueExpr, isAnonymous);
        } catch (Throwable ignored) {
            // SpEL errors, Solace validation exceptions, etc. are not
            // findings here — keep the run alive for the timing comparison.
        }
    }
}
