package fi.livi.digitraffic.tie.metadata.model;

import org.apache.log4j.Logger;

import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaAsemaTyyppi;

public enum RoadWeatherStationType {

     ROSA("ROSA"),
    E_18("E18"),
    FINAVIA("FINAVIA"),
    ISGN("ISGN"),
    OLD("OLD");

    private static final Logger LOG = Logger.getLogger(RoadWeatherStationType.class);

    private final String value;

    RoadWeatherStationType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoadWeatherStationType fromTiesaaAsemaTyyppi(final TiesaaAsemaTyyppi tsaTyyppi) {
        if (tsaTyyppi != null) {
            return valueOf(tsaTyyppi.name());
        }
        return null;
    }
}
