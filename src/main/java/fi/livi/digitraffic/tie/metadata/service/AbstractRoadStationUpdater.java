package fi.livi.digitraffic.tie.metadata.service;

import org.slf4j.Logger;

import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class AbstractRoadStationUpdater {

    protected final Logger log;

    public AbstractRoadStationUpdater(final Logger logger) {
        this.log = logger;
    }

    protected void setRoadAddressIfNotSet(final RoadStation rs) {
        if (rs.getRoadAddress() == null) {
            rs.setRoadAddress(new RoadAddress());
        }
    }

    protected void logErrorIf(boolean doLog, String format, Object... arguments) {
        if (doLog) {
            log.error(format, arguments);
        }
    }
}
