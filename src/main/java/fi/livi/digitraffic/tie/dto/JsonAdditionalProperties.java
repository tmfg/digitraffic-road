package fi.livi.digitraffic.tie.dto;

import static com.google.common.collect.Sets.newHashSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Contact;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadWorkPhase;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.helper.LoggerHelper;



/**
 * Implements mapping and logging for Json additional properties.
 * The additionalProperties keyword is used to control the handling of extra stuff,
 * that is, properties whose names are not listed in the properties keyword.
 */
public abstract class JsonAdditionalProperties implements Serializable {

    private final Map<Class<?>, Set<String>> ignoreClassToPropertiesMap = Stream.of(
            new ClassWithProperties(RoadWorkPhase.class, newHashSet("features")),
            new ClassWithProperties(TrafficAnnouncementProperties.class, newHashSet("locationToDisplay")),
            new ClassWithProperties(Contact.class, newHashSet("fax")),
            new ClassWithProperties(TrafficAnnouncementFeature.class, newHashSet("bbox"))
    ).collect(Collectors.toMap(ClassWithProperties::getType, ClassWithProperties::getProperties));

    /**
     * This is called by jackson when converting serialized json to objects
     */
    @JsonAnySetter
    public void setAdditionalProperty(final String property, final Object value) {
        // Don's save the value, just log unmapped values
        if (isLoggable(property)) {
            // Log missing properties
            getLogger(getClass()).warn("method=setAdditionalProperty No property found with the name: {} and value: {}",
                property, LoggerHelper.objectToStringLoggerSafe(value));
        }
    }

    private boolean isLoggable(final String property) {
        final Set<String> set = ignoreClassToPropertiesMap.get(getClass());
        return set == null || !set.contains(property);
    }

    static class ClassWithProperties {
        private final Class<?> type;
        private final Set<String> properties;

        ClassWithProperties(final Class<?> type, final Set<String> properties) {
            this.type = type;
            this.properties = properties;
        }

        public Class<?> getType() {
            return type;
        }

        public Set<String> getProperties() {
            return properties;
        }
    }
}
