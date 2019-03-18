package fi.livi.digitraffic.tie.metadata.service;

import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

public abstract class AbstractRoadStationSensorUpdater {

    protected final RoadStationSensorService roadStationSensorService;

    public AbstractRoadStationSensorUpdater(RoadStationSensorService roadStationSensorService) {
        this.roadStationSensorService = roadStationSensorService;
    }
}
