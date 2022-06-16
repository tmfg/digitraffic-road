
package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single phase in a larger road work", name = "Restriction_OldV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "restriction"
})
public class Restriction extends JsonAdditionalProperties {

    @Schema(description = "Type of the restriction.")
    public Type type;

    @Schema(description = "Feature describes characteristics and qualities of the situation.")
    @JsonPropertyDescription("Feature describes characteristics and qualities of the situation.")
    public Feature restriction;

    public Restriction() {
    }

    public Restriction(final Type type, final Feature restriction) {
        super();
        this.type = type;
        this.restriction = restriction;
    }

    public enum Type {

        SPEED_LIMIT("speed limit"),
        SPEED_LIMIT_LENGTH("speed limit length"),
        TRAFFIC_LIGHTS("traffic lights"),
        MULTIPLE_LANES_CLOSED("multiple lanes closed"),
        SINGLE_LANE_CLOSED("single lane closed"),
        SINGLE_CARRIAGEWAY_CLOSED("single carriageway closed"),
        ROAD_CLOSED("road closed"),
        SINGLE_ALTERNATE_LINE_TRAFFIC("single alternate line traffic"),
        CONTRA_FLOW_TRAFFIC("contra flow traffic"),
        INTERMITTENT_SHORT_TERM_STOPS("intermittent short term stops"),
        INTERMITTENT_SHORT_TERM_CLOSURE("intermittent short term closure"),
        INTERMITTENT_STOPS_AND_CLOSURE_EFFECTIVE("intermittent stops and closure effective"),
        NARROW_LANES("narrow lanes"),
        DETOUR("detour"),
        DETOUR_SIGNS("detour signs"),
        DETOUR_CURVES_STEEP("detour curves steep"),
        DETOUR_CURVES_GENTLE("detour curves gentle"),
        DETOUR_USING_ROADWAYS("detour using roadways"),
        DETOUR_SURFACE_PAVED("detour surface paved"),
        DETOUR_SURFACE_MILLED("detour surface milled"),
        DETOUR_SURFACE_GRAVEL("detour surface gravel"),
        DETOUR_LENGTH("detour length"),
        DETOUR_GROSS_WEIGHT_LIMIT("detour gross weight limit"),
        SLOW_MOVING_MAINTENANCE_VEHICLE("slow moving maintenance vehicle"),
        ESTIMATED_DELAY("estimated delay"),
        ESTIMATED_DELAY_DURING_RUSH_HOUR("estimated delay during rush hour"),
        NARROW_OR_CLOSED_PEDESTRIAN_AND_BICYLE_PATH("narrow or closed pedestrian and bicyle path"),
        VEHICLE_HEIGHT_LIMIT("vehicle height limit"),
        VEHICLE_WIDTH_LIMIT("vehicle width limit"),
        VEHICLE_LENGTH_LIMIT("vehicle length limit"),
        VEHICLE_GROSS_WEIGHT_LIMIT("vehicle gross weight limit"),
        ROAD_SURFACE_PAVED("road surface paved"),
        ROAD_SURFACE_MILLED("road surface milled"),
        ROAD_SURFACE_GRAVEL("road surface gravel"),
        OPEN_FIRE_HEATER_IN_USE("open fire heater in use");

        private final String value;
        private final static Map<String, Type> CONSTANTS = new HashMap<>();

        static {
            for (Type c: values()) {
                CONSTANTS.put(c.value, c);
                CONSTANTS.put(c.name(), c);
            }
        }

        Type(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static Type fromValue(final String value) {
            final Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

        @JsonValue
        public String value() {
            return this.name();
        }
    }

}
