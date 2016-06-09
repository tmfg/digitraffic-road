package fi.livi.digitraffic.tie.metadata.model;

import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.TiesaaAsemaTyyppi;

public enum RoadWeatherStationType {

    ROSA("ROSA"),
    @XmlEnumValue("E18")
    E_18("E18"),
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
