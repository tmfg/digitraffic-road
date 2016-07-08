package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaTyyppi;

public enum LamStationType {

    DSL("DSL"),
    E_18("E18"),
    FINAVIA("FINAVIA");
    private final String value;

    LamStationType(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LamStationType convertFromLamasemaTyyppi(final LamAsemaTyyppi lamAsemaTyyppi) {
        if (lamAsemaTyyppi != null) {
            return valueOf(lamAsemaTyyppi.name());
        }
        return null;
    }
}
