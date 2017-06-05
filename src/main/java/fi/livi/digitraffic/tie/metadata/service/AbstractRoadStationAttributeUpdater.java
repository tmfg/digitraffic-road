package fi.livi.digitraffic.tie.metadata.service;

import org.slf4j.Logger;

import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class AbstractRoadStationAttributeUpdater {

    protected final Logger log;

    public AbstractRoadStationAttributeUpdater(final Logger logger) {
        this.log = logger;
    }

    public static boolean setRoadAddressIfNotSet(final RoadStation rs) {
        if (rs.getRoadAddress() == null) {
            rs.setRoadAddress(new RoadAddress());
            return true;
        }
        return false;
    }
}
