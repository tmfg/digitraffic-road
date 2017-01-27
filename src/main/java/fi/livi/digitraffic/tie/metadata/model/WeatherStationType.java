package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaTyyppi;

public enum WeatherStationType {

    ROSA("ROSA"),
    E_18("E18"),
    FINAVIA_V("FINAVIA_V"),
    FINAVIA_B("FINAVIA_B"),
    ISGN("ISGN"),
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
