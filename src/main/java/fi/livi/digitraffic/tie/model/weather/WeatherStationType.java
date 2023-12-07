package fi.livi.digitraffic.tie.model.weather;

import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaTyyppi;

public enum WeatherStationType {

    ROSA("ROSA"),
    RWS_200("RWS_200"),
    E_18("E18"),
    FINAVIA_V("FINAVIA_V"),
    FINAVIA_B("FINAVIA_B"),
    ELY_B("ELY_B"),
    ISGN("ISGN"),
    VAISALA_API("VAISALA_API"),
    OLD("OLD"),

    // Legacy values
    FINAVIA("FINAVIA");

    private final String value;

    WeatherStationType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static WeatherStationType fromTiesaaAsemaTyyppi(final TiesaaAsemaTyyppi tsaTyyppi) {
        if (tsaTyyppi != null) {
            return valueOf(tsaTyyppi.name());
        }
        return null;
    }
}
