package fi.livi.digitraffic.tie.metadata.quartz;


import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationUpdater;

@DisallowConcurrentExecution
public class RoadWeatherStationUpdateJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(RoadWeatherStationUpdateJob.class);

    @Autowired
    public RoadWeatherStationUpdater roadWeatherStationUpdater;

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) {
        log.info("Quartz RoadWeatherStationUpdateJob start");

        final long startSensors = System.currentTimeMillis();
        roadWeatherStationUpdater.updateRoadStationSensors();

        final long startStationsEndSensors = System.currentTimeMillis();
        roadWeatherStationUpdater.updateRoadWeatherStations();

        final long startStationsSensorsEndStations = System.currentTimeMillis();
        roadWeatherStationUpdater.updateRoadWeatherStationsRoadStationSensors();
        final long endStationsSensors = System.currentTimeMillis();

        final long timeSensors = (startStationsEndSensors - startSensors)/1000;
        final long timeStations = (startStationsSensorsEndStations - startStationsEndSensors)/1000;
        final long timeStationsSensors = (endStationsSensors - startStationsSensorsEndStations)/1000;


        log.info("Quartz RoadWeatherStationUpdateJob end (updateRoadStationSensors took: " + timeSensors +
                 " s, updateRoadWeatherStations took: " + timeStations + " s, updateRoadWeatherStationsRoadStationSensors took: " + timeStationsSensors + " s)");
    }
}
