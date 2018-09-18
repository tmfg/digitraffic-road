package fi.livi.digitraffic.tie.metadata.model;

import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi.KORJAUSHUOLTO_TEHTY;
import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi.KORJAUSPYYNTO_LAHETETTY;
import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi.KORJAUS_KESKEYTETTY;
import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi.OK_VIKAEPAILY_PERUUTETTU;
import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi.VIKAEPAILY;
import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi.VIKA_VAHVISTETTU;
import static fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi.VIKA_VAHVISTETTU_EI_KORJATA_LAHIAIKOINA;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RoadStationState {

    OK(fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi.OK.value()),
    OK_FAULT_DOUBT_CANCELLED(OK_VIKAEPAILY_PERUUTETTU.value()),
    FAULT_DOUBT(VIKAEPAILY.value()),
    FAULT_CONFIRMED(VIKA_VAHVISTETTU.value()),
    FAULT_CONFIRMED_NOT_FIXED_IN_NEAR_FUTURE(VIKA_VAHVISTETTU_EI_KORJATA_LAHIAIKOINA.value()),
    REPAIR_REQUEST_POSTED(KORJAUSPYYNTO_LAHETETTY.value()),
    REPAIR_MAINTENANCE_DONE(KORJAUSHUOLTO_TEHTY.value()),
    REPAIR_INTERRUPTED(KORJAUS_KESKEYTETTY.value());

    private static final Logger LOG = LoggerFactory.getLogger(RoadStationState.class);

    private final String fiValue;

    RoadStationState(final String fiValue) {
        this.fiValue = fiValue;
    }

    public String getFiValue() {
        return fiValue;
    }

    public static RoadStationState convertAsemanTila(final fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.TilaTyyppi asemanTila) {
        if (asemanTila != null) {
            return getState(asemanTila.value());
        }
        return null;
    }

    public static RoadStationState convertAsemanTila(fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TilaTyyppi asemanTila) {
        if (asemanTila != null) {
            return getState(asemanTila.value());
        }
        return null;
    }

    public static RoadStationState convertAsemanTila(fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.TilaTyyppi asemanTila) {
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
