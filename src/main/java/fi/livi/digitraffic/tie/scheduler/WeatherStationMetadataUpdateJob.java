package fi.livi.digitraffic.tie.scheduler;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.service.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.service.weather.WeatherStationsSensorsUpdater;

@DisallowConcurrentExecution
public class WeatherStationMetadataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public WeatherStationSensorUpdater weatherStationSensorUpdater;

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public WeatherStationUpdater weatherStationUpdater;

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public WeatherStationsSensorsUpdater weatherStationsSensorsUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) throws Exception {
        final StopWatch sensorsWatch = StopWatch.createStarted();
        final boolean sensorsUpdated = weatherStationSensorUpdater.updateRoadStationSensors();
        if (sensorsUpdated) {
            dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_SENSOR_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_SENSOR_METADATA_CHECK);
        sensorsWatch.stop();

        final StopWatch stationsWatch = StopWatch.createStarted();
        boolean stationsUpdated = weatherStationUpdater.updateWeatherStations();
        stationsWatch.stop();

        final StopWatch stationsSensors = StopWatch.createStarted();
        stationsUpdated = weatherStationsSensorsUpdater.updateWeatherStationsSensors() || stationsUpdated;
        stationsSensors.stop();

        if (stationsUpdated) {
            dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA_CHECK);

        log.info("UpdateRoadStationSensors took: sensorsTimeMs={} ms, updateWeatherStations took: stationsTimeMs={} ms, updateWeatherStationsSensors took: stationsSensorsTimeMs={} ms",
                sensorsWatch.getDuration().toMillis(), stationsWatch.getDuration().toMillis(), stationsSensors.getDuration().toMillis());
    }
}
