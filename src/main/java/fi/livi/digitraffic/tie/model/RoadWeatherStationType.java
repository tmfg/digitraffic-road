package fi.livi.digitraffic.tie.model;

import javax.xml.bind.annotation.XmlEnumValue;

import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsemaTyyppi;
import org.apache.log4j.Logger;

public enum RoadWeatherStationType {

    ROSA("ROSA"),
    @XmlEnumValue("E18")
    E_18("E18"),
    ISGN("ISGN"),
    OLD("OLD");

    private static final Logger LOG = Logger.getLogger(RoadWeatherStationType.class);

    private final String value;

    RoadWeatherStationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoadWeatherStationType fromTiesaaAsemaTyyppi(TiesaaAsemaTyyppi tsaTyyppi) {
        if (tsaTyyppi != null) {
            return RoadWeatherStationType.valueOf(tsaTyyppi.name());
        }
        return null;
    }


}
