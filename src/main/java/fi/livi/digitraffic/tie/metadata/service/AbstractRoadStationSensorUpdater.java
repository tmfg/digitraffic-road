package fi.livi.digitraffic.tie.metadata.service;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

public class AbstractRoadStationSensorUpdater {

    private static final Logger log = LoggerFactory.getLogger(AbstractRoadStationSensorUpdater.class);
    protected final RoadStationSensorService roadStationSensorService;

    public AbstractRoadStationSensorUpdater(RoadStationSensorService roadStationSensorService) {
        this.roadStationSensorService = roadStationSensorService;
    }

    protected static long obsoleteRoadStationSensors(final Collection<RoadStationSensor> toObsolete) {
        return toObsolete.stream().filter(rss -> rss.obsolete()).count();
    }
}
