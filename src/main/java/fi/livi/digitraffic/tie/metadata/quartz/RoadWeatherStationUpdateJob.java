package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationUpdater;

@DisallowConcurrentExecution
public class RoadWeatherStationUpdateJob implements Job {

    private static final Logger log = Logger.getLogger(RoadWeatherStationUpdateJob.class);

    @Autowired
    public RoadWeatherStationUpdater roadWeatherStationUpdater;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Quartz RoadWeatherStationUpdateJob start");

        long startStations = System.currentTimeMillis();
        roadWeatherStationUpdater.updateWeatherStations();

        long startSensors = System.currentTimeMillis();
        roadWeatherStationUpdater.updateRoadStationSensors();

        long end = System.currentTimeMillis();
        long timeStations = (startSensors - startStations)/1000;
        long timeSensors = (end - startSensors)/1000;

        log.info("Quartz RoadWeatherStationUpdateJob end (updateWeatherStations: " + timeStations + " s, updateRoadStationSensors: " + timeSensors + " s)");
    }
}
