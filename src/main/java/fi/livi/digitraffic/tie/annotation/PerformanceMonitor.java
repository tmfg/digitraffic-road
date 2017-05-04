package fi.livi.digitraffic.tie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure PerformanceMonitor limits.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})
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
