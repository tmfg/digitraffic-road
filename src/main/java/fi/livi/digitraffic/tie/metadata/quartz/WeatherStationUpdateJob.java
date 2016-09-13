package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationsSensorsUpdater;

@DisallowConcurrentExecution
public class WeatherStationUpdateJob extends AbstractUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationUpdateJob.class);

    @Autowired
    public WeatherStationSensorUpdater weatherStationSensorUpdater;

    @Autowired
    public WeatherStationUpdater weatherStationUpdater;

    @Autowired
    public WeatherStationsSensorsUpdater weatherStationsSensorsUpdater;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        log.info("Quartz WeatherStationUpdateJob start");

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

        log.info("Quartz WeatherStationUpdateJob end (updateRoadStationSensors took: " + timeSensors +
                 " s, updateWeatherStations took: " + timeStations + " s, updateWeatherStationsSensors took: " + timeStationsSensors + " s)");
    }
}
