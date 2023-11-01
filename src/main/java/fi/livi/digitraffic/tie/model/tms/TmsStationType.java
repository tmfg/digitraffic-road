package fi.livi.digitraffic.tie.model.tms;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaTyyppi;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TMS station type", name = "TmsStationType")
public enum TmsStationType {

    DSL_4("DSL_4"),
    DSL_6("DSL_6"),
    E_18("E18"),
    LML_1("LML_1"),
    OLD("OLD"),

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
