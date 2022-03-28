package fi.livi.digitraffic.tie.model;

import static com.google.common.collect.Sets.newHashSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import fi.livi.digitraffic.tie.helper.LoggerHelper;

/**
 * Implements mapping and logging for Json additional properties.
 *
 * The additionalProperties keyword is used to control the handling of extra stuff,
 * that is, properties whose names are not listed in the properties keyword.
 *
 */
public abstract class JsonAdditionalProperties implements Serializable {

    private final Map<Class<?>, Set<String>> ignoreClassToPropertiesMap = Stream.of(new Object[][] {
        { fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadWorkPhase.class,
          newHashSet("features") },
        { fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadWorkPhase.class,
          newHashSet("features") },
        { fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementProperties.class,
          newHashSet("situationType", "trafficAnnouncementType", "locationToDisplay") },
        { fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementProperties.class,
          newHashSet("locationToDisplay") },
        { fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties.class,
          newHashSet("locationToDisplay") },
        { fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncement.class,
          newHashSet("lastActiveItinerarySegment", "roadWorkPhases", "earlyClosing") },
        { fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Contact.class,
          newHashSet("fax") },
        { fi.livi.digitraffic.tie.dto.trafficmessage.v1.Contact.class,
          newHashSet("fax") },
        { fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature.class,
          newHashSet("bbox") },
        { fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature.class,
          newHashSet("bbox") },

    }).collect(Collectors.toMap(data -> (Class<?>)data[0], data -> (Set<String>)data[1]));

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
}
