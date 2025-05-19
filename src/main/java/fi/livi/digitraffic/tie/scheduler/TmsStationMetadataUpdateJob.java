package fi.livi.digitraffic.tie.scheduler;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.tms.TmsSensorUpdater;
import fi.livi.digitraffic.tie.service.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.service.tms.TmsStationsSensorsUpdater;

@DisallowConcurrentExecution
public class TmsStationMetadataUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TmsSensorUpdater tmsSensorUpdater;

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private TmsStationsSensorsUpdater tmsStationsSensorsUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) throws Exception {

        final StopWatch sensorsWatch = StopWatch.createStarted();
        final boolean sensorsUpdated = tmsSensorUpdater.updateTmsSensors();
        if (sensorsUpdated) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA_CHECK);
        sensorsWatch.stop();

        final StopWatch stationsWatch = StopWatch.createStarted();
        tmsStationUpdater.updateTmsStations();
        stationsWatch.stop();

        final StopWatch stationsSensorsWatch = StopWatch.createStarted();
        tmsStationsSensorsUpdater.updateTmsStationsSensors();
        stationsSensorsWatch.stop();

        dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA_CHECK);

        log.info("method=doExecute UpdateRoadStationSensors took: sensorsTimeMs={} ms, updateTmsStations took: stationsTimeMs={} ms, updateTmsStationsSensors took: stationsSensorsTimeMs={} ms",
                 sensorsWatch.getDuration().toMillis(), stationsWatch.getDuration().toMillis(), stationsSensorsWatch.getDuration().toMillis());
    }
}
