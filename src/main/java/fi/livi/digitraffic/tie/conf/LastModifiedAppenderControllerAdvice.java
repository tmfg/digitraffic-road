package fi.livi.digitraffic.tie.conf;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import fi.livi.digitraffic.tie.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;

/**
 * Appends Last-Modified -header to response if supported by returned object
 */
@ControllerAdvice
public class LastModifiedAppenderControllerAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(LastModifiedAppenderControllerAdvice.class);
    public static final String LAST_MODIFIED_HEADER = "Last-Modified";

    @Override
    public boolean supports(final MethodParameter returnType, final Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(final Object body, final MethodParameter returnType, final MediaType selectedContentType,
                                  final Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  final ServerHttpRequest request, final ServerHttpResponse response) {
        if (body instanceof LastModifiedSupport) {
            final Instant lastModified = ((LastModifiedSupport) body).getLastModified();
            if (lastModified != null) {
                response.getHeaders().add(LAST_MODIFIED_HEADER, DateHelper.getInLastModifiedHeaderFormat(lastModified));
            } else {
                log.error("Entity implementing LastModifiedSupport.getLastModified() should return non null value. Null value for object: " + ToStringHelper.toStringFull(response));
            }
        }

        return body;
    }
}
