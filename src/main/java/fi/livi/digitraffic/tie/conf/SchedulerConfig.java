package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.conf.TriggerFactoryFactory.createRepeatingTrigger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import fi.livi.digitraffic.tie.metadata.quartz.AutowiringSpringBeanJobFactory;
import fi.livi.digitraffic.tie.metadata.quartz.CameraUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.ForecastSectionCoordinatesUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.ForecastSectionWeatherUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.LocationMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.RoadStationsStatusUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TmsStationUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TravelTimeMeasurementsUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TravelTimeMediansUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.UnhandledDatex2MessagesImportJob;
import fi.livi.digitraffic.tie.metadata.quartz.WeatherStationUpdateJob;

@Configuration
@ConditionalOnProperty(name = "quartz.enabled")
public class SchedulerConfig {
    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

    @Bean
    public JobFactory jobFactory(final ApplicationContext applicationContext) {
        final AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(final DataSource dataSource,
                                                     final JobFactory jobFactory,
                                                     final Optional<List<Trigger>> triggerBeans) throws IOException {
        final SchedulerFactoryBean factory = new SchedulerFactoryBean();
        // this allows to update triggers in DB when updating settings in config file:
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());

        // https://github.com/javamelody/javamelody/wiki/UserGuide#13-batch-jobs-if-quartz
        factory.setExposeSchedulerInRepository(true);

        if (triggerBeans.isPresent()) {
            final List<Trigger> triggers = triggerBeans.get();

            triggers.stream().forEach(triggerBean -> log.info("Schedule trigger {}", triggerBean.getJobKey()));

            factory.setTriggers(triggers.toArray(new Trigger[triggers.size()]));
        }
        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @Bean
    public JobDetailFactoryBean cameraUpdateJobDetail() {
        return createJobDetail(CameraUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean tmsStationUpdateJobDetail() {
        return createJobDetail(TmsStationUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean weatherStationUpdateJobDetail() {
        return createJobDetail(WeatherStationUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean roadStationsStatusUpdateJobDetail() {
        return createJobDetail(RoadStationsStatusUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean locationMetadataUpdateJobDetail() { return createJobDetail(LocationMetadataUpdateJob.class); }

    @Bean
    public JobDetailFactoryBean forecastSectionCoordinatesUpdateJobDetail() {
        return createJobDetail(ForecastSectionCoordinatesUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean forecastSectionWeatherUpdateJobDetail() {
        return createJobDetail(ForecastSectionWeatherUpdateJob.class);
    }


    @Bean
    public JobDetailFactoryBean unhandledDatex2MessagesImportJobDetail() {
        return createJobDetail(UnhandledDatex2MessagesImportJob.class);
    }

    @Bean
    public JobDetailFactoryBean travelTimeMediansUpdateJobDetail() {
        return createJobDetail(TravelTimeMediansUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean travelTimeMeasurementsUpdateJobDetail() {
        return createJobDetail(TravelTimeMeasurementsUpdateJob.class);
    }

    @Bean
    public SimpleTriggerFactoryBean cameraUpdateJobTrigger(final JobDetail cameraUpdateJobDetail,
                                                           @Value("${cameraStationUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(cameraUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean tmsStationUpdateJobTrigger(final JobDetail tmsStationUpdateJobDetail,
                                                               @Value("${tmsStationUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(tmsStationUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean weatherStationUpdateJobTrigger(final JobDetail weatherStationUpdateJobDetail,
                                                                   @Value("${weatherStationUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(weatherStationUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean roadStationsStatusUpdateJobTrigger(final JobDetail roadStationsStatusUpdateJobDetail,
                                                                       @Value("${roadStationsStatusUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(roadStationsStatusUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean locationsMetadataUpdateJobTrigger(final JobDetail locationMetadataUpdateJobDetail,
                                                                      @Value("${locationsMetadataUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(locationMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean forecastSectionCoordinatesUpdateJobTrigger(final JobDetail forecastSectionCoordinatesUpdateJobDetail,
                                                                               @Value("${forecastSectionCoordinatesUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(forecastSectionCoordinatesUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean forecastSectionWeatherUpdateJobTrigger(final JobDetail forecastSectionWeatherUpdateJobDetail,
                                                                           @Value("${forecastSectionWeatherUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(forecastSectionWeatherUpdateJobDetail, frequency);
    }


    @Bean
    public SimpleTriggerFactoryBean unhandledDatex2MessagesImportJobTrigger(final JobDetail unhandledDatex2MessagesImportJobDetail,
                                                                            @Value("${unhandledDatex2MessagesImportJob.frequency}") final long frequency) {
        return createRepeatingTrigger(unhandledDatex2MessagesImportJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean travelTimeMediansUpdateJobTrigger(final JobDetail travelTimeMediansUpdateJobDetail,
                                                                      @Value("${travelTimeMediansUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(travelTimeMediansUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean travelTimeMeasurementsUpdateJobTrigger(final JobDetail travelTimeMeasurementsUpdateJobDetail,
                                                                           @Value("${travelTimeMeasurementsUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(travelTimeMeasurementsUpdateJobDetail, frequency);
    }

    private static JobDetailFactoryBean createJobDetail(final Class jobClass) {
        final JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        // job has to be durable to be stored in DB:
        factoryBean.setDurability(true);
        return factoryBean;
    }
}
