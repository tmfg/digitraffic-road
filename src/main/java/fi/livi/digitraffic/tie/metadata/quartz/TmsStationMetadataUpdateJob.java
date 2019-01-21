package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationsSensorsUpdater;

@DisallowConcurrentExecution
public class TmsStationMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private TmsStationSensorUpdater tmsStationSensorUpdater;

    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    @Autowired
    private TmsStationsSensorsUpdater tmsStationsSensorsUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {

        final StopWatch sensorsWatch = StopWatch.createStarted();
        final boolean sensorsUpdated = tmsStationSensorUpdater.updateRoadStationSensors();
        if (sensorsUpdated) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA_CHECK);
        sensorsWatch.stop();

        final StopWatch stationsWatch = StopWatch.createStarted();
        final boolean stationsUpdated = tmsStationUpdater.updateTmsStations();
        stationsWatch.stop();

        final StopWatch stationsSensorsWatch = StopWatch.createStarted();
        final boolean updatedTmsStationsSensors = tmsStationsSensorsUpdater.updateTmsStationsSensors();
        stationsSensorsWatch.stop();

        final StopWatch stationsSensorConstantsWatch = StopWatch.createStarted();
        final boolean updatedTmsStationSensorConstants = tmsStationsSensorsUpdater.updateTmsStationsSensorConstants();
        stationsSensorConstantsWatch.stop();

        final StopWatch stationsSensorConstantValuesWatch = StopWatch.createStarted();
        final boolean updatedTmsStationSensorConstantValues = tmsStationsSensorsUpdater.updateTmsStationsSensorConstantsValues();
        stationsSensorConstantValuesWatch.stop();

        if (stationsUpdated || updatedTmsStationsSensors) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA_CHECK);

        log.info("UpdateRoadStationSensors took: sensorsTimeMs={} ms, updateWeatherStations took: stationsTimeMs={} ms, updateWeatherStationsSensors took: stationsSensorsTimeMs={} ms",
                sensorsWatch.getTime(), stationsWatch.getTime(), stationsSensorsWatch.getTime());
    }
}
