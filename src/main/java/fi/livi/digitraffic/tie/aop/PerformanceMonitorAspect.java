package fi.livi.digitraffic.tie.aop;

import java.lang.reflect.Method;
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

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PerformanceMonitorAspect {

    private static final Logger log = LoggerFactory.getLogger("PerformanceMonitor");
    public static final int DEFAULT_ERROR_LIMIT = 60000;
    public static final int DEFAULT_WARNING_LIMIT = 5000;
    public static final int DEFAULT_INFO_LIMIT = 1000;

    /**
     * By default every method in class with @Service annotation is monitored.
     * In addition any method can be monitored with @PerformanceMonitor annotation.
     * @PerformanceMonitor annotation also has properties to adjust monitoring settings.
     *
     * @Around("@annotation(org.springframework.transaction.annotation.Transactional)") -> @Transactional annotated methods.
     * @Around("execution(* fi.livi.digitraffic.tie..*Service.*(..))") -> Every class which name ends to Service.
     * @Around("within(@org.springframework.stereotype.Service *)") -> Every class which has @Service annotation.
     *
     */
    @Around("within(@org.springframework.stereotype.Service *) || @annotation(fi.livi.digitraffic.tie.annotation.PerformanceMonitor)")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {

        final MethodSignature methodSignature = (MethodSignature) pjp.getSignature();

        final StopWatch stopWatch = StopWatch.createStarted();

        if (log.isDebugEnabled()) {
            log.debug("monitor {}#{}", methodSignature.getDeclaringType().getName(), methodSignature.getName());
        }

        try {
            return pjp.proceed();
        } finally {
            stopWatch.stop();
            monitor(pjp, methodSignature, stopWatch.getTime());
        }
    }

    private void monitor(final ProceedingJoinPoint pjp, final MethodSignature methodSignature, final long executionTime) {
        final Method method = methodSignature.getMethod();
        final PerformanceMonitor monitorAnnotation = method.getAnnotation(PerformanceMonitor.class);
        final boolean monitor = getMonitor(monitorAnnotation);

        if (monitor) {
            final int errorLimit = getErrorLimit(monitorAnnotation);
            final int warningLimit = getWarningLimit(monitorAnnotation);
            final int infoLimit = getInfoLimit(monitorAnnotation);
            final Object[] args = pjp.getArgs();
            final String methodWithClass = getMethodWithClass(methodSignature);

            if (executionTime > errorLimit && log.isErrorEnabled()) {
                log.error(buildMessage(methodWithClass, args, executionTime));
            } else if (executionTime > warningLimit && log.isWarnEnabled()) {
                log.warn(buildMessage(methodWithClass, args, executionTime));
            } else if (executionTime > infoLimit && log.isInfoEnabled()) {
                log.info(buildMessage(methodWithClass, args, executionTime));
            }
        }
    }

    private int getInfoLimit(final PerformanceMonitor monitorAnnotation) {
        return monitorAnnotation != null ? monitorAnnotation.maxInfoExcecutionTime() : DEFAULT_INFO_LIMIT;
    }

    private int getWarningLimit(final PerformanceMonitor monitorAnnotation) {
        return monitorAnnotation != null ? monitorAnnotation.maxWarnExcecutionTime() : DEFAULT_WARNING_LIMIT;
    }

    private int getErrorLimit(final PerformanceMonitor monitorAnnotation) {
        return monitorAnnotation != null ? monitorAnnotation.maxErrorExcecutionTime() : DEFAULT_ERROR_LIMIT;
    }

    private boolean getMonitor(final PerformanceMonitor monitorAnnotation) {
        return monitorAnnotation == null || monitorAnnotation.monitor();
    }

    private static String getMethodWithClass(final MethodSignature methodSignature) {
        return methodSignature.getDeclaringType().getSimpleName() + "." + methodSignature.getName();
    }

    private String buildMessage(final String invocationName,
                                final Object[] args,
                                final long executionTimeMs) {
        final StringBuilder builder = new StringBuilder(100)
            .append("method=").append(invocationName)
            .append(" tookMs=").append(executionTimeMs);

        if (args != null && args.length > 0) {
            builder.append(" arguments=");
            buildValueToString(builder, args);
        }

        return StringUtils.truncate(builder.toString(), 1000);
    }

    public static void buildValueToString(final StringBuilder builder, final Object value) {
        if (value == null) {
            builder.append("null");
            return;
        }

        if (value.getClass().isArray()) {
            try {
                final Object[] objects = (Object[]) value;
                buildArrayToString(builder, objects);
            } catch (final ClassCastException e) {
                log.debug("buildArrayToString error", e);
                builder.append("[").append(value.toString().replace(' ', '_')).append("]");
            }
        } else if (value instanceof Collection<?>) {
            final Collection<?> values = (Collection<?>) value;
            final Object[] objects = values.toArray(new Object[0]);
            buildArrayToString(builder, objects);
        } else {
            builder.append(value);
        }
    }

    private static void buildArrayToString(final StringBuilder builder, final Object[] objects) {
        builder.append("[");
        for (int index = 0;
            index < objects.length && index < 5;
            index++) {
            if(index > 0) {
                builder.append(";");
            }
            buildValueToString(builder, objects[index]);
        }
        builder.append("]");
    }

}
