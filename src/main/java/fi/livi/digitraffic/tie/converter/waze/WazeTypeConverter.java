package fi.livi.digitraffic.tie.converter.waze;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Restriction;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadWorkPhase;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto.WazeType.*;

@ConditionalOnWebApplication
@Component
public class WazeTypeConverter {
    public Optional<WazeFeedIncidentDto.WazeType> convertToWazeType(final WazeDatex2FeatureDto dto, final Instant now) {
        final TrafficAnnouncementProperties properties = dto.feature.getProperties();

        if (properties.getSituationType() == SituationType.TRAFFIC_ANNOUNCEMENT && properties.getTrafficAnnouncementType() == null) {
            return Optional.empty();
        }

        if (isRoadClosed(dto.feature, now)) {
            if (properties.getSituationType() == SituationType.TRAFFIC_ANNOUNCEMENT) {
                return Optional.of(ROAD_CLOSED_HAZARD);
            } else {
                return Optional.of(ROAD_CLOSED_CONSTRUCTION);
            }
        }

        final WazeFeedIncidentDto.WazeType commonWazeType = getCommonWazeTypeFromDatex2(dto.datex2Message);
        if (commonWazeType != null) {
            return Optional.of(commonWazeType);
        }

        if (properties.getSituationType() == SituationType.TRAFFIC_ANNOUNCEMENT) {
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
            return Optional.empty();
        }

        throw new IllegalArgumentException("Could not figure out type for id " + dto.feature.getProperties().situationId);
    }

    /**
     * Tells whether the announcement has a road closure that is still relevant. Uses the same filters
     * as the phase level conversion in {@code WazeDatex2JsonConverter}, and must keep doing so: if this
     * said the road is closed but no phase qualified, the whole road work would be published as a
     * closure. The time is passed in for the same reason, see
     * {@link WazeAnnouncementDurationConverter#hasNotEnded}.
     */
    public static boolean isRoadClosed(final TrafficAnnouncementFeature feature, final Instant now) {
        return !feature.getProperties().announcements.isEmpty() && feature.getProperties().announcements.getFirst().roadWorkPhases.stream()
                .filter(WazeTypeConverter::hasRoadClosedRestriction)
                .anyMatch(phase -> WazeAnnouncementDurationConverter.hasNotEnded(phase, now));
    }

    private static final Set<Restriction.Type> INCLUDED_TYPES = EnumSet.of(Restriction.Type.ROAD_CLOSED);

    public static boolean hasRoadClosedRestriction(final RoadWorkPhase phase) {
        return phase.restrictions.stream().anyMatch(r -> INCLUDED_TYPES.contains(r.type));
    }

    private static final List<Pair<String, WazeFeedIncidentDto.WazeType>> typeMapping = List.of(
        Pair.of("obstructionType>hazardsOnTheRoad",                    HAZARD_ON_ROAD),
        Pair.of("obstructionType>objectOnTheRoad",                     HAZARD_ON_ROAD_OBJECT),
        Pair.of("roadOrCarriagewayOrLaneManagementType>laneClosures",  HAZARD_ON_ROAD_LANE_CLOSED),
        Pair.of("animalPresenceType>animalsOnTheRoad",                 HAZARD_ON_SHOULDER_ANIMALS),
        Pair.of("animalPresenceType>largeAnimalsOnTheRoad",            HAZARD_ON_SHOULDER_ANIMALS),
        Pair.of("animalPresenceType>herdOfAnimalsOnTheRoad",           HAZARD_ON_SHOULDER_ANIMALS),
        Pair.of("poorEnvironmentType>badWeather",                      HAZARD_WEATHER),
        Pair.of("nonWeatherRelatedRoadConditionType>oilOnRoad",        HAZARD_ON_ROAD_OIL),
        Pair.of("nonWeatherRelatedRoadConditionType>roadSurfaceInPoorCondition", HAZARD_ON_ROAD_POT_HOLE),
        Pair.of("weatherRelatedRoadConditionType>ice",                 HAZARD_ON_ROAD_ICE),
        Pair.of("weatherRelatedRoadConditionType>freezingRain",        HAZARD_WEATHER_FREEZING_RAIN),
        Pair.of("weatherRelatedRoadConditionType>heavyRain",           HAZARD_WEATHER_HEAVY_RAIN),
        Pair.of("weatherRelatedRoadConditionType>damagingHail",        HAZARD_WEATHER_HAIL),
        Pair.of("weatherRelatedRoadConditionType>blizzard",            HAZARD_WEATHER_HEAVY_SNOW),
        Pair.of("poorEnvironmentType>heavySnowfall",                   HAZARD_WEATHER_HEAVY_SNOW),
        Pair.of("environmentalObstructionType>flooding",               HAZARD_WEATHER_FLOOD),
        Pair.of("environmentalObstructionType>denseFog",               HAZARD_WEATHER_FOG),
        Pair.of("environmentalObstructionType>fog",                    HAZARD_WEATHER_FOG),
        Pair.of("abnormalTrafficType>stationaryTraffic",               JAM_STAND_STILL_TRAFFIC),
        Pair.of("abnormalTrafficType>heavyTraffic",                    JAM_HEAVY_TRAFFIC),
        Pair.of("abnormalTrafficType>queuingTraffic",                  JAM_MODERATE_TRAFFIC)
        );

    private WazeFeedIncidentDto.WazeType getCommonWazeTypeFromDatex2(final String datex2Message) {
        return typeMapping.stream()
            .filter(e -> containsTagValue(datex2Message, e.getKey()))
            .findFirst()
            .map(Pair::getValue)
            .orElse(null);
    }

    private boolean containsTagValue(final String xml, final String tagAndValue) {
        return xml.contains("<" + tagAndValue) || xml.contains("<sit:" + tagAndValue);
    }
}
