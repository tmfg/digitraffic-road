package fi.livi.digitraffic.tie.converter.waze;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.*;

@ConditionalOnWebApplication
@Component
public class WazeTypeConverter {
    public Optional<WazeFeedIncidentDto.WazeType> convertToWazeType(final WazeDatex2FeatureDto dto) {
        final TrafficAnnouncementProperties properties = dto.feature.getProperties();

        if (properties.getSituationType() == SituationType.TRAFFIC_ANNOUNCEMENT && properties.getTrafficAnnouncementType() == null) {
            return Optional.empty();
        }

        if (isRoadClosed(dto.datex2)) {
            if (properties.getSituationType() == SituationType.TRAFFIC_ANNOUNCEMENT) {
                return Optional.of(ROAD_CLOSED_HAZARD);
            } else {
                return Optional.of(ROAD_CLOSED_CONSTRUCTION);
            }
        }

        // Check datex2 for any common type for TrafficAnnouncement or MaintenanceWork
        final WazeFeedIncidentDto.WazeType commonWazeType = getCommonWazeTypeFromDatex2(dto.datex2);
        if (commonWazeType != null) {
            return Optional.of(commonWazeType);
        }

        if (properties.getSituationType() == SituationType.TRAFFIC_ANNOUNCEMENT) {
            // traffic announcement
            // old functionality
            switch (properties.getTrafficAnnouncementType()) {
                case ACCIDENT_REPORT:
                case PRELIMINARY_ACCIDENT_REPORT:
                    return Optional.of(ACCIDENT_NONE);
                case GENERAL:
                    return Optional.of(HAZARD_NONE);
                default:
                    return Optional.empty();
            }
        } else if (properties.getSituationType() == SituationType.ROAD_WORK) {
            // road works

            // default type for ROAD_WORK
            return Optional.empty();
        }

        throw new IllegalArgumentException("Could not figure out type for id " + dto.feature.getProperties().situationId);
    }

    /**
     * Check if any restriction is of type ROAD_CLOSED
     */
    private boolean isRoadClosed(final Datex2 d2) {
        return d2.getMessage().contains("<roadOrCarriagewayOrLaneManagementType>roadClosed");
    }

    // note, this is also a priority listing
    // if multiple mappings are present, the one higher in the list will be selected
    private static final List<Pair<String, WazeFeedIncidentDto.WazeType>> typeMapping = List.of(
        Pair.of("<obstructionType>hazardsOnTheRoad",                    HAZARD_ON_ROAD),
        Pair.of("<obstructionType>objectOnTheRoad",                     HAZARD_ON_ROAD_OBJECT),
        Pair.of("<roadOrCarriagewayOrLaneManagementType>laneClosures",  HAZARD_ON_ROAD_LANE_CLOSED),
        Pair.of("<animalPresenceType>animalsOnTheRoad",                 HAZARD_ON_SHOULDER_ANIMALS),
        Pair.of("<animalPresenceType>largeAnimalsOnTheRoad",            HAZARD_ON_SHOULDER_ANIMALS),
        Pair.of("<animalPresenceType>herdOfAnimalsOnTheRoad",           HAZARD_ON_SHOULDER_ANIMALS),

        Pair.of("<poorEnvironmentType>badWeather",                      HAZARD_WEATHER),

        Pair.of("<nonWeatherRelatedRoadConditionType>oilOnRoad",                    HAZARD_ON_ROAD_OIL),
        Pair.of("<nonWeatherRelatedRoadConditionType>roadSurfaceInPoorCondition",   HAZARD_ON_ROAD_POT_HOLE),
        Pair.of("<weatherRelatedRoadConditionType>ice",             HAZARD_ON_ROAD_ICE),
        Pair.of("<weatherRelatedRoadConditionType>freezingRain",    HAZARD_WEATHER_FREEZING_RAIN),
        Pair.of("<weatherRelatedRoadConditionType>heavyRain",       HAZARD_WEATHER_HEAVY_RAIN),
        Pair.of("<weatherRelatedRoadConditionType>damagingHail",    HAZARD_WEATHER_HAIL),
        Pair.of("<weatherRelatedRoadConditionType>blizzard",        HAZARD_WEATHER_HEAVY_SNOW),
        Pair.of("<poorEnvironmentType>heavySnowfall",               HAZARD_WEATHER_HEAVY_SNOW),

        Pair.of("<environmentalObstructionType>flooding",           HAZARD_WEATHER_FLOOD),
        Pair.of("<environmentalObstructionType>denseFog",           HAZARD_WEATHER_FOG),
        Pair.of("<environmentalObstructionType>fog",                HAZARD_WEATHER_FOG),

        Pair.of("<abnormalTrafficType>stationaryTraffic",           JAM_STAND_STILL_TRAFFIC),
        Pair.of("<abnormalTrafficType>heavyTraffic",                JAM_HEAVY_TRAFFIC),
        Pair.of("<abnormalTrafficType>queuingTraffic",              JAM_MODERATE_TRAFFIC)
        );

    private WazeFeedIncidentDto.WazeType getCommonWazeTypeFromDatex2(final Datex2 d2) {
        return typeMapping.stream()
            .filter(e -> d2.getMessage().contains(e.getKey()))
            .findFirst()
            .map(e -> e.getValue())
            .orElse(null);
    }
}