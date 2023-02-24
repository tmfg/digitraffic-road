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

    /**
     * Some times last modified time is not relevant i.e. if there is no data, then value is not required.
     * @return true if data should have last modified value.
     */
    @JsonIgnore
    default boolean shouldContainLastModified() {
        return true;
    }
}