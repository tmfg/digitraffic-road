package fi.livi.digitraffic.tie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking the sunset date of a deprecated API.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD})
public @interface Sunset {
    /**
     * The earliest date at which the deprecated API may be taken down. Should be in format YYYY-MM-DD.
     *
     * @return sunset date as string
     */
    String date() default "";

    /**
     * If true, sunset date is still to be determined.
     *
     * @return boolean value indicating if sunset date is TBD
     */
    boolean tbd() default false;
}