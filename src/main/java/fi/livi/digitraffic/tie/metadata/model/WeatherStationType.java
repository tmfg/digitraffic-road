package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaAsemaTyyppi;

public enum WeatherStationType {

    ROSA("ROSA"),
    E_18("E18"),
    FINAVIA("FINAVIA"),
    ISGN("ISGN"),
    OLD("OLD");

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