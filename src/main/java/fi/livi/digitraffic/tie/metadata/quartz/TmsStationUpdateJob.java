package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationsSensorsUpdater;

@DisallowConcurrentExecution
public class TmsStationUpdateJob extends SimpleUpdateJob {

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
            dataStatusService.updateMetadataUpdated(MetadataType.LAM_ROAD_STATION_SENSOR);
        }
        sensorsWatch.stop();

        final StopWatch stationsWatch = StopWatch.createStarted();
        final boolean stationsUpdated = tmsStationUpdater.updateTmsStations();
        stationsWatch.stop();

        final StopWatch stationsSensorsWatch = StopWatch.createStarted();
        final boolean updatedTmsStationsSensors = tmsStationsSensorsUpdater.updateTmsStationsSensors();
        stationsSensorsWatch.stop();

        if (stationsUpdated || updatedTmsStationsSensors) {
            dataStatusService.updateMetadataUpdated(MetadataType.LAM_STATION);
        }

        log.info("UpdateRoadStationSensors took: {} ms, updateTmsStations took: {} ms, updateTmsStationsSensors took: {} ms",
                sensorsWatch.getTime(), stationsWatch.getTime(), stationsSensorsWatch.getTime());
    }
}
