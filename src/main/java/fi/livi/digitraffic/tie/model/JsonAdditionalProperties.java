package fi.livi.digitraffic.tie.model;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

/**
 * Implements mapping and logging for Json additional properties.
 */
public abstract class JsonAdditionalProperties {

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnySetter
    public void setAdditionalProperty(final String property, final Object value) {
        this.additionalProperties.put(property, value);
        getLogger(getClass()).warn("method=setAdditionalProperty not found property: {} with value: {}",
                                   property, ToStringHelper.padKeyValuePairsEqualitySignWithSpaces(value));
    }
}
