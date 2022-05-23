package fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Area location type", name = "AreaTypeV3")
public enum AreaType {

    MUNICIPALITY("municipality"),
    PROVINCE("province"),
    REGIONAL_STATE_ADMINISTRATIVE_AGENCY("regional state administrative agency"),
    WEATHER_REGION("weather region"),
    COUNTRY("country"),
    CITY_REGION("city region"),
    TRAVEL_REGION("travel region"),
    UNKNOWN("UNKNOWN");

    AreaType(final String fromValue) {
        this.value = fromValue;
    }

    private final String value;
    private final static Map<String, AreaType> CONSTANTS = new HashMap<>();

    static {
        for (AreaType c : values()) {
            CONSTANTS.put(c.value, c);
            CONSTANTS.put(c.name(), c);
        }
    }

    @JsonCreator
    public static AreaType fromValue(final String value) {
        final AreaType constant = CONSTANTS.get(value);
        return Objects.requireNonNullElseGet(constant, () -> AreaType.valueOf(value.toUpperCase()));
    }

    public String getFromValue() {
        return value;
    }
}
