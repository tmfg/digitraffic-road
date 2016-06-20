package fi.livi.digitraffic.tie.metadata.model;

import static fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi.KORJAUSHUOLTO_TEHTY;
import static fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi.KORJAUSPYYNTO_LAHETETTY;
import static fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi.KORJAUS_KESKEYTETTY;
import static fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi.OK_VIKAEPAILY_PERUUTETTU;
import static fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi.VIKAEPAILY;
import static fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi.VIKA_VAHVISTETTU;
import static fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi.VIKA_VAHVISTETTU_EI_KORJATA_LAHIAIKOINA;

import org.apache.log4j.Logger;

import fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi;

public enum RoadStationState {

    OK(fi.livi.digitraffic.tie.lotju.wsdl.kamera.TilaTyyppi.OK.value()),
    OK_FAULT_DOUBT_CANCELLED(OK_VIKAEPAILY_PERUUTETTU.value()),
    FAULT_DOUBT(VIKAEPAILY.value()),
    FAULT_CONFIRMED(VIKA_VAHVISTETTU.value()),
    FAULT_CONFIRMED_NOT_FIXED_IN_NEAR_FUTURE(VIKA_VAHVISTETTU_EI_KORJATA_LAHIAIKOINA.value()),
    REPAIR_REQUEST_POSTED(KORJAUSPYYNTO_LAHETETTY.value()),
    REPAIR_MAINTENANCE_DONE(KORJAUSHUOLTO_TEHTY.value()),
    REPAIR_INTERRUPTED(KORJAUS_KESKEYTETTY.value());

    private static final Logger LOG = Logger.getLogger(RoadStationState.class);

    private final String fiValue;

    RoadStationState(final String fiValue) {
        this.fiValue = fiValue;
    }

    public String getFiValue() {
        return fiValue;
    }

    public static RoadStationState convertAsemanTila(TilaTyyppi asemanTila) {
        if (asemanTila != null) {
            return getState(asemanTila.value());
        }
        return null;
    }

    private static RoadStationState getState(final String fiValue) {
        for (final RoadStationState state : RoadStationState.values()) {
            if (state.getFiValue().equals(fiValue)) {
                return state;
            }
        }
        LOG.error("RoadStationState for TilaTyyppi " + fiValue + " not found");
        return null;
    }

}
