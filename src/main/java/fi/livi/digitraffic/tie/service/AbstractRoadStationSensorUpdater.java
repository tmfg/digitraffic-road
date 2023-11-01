package fi.livi.digitraffic.tie.service;

public abstract class AbstractRoadStationSensorUpdater {

    protected final RoadStationSensorService roadStationSensorService;

    public AbstractRoadStationSensorUpdater(final RoadStationSensorService roadStationSensorService) {
        this.roadStationSensorService = roadStationSensorService;
    }
}
