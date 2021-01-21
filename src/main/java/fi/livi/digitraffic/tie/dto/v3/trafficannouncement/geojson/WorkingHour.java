
package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

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

        // ma, ti ke to pe -> legacy
        MONDAY("Monday", "ma"),
        TUESDAY("Tuesday","ti"),
        WEDNESDAY("Wednesday", "ke"),
        THURSDAY("Thursday", "to"),
        FRIDAY("Friday", "pe"),
        SATURDAY("Saturday", "la"),
        SUNDAY("Sunday", "su");

        private final String[] values;
        private final static Map<String, Weekday> CONSTANTS = new HashMap<>();

        static {
            for (final Weekday c: values()) {
                for (final String v: c.values) {
                    CONSTANTS.put(v.toUpperCase(), c);
                }
                CONSTANTS.put(c.name(), c);
            }
        }

        Weekday(final String...values) {
            this.values = values;
        }

        @JsonValue
        public String value() {
            return this.name();
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
