package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationsSensorsUpdater;

@DisallowConcurrentExecution
public class TmsStationUpdateJob extends AbstractUpdateJob {

    private static final Logger log =  LoggerFactory.getLogger(TmsStationUpdateJob.class);

    @Autowired
    public TmsStationSensorUpdater tmsStationSensorUpdater;

    @Autowired
    public TmsStationUpdater tmsStationUpdater;

    @Autowired
    public TmsStationsSensorsUpdater tmsStationsSensorsUpdater;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        log.info("Quartz TmsStationUpdateJob start");

        final long startSensors = System.currentTimeMillis();
        final boolean sensorsUpdated = tmsStationSensorUpdater.updateRoadStationSensors();

        if (sensorsUpdated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.LAM_ROAD_STATION_SENSOR);
        }

        final long startStationsEndSensors = System.currentTimeMillis();
        boolean stationsUpdated = tmsStationUpdater.updateTmsStations();
        final long startStationsSensorsEndStations = System.currentTimeMillis();
        tmsStationsSensorsUpdater.updateTmsStationsSensors();
        final long endStationsSensors = System.currentTimeMillis();

        if (stationsUpdated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.LAM_STATION);
        }

        final long timeSensors = (startStationsEndSensors - startSensors)/1000;
        final long timeStations = (startStationsSensorsEndStations - startStationsEndSensors)/1000;
        final long timeStationsSensors = (endStationsSensors - startStationsSensorsEndStations)/1000;

        log.info("Quartz TmsStationUpdateJob end (updateRoadStationSensors took: " + timeSensors +
                " s, updateTmsStations took: " + timeStations + " s, updateRoadStationSensors took: " + timeStationsSensors + " s)");
    }
}
