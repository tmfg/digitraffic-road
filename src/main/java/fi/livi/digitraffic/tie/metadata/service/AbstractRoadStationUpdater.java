package fi.livi.digitraffic.tie.metadata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class AbstractRoadStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(AbstractRoadStationUpdater.class);

    protected void setRoadAddressIfNotSet(final RoadStation rs) {
        if (rs.getRoadAddress() == null) {
            rs.setRoadAddress(new RoadAddress());
        }
    }
}
