package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.helper.DateHelper.isoLocalDateToHttpDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import fi.livi.digitraffic.tie.annotation.Sunset;
import fi.livi.digitraffic.tie.controller.ApiDeprecations;

public class DeprecationInterceptor implements HandlerInterceptor {

    private final static Logger log = LoggerFactory.getLogger(DeprecationInterceptor.class);

    @Override
    public boolean preHandle(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Object handler) {

        try {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;

            if (handlerMethod.getMethod().isAnnotationPresent(Deprecated.class)
                || handlerMethod.getMethod().isAnnotationPresent(Sunset.class)) {

                if (handlerMethod.getMethod().isAnnotationPresent(Deprecated.class)
                    && handlerMethod.getMethod().isAnnotationPresent(Sunset.class)) {

                    final String sunsetHeaderContent = handlerMethod.getMethod().getAnnotation(Sunset.class).tbd() ?
                                                       ApiDeprecations.SUNSET_FUTURE :
                                                       isoLocalDateToHttpDateTime(handlerMethod.getMethod().getAnnotation(Sunset.class).date());

                    response.addHeader("Deprecation", "true");
                    response.addHeader("Sunset", sunsetHeaderContent);

                } else {
                    log.error("Deprecated handler {} is missing either a @Deprecated or @Sunset annotation", handlerMethod.getMethod().getName());
                }
            }
        } catch (final ClassCastException error) {
            log.error(error.getMessage());
        }
        return true;
    }

}
