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
    protected void doExecute(final JobExecutionContext context) throws Exception {

        final StopWatch stationsSensorConstantsWatch = StopWatch.createStarted();
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstants();
        stationsSensorConstantsWatch.stop();

        final StopWatch stationsSensorConstantValuesWatch = StopWatch.createStarted();
        tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantsValues();
        stationsSensorConstantValuesWatch.stop();

        dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_CONSTANT_METADATA_CHECK);

        log.info("TmsStationSensorConstants took: sensorConstantsTimeMs={} ms, TmsStationSensorConstantValues took: sensorConstantsValuesTimeMs={}",
                 stationsSensorConstantsWatch.getDuration().toMillis(), stationsSensorConstantValuesWatch.getDuration().toMillis());
    }
}
