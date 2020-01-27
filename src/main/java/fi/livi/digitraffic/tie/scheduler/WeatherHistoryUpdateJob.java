package fi.livi.digitraffic.tie.scheduler;

import java.time.ZonedDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.v1.SensorDataUpdateService;

@DisallowConcurrentExecution
public class WeatherHistoryUpdateJob extends SimpleUpdateJob {

    @Autowired
    private SensorDataUpdateService sensorDataUpdateService;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        // Get current time and move offset 24h
        //final ZonedDateTime before = ZonedDateTime.now().minusHours(24);
        // Just for tests
        final ZonedDateTime before = ZonedDateTime.now().minusHours(1);

        sensorDataUpdateService.cleanWeatherHistoryData(before);
    }
}
