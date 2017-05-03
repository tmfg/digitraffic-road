package fi.livi.digitraffic.tie.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitorAspect.class);

    private HashMap<String, Double> times = new HashMap<>();
    private HashMap<String, Integer> count = new HashMap<>();

    public static final int DEFAULT_WARNING_LIMIT = 5000;
    public static final int DEFAULT_INFO_LIMIT = 5000;

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object monitor(ProceedingJoinPoint pjp) throws Throwable {
        final Class<?> className = pjp.getSignature().getDeclaringType();
        final String invocationName = pjp.getSignature().getName();
        final MethodSignature methodSignature = (MethodSignature)pjp.getSignature();
        final Method method = methodSignature.getMethod();
        final PerformanceMonitor monitorAnnotation = method.getAnnotation(PerformanceMonitor.class);
        final int warningLimit = monitorAnnotation != null ? monitorAnnotation.maxWarnExcecutionTime() : DEFAULT_WARNING_LIMIT;
        final int infoLimit = monitorAnnotation != null ? monitorAnnotation.maxInfoExcecutionTime() : DEFAULT_INFO_LIMIT;

        final Object[] args = pjp.getArgs();
        final Log currentLogger = LogFactory.getLog(className);

        final StopWatch stopWatch = StopWatch.createStarted();

        try {
            return pjp.proceed();
        } finally {
            stopWatch.stop();
            final double executionTime = stopWatch.getTime();
            updateTimes(className + "." + invocationName, executionTime, method);

            if (executionTime > warningLimit &&
                currentLogger.isWarnEnabled()) {
                String message =
                    buildMessage(
                        invocationName,
                        args,
                        executionTime);
                currentLogger.warn(message);
            } else if (executionTime > infoLimit &&
                       currentLogger.isInfoEnabled()) {
                String message =
                    buildMessage(
                        invocationName,
                        args,
                        executionTime);
                currentLogger.info(message);
            }
        }
    }

    private String buildMessage(final String invocationName,
                                final Object[] args,
                                final double executionTime) {
        final double executionTimeSeconds = executionTime/1000.0;
        StringBuilder builder = new StringBuilder();
        builder
            .append("PerformanceMonitor: Method ")
            .append(invocationName);
        if (args != null && args.length > 0) {
            builder.append(" with arguments: ");
            buildValueToString(builder, args);
        }
        builder.append(". Invocation time was: ")
               .append(executionTimeSeconds)
               .append(" s");
        return truncate(builder.toString());
    }

    private String truncate(String original) {
        if (original.length() > 1000) {
            return original.substring(0, 1000);
        } else {
            return original;
        }
    }

    private void buildValueToString(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
            return;
        }
        
        if (value.getClass().isArray()) {
            try {
                Object[] objects = (Object[]) value;
                buildArrayToString(builder, objects);
            } catch (ClassCastException e) {
                builder.append("?");
            }
        } else if (value instanceof Collection<?>) {
            Collection<?> values = (Collection<?>) value;
            Object[] objects = values.toArray(new Object[values.size()]);
            buildArrayToString(builder, objects);
        } else {
            builder.append(value.toString());
        }
    }

    private void buildArrayToString(StringBuilder builder, Object[] objects) {
        builder.append("[");
        for (int index = 0;
            index < objects.length && index < 5;
            index++) {
            if(index > 0) {
                builder.append(", ");
            }
            buildValueToString(builder, objects[index]);
        }
        if (objects.length > 5) {
            builder.append(",...");
        }
        builder.append("]");
    }


    private static final String NO_SUM = "_NO_SUM";
    private synchronized void updateTimes(String key, double executionTime, Method method) {
        if (method.getAnnotation(NoPerformanceSum.class) != null) {
            key = key + NO_SUM;
        }
        Double prevSum = times.get(key);
        Integer prevCount = count.get(key);
        times.put(key, executionTime + (prevSum != null ? prevSum : 0));
        count.put(key, 1 + (prevCount != null ? prevCount : 0));
    }

    synchronized public void logRequestExecutionTimesAndResetCounters() {
        if (times.size() > 0) {

            StringBuilder sb = new StringBuilder("Request execution statistics:");
            double executionTimesSum = 0;
            // Järjestetään data suoritusajan mukaan
            ValueComparator bvc =  new ValueComparator(times);
            TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(bvc);
            sortedMap.putAll(times);
            for (String s : sortedMap.keySet()) {
                boolean noSum = s.endsWith(NO_SUM);
                sb.append("\n" + s.replace(NO_SUM, "") + " count: " + count.get(s) + " total: " + times.get(s) + " s" + (noSum ? " (not included in total)":""));
                if (!noSum) {
                    executionTimesSum = executionTimesSum + times.get(s);
                }
            }
            if (executionTimesSum > DEFAULT_WARNING_LIMIT &&
                log.isWarnEnabled()) {
                log.warn(sb.toString() +
                         "\nTotal execution time inside request {}", executionTimesSum);
            } else if (executionTimesSum > DEFAULT_INFO_LIMIT &&
                       log.isInfoEnabled()) {
                log.info(sb.toString() +
                         "\nTotal execution time inside request {} ", executionTimesSum);
            }

            times.clear();
            count.clear();
        }
    }

    /**
     * Ei lasketa suoritusaikaa kokonais summaan PerformanceMonitorAspect:ssa.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface NoPerformanceSum {
        // empty
    }

    /**
     * Ei lasketa suoritusaikaa kokonais summaan PerformanceMonitorAspect:ssa.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface PerformanceMonitor {
        /*
         * maxWarnExcecutionTime in seconds without warning message
         */
        int maxWarnExcecutionTime() default 60000;
        /*
         * maxInfoExcecutionTime in seconds without info message
         */
        int maxInfoExcecutionTime() default 5000;
    }

    private class ValueComparator implements Comparator<String> {

        Map<String, Double> base;
        public ValueComparator(Map<String, Double> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
