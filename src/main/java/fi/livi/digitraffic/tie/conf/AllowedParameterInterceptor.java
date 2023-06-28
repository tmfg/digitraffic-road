package fi.livi.digitraffic.tie.conf;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Throws IllegalArgumentException if request parameters contains parameter names
 * that are not allowed parameter names for handler method.
 */
public class AllowedParameterInterceptor implements HandlerInterceptor {

    // Dont check for old apis pattern /api/v{n}/some-text
    private final static Pattern OLD_API_PATTERN = Pattern.compile(".*/api/(beta|v\\d*)/.*");

    @Override
    public boolean preHandle(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Object handler) {

        if (handler instanceof HandlerMethod) {
            // Check only new api patterns
            if (OLD_API_PATTERN.matcher(request.getRequestURI()).matches()) {
                return true;
            }

            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            // Collect all possible parameters defined in controller method
            final Set<String> allowedParams =
                Arrays.stream(handlerMethod.getMethodParameters()).map(p -> p.getParameter().getName()).collect(Collectors.toSet());

            request.getParameterNames().asIterator().forEachRemaining(p -> {
                if (!allowedParams.contains(p)) {
                    throw new IllegalArgumentException("Illegal query parameter name: " + p + " for url: " + request.getRequestURI());
                }
            });
        }
        return true;
    }

}
