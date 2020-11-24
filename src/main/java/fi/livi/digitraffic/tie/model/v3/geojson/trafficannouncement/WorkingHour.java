
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Working hour", value = "WorkingHourV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "weekday",
    "startTime",
    "endTime"
})
public class WorkingHour extends JsonAdditionalProperties {

    @ApiModelProperty(value = "Weekday", required = true)
    @NotNull
    public WorkingHour.Weekday weekday;

    @ApiModelProperty(value = "Road work start time using ISO 8601 local time", required = true)
    @NotNull
    public String startTime;

    @ApiModelProperty(value = "Road work end time using ISO 8601 local time", required = true)
    @NotNull
    public String endTime;

    public WorkingHour() {
    }

    public WorkingHour(Weekday weekday, String startTime, String endTime) {
        super();
        this.weekday = weekday;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public enum Weekday {

        MONDAY("ma"),
        TUESDAY("ti"),
        WEDNESDAY("ke"),
        THURSDAY("to"),
        FRIDAY("pe"),
        SATURDAY("la"),
        SUNDAY("su");

        private final String value;
        private final static Map<String, Weekday> CONSTANTS = new HashMap<>();

        static {
            for (Weekday c: values()) {
                CONSTANTS.put(c.value.toUpperCase(), c);
            }
        }

        Weekday(String value) {
            this.value = value;
        }

        @JsonCreator
        public static Weekday fromValue(final String value) {
            final Weekday constant = CONSTANTS.get(value.toUpperCase());
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }
    }
}
