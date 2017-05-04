package fi.livi.digitraffic.tie.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PerformanceMonitorAspect {

    private static final Logger log = LoggerFactory.getLogger("PerformanceMonitor");
    public static final int DEFAULT_WARNING_LIMIT = 5000;
    public static final int DEFAULT_INFO_LIMIT = 5000;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.0");

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {


        final MethodSignature methodSignature = (MethodSignature)pjp.getSignature();
        final Method method = methodSignature.getMethod();
        final PerformanceMonitor monitorAnnotation = method.getAnnotation(PerformanceMonitor.class);
        final int warningLimit = monitorAnnotation != null ? monitorAnnotation.maxWarnExcecutionTime() : DEFAULT_WARNING_LIMIT;
        final int infoLimit = monitorAnnotation != null ? monitorAnnotation.maxInfoExcecutionTime() : DEFAULT_INFO_LIMIT;
        final boolean monitor = monitorAnnotation != null ? monitorAnnotation.monitor() : true;

        final StopWatch stopWatch = StopWatch.createStarted();

        try {
            return pjp.proceed();
        } finally {
            stopWatch.stop();
            final double executionTime = stopWatch.getTime();

            if (monitor) {
                final String methodWithClass = methodSignature.getDeclaringType().getName() + "#" + methodSignature.getName();
                final Object[] args = pjp.getArgs();

                if (executionTime > warningLimit &&
                    log.isWarnEnabled()) {
                    log.warn(buildMessage(methodWithClass, args, executionTime));
                } else if (executionTime > infoLimit &&
                    log.isInfoEnabled()) {
                    log.info(buildMessage(methodWithClass, args, executionTime));
                }
            }
        }
    }

    private String buildMessage(final String invocationName,
                                final Object[] args,
                                final double executionTime) {
        final double executionTimeSeconds = executionTime/1000.0;
        final StringBuilder builder = new StringBuilder();
        builder.append(invocationName);

        if (args != null && args.length > 0) {
            builder.append(" with arguments ");
            buildValueToString(builder, args);
        }

        builder.append(" invocation time was ")
               .append(decimalFormat.format(executionTimeSeconds))
               .append(" s");
        return StringUtils.truncate(builder.toString(), 1000);
    }

    private void buildValueToString(final StringBuilder builder, final Object value) {
        if (value == null) {
            builder.append("null");
            return;
        }
        
        if (value.getClass().isArray()) {
            try {
                final Object[] objects = (Object[]) value;
                buildArrayToString(builder, objects);
            } catch (ClassCastException e) {
                builder.append("?");
            }
        } else if (value instanceof Collection<?>) {
            final Collection<?> values = (Collection<?>) value;
            final Object[] objects = values.toArray(new Object[values.size()]);
            buildArrayToString(builder, objects);
        } else {
            builder.append(value.toString());
        }
    }

    private void buildArrayToString(final StringBuilder builder, final Object[] objects) {
        builder.append("[");
        for (int index = 0;
            index < objects.length && index < 5;
            index++) {
            if(index > 0) {
                builder.append(", ");
            }
            buildValueToString(builder, objects[index]);
        }
        builder.append("]");
    }

    /**
     * Annotation to configure PerformanceMonitor limits.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface PerformanceMonitor {
        /**
         * Limits when execution time is logged as warning
         *
         * @return maxWarnExcecutionTime in millis
         */
        int maxWarnExcecutionTime() default 5000;

        /**
         * Limits when execution time is logged as info
         *
         * @return maxInfoExcecutionTime in millis
         */
        int maxInfoExcecutionTime() default 1000;

        /**
         * Should transactional method be monitored
         * @return monitor
         */
        boolean monitor() default true;
    }
}
