package fi.livi.digitraffic.tie.metadata.quartz;

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

        final long startSensors = System.currentTimeMillis();
        final boolean sensorsUpdated = tmsStationSensorUpdater.updateRoadStationSensors();

        if (sensorsUpdated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.LAM_ROAD_STATION_SENSOR);
        }

        final long startStationsEndSensors = System.currentTimeMillis();
        final boolean stationsUpdated = tmsStationUpdater.updateTmsStations();
        final long startStationsSensorsEndStations = System.currentTimeMillis();
        final boolean updatedTmsStationsSensors = tmsStationsSensorsUpdater.updateTmsStationsSensors();
        final long endStationsSensors = System.currentTimeMillis();

        if (stationsUpdated || updatedTmsStationsSensors) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.LAM_STATION);
        }

        final long timeSensors = (startStationsEndSensors - startSensors)/1000;
        final long timeStations = (startStationsSensorsEndStations - startStationsEndSensors)/1000;
        final long timeStationsSensors = (endStationsSensors - startStationsSensorsEndStations)/1000;

        log.info("Update TmsStations took: {} s, updateTmsStations took: {} s, updateTmsStationsSensors took: {} s)",
                timeSensors, timeStations, timeStationsSensors);
    }
}
