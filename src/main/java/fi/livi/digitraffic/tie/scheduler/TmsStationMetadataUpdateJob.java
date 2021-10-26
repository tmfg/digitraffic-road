package fi.livi.digitraffic.tie.scheduler;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.v1.tms.TmsSensorUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationsSensorsUpdater;

@DisallowConcurrentExecution
public class TmsStationMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private TmsSensorUpdater tmsSensorUpdater;

    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    @Autowired
    private TmsStationsSensorsUpdater tmsStationsSensorsUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {

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
                 sensorsWatch.getTime(), stationsWatch.getTime(), stationsSensorsWatch.getTime());
    }
}
