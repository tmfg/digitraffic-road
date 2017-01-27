package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaTyyppi;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "TMS station type", value = "TmsStationType")
public enum TmsStationType {

    DSL_4("DSL_4"),
    DSL_6("DSL_6"),
    E_18("E18"),

    // legacy values
    DSL("DSL"),
    FINAVIA("FINAVIA");

    private final String value;

    TmsStationType(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TmsStationType convertFromLamasemaTyyppi(final LamAsemaTyyppi lamAsemaTyyppi) {
        if (lamAsemaTyyppi != null) {
            return valueOf(lamAsemaTyyppi.name());
        }
        return null;
    }
}
