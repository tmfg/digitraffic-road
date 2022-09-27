package fi.livi.digitraffic.tie.conf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class DeprecationInterceptor implements HandlerInterceptor {

    private static Logger log = LoggerFactory.getLogger(DeprecationInterceptor.class);

    @Override
    public void postHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        ModelAndView modelAndView) throws Exception {

        HandlerMethod hm;

        try {
            hm = (HandlerMethod) handler;
            if (hm.getMethod().isAnnotationPresent(Deprecated.class)) {
                response.addHeader("Deprecation", hm.getMethod().getAnnotation(Deprecated.class).since());
                response.addHeader("Sunset", "2022-12-31");
            }
        } catch (ClassCastException error) {
            log.error(error.getMessage());
        }

    }

}
