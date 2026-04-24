package fuzzing.exercise06;

import com.solace.spring.cloud.stream.binder.provisioning.SolaceProvisioningUtil;
import com.solace.spring.cloud.stream.binder.util.QualityOfService;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Exercise 06: Resolve a queue name from a group name and a SpEL
 * expression.
 *
 * This mirrors the pattern used inside
 * com.solace.spring.cloud.stream.binder.provisioning
 * .SolaceProvisioningUtil#resolveQueueNameExpression (which is
 * private in the SBB binder). We call the SBB binder's public
 * helper SolaceProvisioningUtil.isAnonEndpoint() to decide whether
 * to skip expression evaluation, and otherwise evaluate the
 * expression via Spring's SpelExpressionParser.
 *
 * The interesting property for Exercise 06: most of the work done
 * here happens inside third-party libraries (com.solace.**,
 * org.springframework.expression.**), not in this class. That
 * makes this method a good target to observe how
 * --instrumentation_includes (jazzer.instrumentation_includes)
 * trades coverage against throughput.
 */
public final class QueueNameResolver {

    private QueueNameResolver() {
        // utility class
    }

    public static String resolve(String groupName, String expression) {
        if (SolaceProvisioningUtil.isAnonEndpoint(groupName, QualityOfService.AT_LEAST_ONCE)) {
            return "anon";
        }
        if (expression == null || expression.isEmpty()) {
            return groupName;
        }
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext ctx = new StandardEvaluationContext(groupName);
        Expression exp = parser.parseExpression(expression);
        Object result = exp.getValue(ctx);
        return result != null ? result.toString() : null;
    }
}
