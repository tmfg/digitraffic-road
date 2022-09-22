package fi.livi.digitraffic.tie.controller;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseEntityWithLastModifiedHeader<T> extends ResponseEntity<T> {

    private ResponseEntityWithLastModifiedHeader(final T object, final Instant lastModified) {
        super(object, createHeaders(lastModified), HttpStatus.OK);
    }

    private static HttpHeaders createHeaders(final Instant lastModified) {
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLastModified(lastModified);
        return responseHeaders;
    }

    public static <T> ResponseEntityWithLastModifiedHeader<T> of(final T object, final Instant lastModified) {
        return new ResponseEntityWithLastModifiedHeader<>(object, lastModified);
    }
}
