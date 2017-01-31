package fi.livi.digitraffic.tie.metadata.service;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

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

    protected static int obsoleteRoadStationSensors(final Collection<RoadStationSensor> toObsolete) {
        final AtomicInteger obsoleted = new AtomicInteger();
        toObsolete.stream().forEach(sensor -> {
            if (sensor.obsolete()) {
                obsoleted.addAndGet(1);
            }
        });
        return obsoleted.get();
    }
}
