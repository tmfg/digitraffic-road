
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Work type", name = "WorkTypeV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "description"
})
public class WorkType extends JsonAdditionalProperties {

    @Schema(description = "Worktype", required = true)
    @NotNull
    public WorkType.Type type;

    @Schema(description = "Description", required = true)
    @NotNull
    public String description;

    public WorkType() {
    }

    public WorkType(final Type type, final String description) {
        super();
        this.type = type;
        this.description = description;
    }

    public enum Type {

        BRIDGE("bridge"),
        JUNCTION("junction"),
        CRASH_BARRIER("crash barrier"),
        BURIED_CABLES("buried cables"),
        LIGHTING("lighting"),
        ROADSIDE_EQUIPMENT("roadside equipment"),
        MEASUREMENT_EQUIPMENT("measurement equipment"),
        LEVEL_CROSSING("level crossing"),
        BLASTING_WORK("blasting work"),
        ROAD_CONSTRUCTION("road construction"),
        STRUCTURAL_IMPROVEMENT("structural improvement"),
        UNDERPASS_CONSTRUCTION("underpass construction"),
        PEDESTRIAN_AND_BICYCLE_PATH("pedestrian and bicycle path"),
        STABILIZATION("stabilization"),
        RESURFACING("resurfacing"),
        ROAD_SURFACE_MARKING("road surface marking"),
        FINISHING_WORK("finishing work"),
        MEASUREMENT("measurement"),
        TREE_AND_VEGETATION_CUTTING("tree and vegetation cutting"),
        GRASS_CUTTING("grass cutting"),
        MAINTENANCE("maintenance"),
        CULVERT_REPLACEMENT("culvert replacement"),
        OTHER("other");

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

    }

}
