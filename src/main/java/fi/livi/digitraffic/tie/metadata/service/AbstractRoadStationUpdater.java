package fi.livi.digitraffic.tie.metadata.service;

import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class AbstractRoadStationUpdater {
    protected void setRoadAddressIfNotSet(final RoadStation rs) {
        if (rs.getRoadAddress() == null) {
            rs.setRoadAddress(new RoadAddress());
        }
    }
}
