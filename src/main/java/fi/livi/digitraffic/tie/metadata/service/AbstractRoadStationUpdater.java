package fi.livi.digitraffic.tie.metadata.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class AbstractRoadStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(AbstractRoadStationUpdater.class);

    protected static int obsoleteRoadStations(final List<RoadStation> obsoleteRoadStations) {
        int counter = 0;
        for (final RoadStation rs : obsoleteRoadStations) {
            if (rs.obsolete()) {
                log.debug("Obsolete " + rs);
                counter++;
            }
        }
        return counter;
    }


    protected void setRoadAddressIfNotSet(final RoadStation rs) {
        if (rs.getRoadAddress() == null) {
            rs.setRoadAddress(new RoadAddress());
        }
    }
}
