package fi.livi.digitraffic.tie.model;

import fi.livi.digitraffic.tie.wsdl.lam.LamAsemaTyyppi;

public enum LamStationType {

    DSL("DSL"),
    E_18("E18");
    private final String value;

    LamStationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LamStationType convertFromKameraTyyppi(LamAsemaTyyppi lamAsemaTyyppi) {
        if (lamAsemaTyyppi != null) {
            return LamStationType.valueOf(lamAsemaTyyppi.name());
        }
        return null;
    }
}
