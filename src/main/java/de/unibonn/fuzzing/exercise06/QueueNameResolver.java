package de.unibonn.fuzzing.exercise06;

import com.solace.spring.cloud.stream.binder.properties.SolaceConsumerProperties;
import com.solace.spring.cloud.stream.binder.provisioning.SolaceProvisioningUtil;
import com.solace.spring.cloud.stream.binder.provisioning.SolaceProvisioningUtil.QueueNames;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;

/**
 * Exercise 06: Resolve consumer queue names via the SBB Solace binder.
 *
 * This calls SolaceProvisioningUtil.getQueueNames(), which internally
 * evaluates two SpEL expressions (queueNameExpression and
 * errorQueueNameExpression) and validates the resulting queue names
 * against Solace naming rules.
 *
 * The interesting property for Exercise 06: almost all the work
 * happens inside com.solace.** and org.springframework.expression.**,
 * not in this class. With default instrumentation Jazzer tracks every
 * branch inside those libraries; with
 *   -Djazzer.instrumentation_includes='de.unibonn.fuzzing.**'
 * only the ~5 branches in this class are tracked.
 *
 * That contrast makes cov and exec/s move in opposite directions,
 * which is the point of the exercise.
 */
public final class QueueNameResolver {

    private QueueNameResolver() {}

    /**
     * Resolve the consumer queue names for a given destination and group,
     * honouring optional SpEL expressions for the queue and error-queue names.
     *
     * @param destination         the Spring Cloud Stream destination name
     * @param groupName           the consumer group name
     * @param queueNameExpression optional SpEL expression for the queue name
     * @param errorQueueExpr      optional SpEL expression for the error-queue name
     * @param isAnonymous         true to request an anonymous (non-durable) queue
     */
    public static QueueNames resolve(
            String destination,
            String groupName,
            String queueNameExpression,
            String errorQueueExpr,
            boolean isAnonymous) {

        SolaceConsumerProperties consumerProps = new SolaceConsumerProperties();
        if (queueNameExpression != null && !queueNameExpression.isEmpty()) {
            consumerProps.setQueueNameExpression(queueNameExpression);
        }
        if (errorQueueExpr != null && !errorQueueExpr.isEmpty()) {
            consumerProps.setErrorQueueNameExpression(errorQueueExpr);
        }

        ExtendedConsumerProperties<SolaceConsumerProperties> extProps =
                new ExtendedConsumerProperties<>(consumerProps);

        return SolaceProvisioningUtil.getQueueNames(
                destination, groupName, extProps, isAnonymous);
    }
}
