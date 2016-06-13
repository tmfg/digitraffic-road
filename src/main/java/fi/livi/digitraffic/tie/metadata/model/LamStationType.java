package fi.livi.digitraffic.tie.metadata.model;

import fi.livi.digitraffic.tie.lotju.wsdl.lam.LamAsemaTyyppi;

public enum LamStationType {

    DSL("DSL"),
    E_18("E18");
    private final String value;

    LamStationType(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LamStationType convertFromKameraTyyppi(final LamAsemaTyyppi lamAsemaTyyppi) {
        if (lamAsemaTyyppi != null) {
            return valueOf(lamAsemaTyyppi.name());
        }
        return null;
    }
}
