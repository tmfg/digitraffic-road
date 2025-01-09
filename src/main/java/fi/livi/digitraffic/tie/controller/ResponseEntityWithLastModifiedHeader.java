package fi.livi.digitraffic.tie.controller;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Used as controller method return value to add LastModified header to responses.
 * Usefull when there is no root object that contains last modified information i.e. with plain array of data
 * and @{{@link fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice} cannot be used.
 */
public class ResponseEntityWithLastModifiedHeader<T> extends ResponseEntity<T> {
    private static final Logger log = LoggerFactory.getLogger(ResponseEntityWithLastModifiedHeader.class);

    private ResponseEntityWithLastModifiedHeader(final T object, final Instant lastModified, final String apiUri) {
        super(object, createHeaders(lastModified, apiUri), HttpStatus.OK);
    }

    private static HttpHeaders createHeaders(final Instant lastModified, final String apiUri) {
        final HttpHeaders responseHeaders = new HttpHeaders();
        if (lastModified != null) {
            responseHeaders.setLastModified(lastModified);
        } else {
            log.error(
                    "method=createHeaders {} should set non null value for last-modified value. Null value for request uri: {}",
                    ResponseEntityWithLastModifiedHeader.class.getSimpleName(), apiUri);
        }
        return responseHeaders;
    }

    public static <T> ResponseEntityWithLastModifiedHeader<T> of(final T object, final Instant lastModified, final String apiUri) {
        return new ResponseEntityWithLastModifiedHeader<>(object, lastModified, apiUri);
    }
}
