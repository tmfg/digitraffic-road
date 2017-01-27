package fi.livi.digitraffic.tie.data.controller.exception.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/error")
@ConditionalOnProperty(value = "spring.main.web_environment", matchIfMissing = true)
public class SimpleErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    private final static boolean includeStackTrace = false;

    @Autowired
    public SimpleErrorController(final ErrorAttributes errorAttributes) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping
    public Map<String, Object> error(final HttpServletRequest aRequest) {
        return getErrorAttributes(aRequest);
    }

    private Map<String, Object> getErrorAttributes(final HttpServletRequest aRequest) {
        final RequestAttributes requestAttributes = new ServletRequestAttributes(aRequest);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }
}