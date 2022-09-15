package fi.livi.digitraffic.tie.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * If object returned by Controller implements this, LastModifiedAppenderControllerAdvice will call getLastModified()
 * from it and add it as "last-modified" -header value.
 */
public interface LastModifiedSupport {
    @JsonIgnore
    Instant getLastModified();
}