package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.helper.DateHelper.isoToHttpDate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import fi.livi.digitraffic.tie.annotation.Sunset;

public class DeprecationInterceptor implements HandlerInterceptor {

    private final static Logger log = LoggerFactory.getLogger(DeprecationInterceptor.class);

    @Override
    public void postHandle(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Object handler,
        final ModelAndView modelAndView) throws Exception {

        final HandlerMethod handlerMethod;

        try {
            handlerMethod = (HandlerMethod) handler;

            if (handlerMethod.getMethod().isAnnotationPresent(Deprecated.class)
                || handlerMethod.getMethod().isAnnotationPresent(Sunset.class)) {
                if (handlerMethod.getMethod().isAnnotationPresent(Deprecated.class)
                    && handlerMethod.getMethod().isAnnotationPresent(Sunset.class)) {
                    String sunsetHttpDate = isoToHttpDate(handlerMethod.getMethod().getAnnotation(Sunset.class).date());
                    response.addHeader("Deprecation", "true");
                    response.addHeader("Sunset", sunsetHttpDate);
                }
                else {
                    throw new Exception("Deprecated handler " + handlerMethod.getMethod().getName() + " is missing either a @Deprecated or @Sunset annotation");
                }
            }
        } catch (final Exception error) {
            log.error(error.getMessage());
        }

    }

}
