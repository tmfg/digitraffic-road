package fi.livi.digitraffic.tie.dto.wazefeed;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WazeFeedIncidentDto implements Serializable {
    public static final String reference = "FINTRAFFIC";

    public final String id;
    public final String description;
    public final String type;
    public final String subtype;
    public final WazeFeedLocationDto location;
    public final String starttime;
    public final String endtime;

    public WazeFeedIncidentDto(final String id, final String street, final String description, final WazeFeedLocationDto.Direction direction,
                               final String polyline, final WazeType type, final String starttime, final String endtime) {
        this.id = id;
        this.location = new WazeFeedLocationDto(street, polyline, direction);
        this.description = description;
        this.type = type == null ? null : type.type.name();
        this.subtype = type == null ? null : type.getSubtype();
        this.starttime = starttime;
        this.endtime = endtime;
    }     

    public enum Type {
        // Defined types currently in use in our Waze integration. More can be added as needed
        // See: https://developers.google.com/waze/data-feed/incident-information
        HAZARD,
        ACCIDENT,
        ROAD_CLOSED,
        JAM,
        POLICE
    }

    public enum WazeType {
        // Defined subtypes currently in use in our Waze integration.
        // See: https://developers.google.com/waze/data-feed/cifs-specification#incident-subtypes

        ACCIDENT_NONE(Type.ACCIDENT),

        HAZARD_NONE(Type.HAZARD),
        HAZARD_ON_ROAD(Type.HAZARD),
        HAZARD_ON_ROAD_CONSTRUCTION(Type.HAZARD),
        HAZARD_ON_ROAD_ICE(Type.HAZARD),
        HAZARD_ON_ROAD_LANE_CLOSED(Type.HAZARD),
        HAZARD_ON_ROAD_OBJECT(Type.HAZARD),
        HAZARD_ON_ROAD_OIL(Type.HAZARD),
        HAZARD_ON_ROAD_POT_HOLE(Type.HAZARD),
        HAZARD_ON_ROAD_TRAFFIC_LIGHT_FAULT(Type.HAZARD),
        HAZARD_ON_SHOULDER_ANIMALS(Type.HAZARD),
        HAZARD_WEATHER(Type.HAZARD),
        HAZARD_WEATHER_FLOOD(Type.HAZARD),
        HAZARD_WEATHER_FOG(Type.HAZARD),
        HAZARD_WEATHER_FREEZING_RAIN(Type.HAZARD),
        HAZARD_WEATHER_HAIL(Type.HAZARD),
        HAZARD_WEATHER_HEAVY_RAIN(Type.HAZARD),
        HAZARD_WEATHER_HEAVY_SNOW(Type.HAZARD),

        ROAD_CLOSED_NONE(Type.ROAD_CLOSED),
        ROAD_CLOSED_HAZARD(Type.ROAD_CLOSED),
        ROAD_CLOSED_CONSTRUCTION(Type.ROAD_CLOSED),

        JAM_NONE(Type.JAM),
        JAM_MODERATE_TRAFFIC(Type.JAM),
        JAM_HEAVY_TRAFFIC(Type.JAM),
        JAM_STAND_STILL_TRAFFIC(Type.JAM),
        ;

        public final Type type;
        WazeType(final Type type) {
            this.type = type;
        }

        public String getSubtype() {
            if(name().endsWith("_NONE")) {
                return null;
            }

            return name();
        }
    }
}