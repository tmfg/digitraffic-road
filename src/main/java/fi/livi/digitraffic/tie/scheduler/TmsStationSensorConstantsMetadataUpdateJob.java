package fi.livi.digitraffic.tie.scheduler;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.tms.TmsStationSensorConstantUpdater;

@DisallowConcurrentExecution
public class TmsStationSensorConstantsMetadataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {

        final StopWatch stationsSensorConstantsWatch = StopWatch.createStarted();
        final boolean updatedTmsStationSensorConstants =
            tmsStationSensorConstantUpdater.updateTmsStationsSensorConstants();
        stationsSensorConstantsWatch.stop();

        final StopWatch stationsSensorConstantValuesWatch = StopWatch.createStarted();
        final boolean updatedTmsStationSensorConstantValues = tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantsValues();
        stationsSensorConstantValuesWatch.stop();

        if (updatedTmsStationSensorConstants || updatedTmsStationSensorConstantValues) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_CONSTANT_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_CONSTANT_METADATA_CHECK);

        log.info("TmsStationSensorConstants took: sensorConstantsTimeMs={} ms, TmsStationSensorConstantValues took: sensorConstantsValuesTimeMs={}",
                 stationsSensorConstantsWatch.getTime(), stationsSensorConstantValuesWatch.getTime());
    }
}
