package fi.livi.digitraffic.tie.model.weather.forecast;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Forecast reliability")
public enum Reliability {

    SUCCESSFUL(0),
    NO_DATA_FROM_ROADSTATION(1),
    FAILED(2);

    private final int value;

    Reliability(final int value) {
        this.value = value;
    }

    public static Reliability of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (final Reliability reliability : values()) {
            if (reliability.value == value) {
                return reliability;
            }
        }
        return null;
    }
}
