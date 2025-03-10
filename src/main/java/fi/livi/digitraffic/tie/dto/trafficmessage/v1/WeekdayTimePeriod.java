
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Weekday time period", name = "WeekdayTimePeriodV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "weekday",
    "startTime",
    "endTime"
})
public class WeekdayTimePeriod extends JsonAdditionalProperties {

    @Schema(description = "Weekday", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public WeekdayTimePeriod.Weekday weekday;

    @Schema(description = "Start time of the time period in ISO 8601 local time in Europe/Helsinki", type = "java.lang.String", example = "09:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public LocalTime startTime;

    @Schema(description = "End time of the time period in ISO 8601 local time in Europe/Helsinki", type = "java.lang.String", example = "15:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public LocalTime endTime;

    public WeekdayTimePeriod() {
    }

    public WeekdayTimePeriod(final Weekday weekday, final LocalTime startTime, final LocalTime endTime) {
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
