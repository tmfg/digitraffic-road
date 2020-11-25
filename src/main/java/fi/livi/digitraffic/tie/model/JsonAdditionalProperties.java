package fi.livi.digitraffic.tie.model;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.helper.LoggerHelper;

/**
 * Implements mapping and logging for Json additional properties.
 *
 * The additionalProperties keyword is used to control the handling of extra stuff,
 * that is, properties whose names are not listed in the properties keyword.
 *
 */
public abstract class JsonAdditionalProperties {

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnySetter
    public void setAdditionalProperty(final String property, final Object value) {
        this.additionalProperties.put(property, value);
        // Log missing properties to notify
        getLogger(getClass()).warn("method=setAdditionalProperty No property found with the name: {} and value: {}",
                                   property, LoggerHelper.objectToStringLoggerSafe(value));
    }
}
