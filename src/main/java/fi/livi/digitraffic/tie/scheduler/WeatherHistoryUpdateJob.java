package fi.livi.digitraffic.tie.scheduler;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.SensorDataS3Writer;
import fi.livi.digitraffic.tie.service.roadstation.SensorDataUpdateService;

@DisallowConcurrentExecution
public class WeatherHistoryUpdateJob extends SimpleUpdateJob {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SensorDataUpdateService sensorDataUpdateService;

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SensorDataS3Writer sensorDataS3Writer;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        final ZonedDateTime now = ZonedDateTime.now();
        // Do one hour time window (xx:00 - xx:59)
        final ZonedDateTime to = now.truncatedTo(ChronoUnit.HOURS);
        final ZonedDateTime from = to.minusHours(1);

        // Check missing history items. NOTE! DISABLED
        //sensorDataS3Writer.updateSensorDataS3History(from);

        log.info("Storing sensor history to S3, time window {} - {}", from, to);

        // Write one hour time window to S3
        sensorDataS3Writer.writeSensorData(from, to);

        log.info("Cleaning sensor data history, older than {}", to.minusHours(24));

        // DB maintenance: remove history older than 24h
        sensorDataUpdateService.cleanWeatherHistoryData(to.minusHours(24));
    }
}
