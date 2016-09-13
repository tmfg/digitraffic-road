package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationsSensorsUpdater;

@DisallowConcurrentExecution
public class LamStationUpdateJob extends AbstractUpdateJob {

    private static final Logger log =  LoggerFactory.getLogger(LamStationUpdateJob.class);

    @Autowired
    public LamStationSensorUpdater lamStationSensorUpdater;

    @Autowired
    public LamStationUpdater lamStationUpdater;

    @Autowired
    public LamStationsSensorsUpdater lamStationsSensorsUpdater;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        log.info("Quartz LamStationUpdateJob start");

        final long startSensors = System.currentTimeMillis();
        final boolean sensorsUpdated = lamStationSensorUpdater.updateRoadStationSensors();

        if (sensorsUpdated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.LAM_ROAD_STATION_SENSOR);
        }

        final long startStationsEndSensors = System.currentTimeMillis();
        boolean stationsUpdated = lamStationUpdater.updateLamStations();
        final long startStationsSensorsEndStations = System.currentTimeMillis();
        lamStationsSensorsUpdater.updateLamStationsSensors();
        final long endStationsSensors = System.currentTimeMillis();

        if (stationsUpdated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.LAM_STATION);
        }

        final long timeSensors = (startStationsEndSensors - startSensors)/1000;
        final long timeStations = (startStationsSensorsEndStations - startStationsEndSensors)/1000;
        final long timeStationsSensors = (endStationsSensors - startStationsSensorsEndStations)/1000;

        log.info("Quartz LamStationUpdateJob end (updateRoadStationSensors took: " + timeSensors +
                " s, updateLamStations took: " + timeStations + " s, updateRoadStationSensors took: " + timeStationsSensors + " s)");
    }
}
