package fi.livi.digitraffic.tie.conf;

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

        final HandlerMethod hm;

        try {
            hm = (HandlerMethod) handler;
            if (hm.getMethod().isAnnotationPresent(Deprecated.class)) {
                response.addHeader("Deprecation", hm.getMethod().getAnnotation(Deprecated.class).since());
            }
            if (hm.getMethod().isAnnotationPresent(Sunset.class)) {
                response.addHeader("Sunset", hm.getMethod().getAnnotation(Sunset.class).date());
            }
        } catch (final ClassCastException error) {
            log.error(error.getMessage());
        }

    }

}
