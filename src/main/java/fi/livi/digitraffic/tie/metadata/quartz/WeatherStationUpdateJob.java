package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationsSensorsUpdater;

@DisallowConcurrentExecution
public class WeatherStationUpdateJob extends SimpleUpdateJob {

    @Autowired
    public WeatherStationSensorUpdater weatherStationSensorUpdater;

    @Autowired
    public WeatherStationUpdater weatherStationUpdater;

    @Autowired
    public WeatherStationsSensorsUpdater weatherStationsSensorsUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        final long startSensors = System.currentTimeMillis();
        final boolean sensorsUpdated = weatherStationSensorUpdater.updateRoadStationSensors();

        if (sensorsUpdated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.WEATHER_STATION_SENSOR);
        }

        final long startStationsEndSensors = System.currentTimeMillis();
        boolean stationsUpdated = weatherStationUpdater.updateWeatherStations();

        final long startStationsSensorsEndStations = System.currentTimeMillis();
        stationsUpdated = weatherStationsSensorsUpdater.updateWeatherStationsSensors() || stationsUpdated;
        final long endStationsSensors = System.currentTimeMillis();

        if (stationsUpdated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.WEATHER_STATION);
        }

        final long timeSensors = (startStationsEndSensors - startSensors)/1000;
        final long timeStations = (startStationsSensorsEndStations - startStationsEndSensors)/1000;
        final long timeStationsSensors = (endStationsSensors - startStationsSensorsEndStations)/1000;

        log.info("UpdateRoadStationSensors took: {} s, updateWeatherStations took: {} s, updateWeatherStationsSensors took: {} s)",
                timeSensors, timeStations, timeStationsSensors);
    }
}
