package fi.livi.digitraffic.tie.conf;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import fi.livi.digitraffic.tie.metadata.quartz.AutowiringSpringBeanJobFactory;
import fi.livi.digitraffic.tie.metadata.quartz.CameraMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.CameraStationsStatusMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.Datex2RoadworksMessageUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.Datex2TrafficAlertMessageUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.Datex2WeightRestrictionsMessageUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.ForecastSectionCoordinatesMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.ForecastSectionWeatherUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.LocationMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TmsStationMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TmsStationSensorConstantsMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TmsStationsStatusMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.WeatherStationMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.WeatherStationsStatusMetadataUpdateJob;

@Configuration
@ConditionalOnProperty(name = "quartz.enabled")
@ConditionalOnNotWebApplication
public class QuartzSchedulerConfig {
    private static final Logger log = LoggerFactory.getLogger(QuartzSchedulerConfig.class);
    private final Environment environment;

    // Number has 13 digits in db
    private final Date QUARTZ_MAX_DATE = new Date(9999999999999L);

    public QuartzSchedulerConfig(final Environment environment) {
        this.environment = environment;
    }

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
        final SchedulerFactoryBean factory = new SchedulerFactoryBean() {
            @Override
            protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) throws SchedulerException {
                Scheduler scheduler = super.createScheduler(schedulerFactory, schedulerName);
                // Clear previous job definitions
                scheduler.clear();
                return scheduler;
            }
        };
        // this allows to update triggers in DB when updating settings in config file:
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());

        if (triggerBeans.isPresent()) {
            final List<Trigger> triggers = triggerBeans.get();
            triggers.forEach(triggerBean -> log.info("Schedule trigger={}", triggerBean.getJobKey()));
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
    public JobDetailFactoryBean cameraMetadataUpdateJobDetail() {
        return createJobDetail(CameraMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean tmsStationMetadataUpdateJobDetail() {
        return createJobDetail(TmsStationMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean weatherStationMetadataUpdateJobDetail() {
        return createJobDetail(WeatherStationMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean cameraStationsStatusMetadataUpdateJobDetail() {
        return createJobDetail(CameraStationsStatusMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean tmsStationsStatusMetadataUpdateJobDetail() {
        return createJobDetail(TmsStationsStatusMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean tmsStationSensorConstantsUpdateJobDetail() {
        return createJobDetail(TmsStationSensorConstantsMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean weatherStationsStatusMetadataUpdateJobDetail() {
        return createJobDetail(WeatherStationsStatusMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean locationMetadataUpdateJobDetail() { return createJobDetail(LocationMetadataUpdateJob.class); }

    @Bean
    public JobDetailFactoryBean forecastSectionCoordinatesMetadataUpdateJobDetail() {
        return createJobDetail(ForecastSectionCoordinatesMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean forecastSectionWeatherUpdateJobDetail() {
        return createJobDetail(ForecastSectionWeatherUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean datex2TrafficAlertMessageUpdateJobDetail() {
        return createJobDetail(Datex2TrafficAlertMessageUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean datex2RoadworksMessageUpdateJobDetail() {
        return createJobDetail(Datex2RoadworksMessageUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean datex2WeightRestrictionsMessageUpdateJobDetail() {
        return createJobDetail(Datex2WeightRestrictionsMessageUpdateJob.class);
    }

    @Bean
    public FactoryBean<? extends Trigger> cameraMetadataUpdateJobTrigger(final JobDetail cameraMetadataUpdateJobDetail,
                                                                   @Value("${cameraStationUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(cameraMetadataUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> tmsStationMetadataUpdateJobTrigger(final JobDetail tmsStationMetadataUpdateJobDetail,
                                                                       @Value("${tmsStationUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(tmsStationMetadataUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> tmsStationSensorConstantsUpdateJobTrigger(final JobDetail tmsStationSensorConstantsUpdateJobDetail,
                                                                                    @Value("${tmsStationSensorConstantsUpdateJob.cron}") final String scheduleExpression) {
        return createTrigger(tmsStationSensorConstantsUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> weatherStationMetadataUpdateJobTrigger(final JobDetail weatherStationMetadataUpdateJobDetail,
                                                                           @Value("${weatherStationUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(weatherStationMetadataUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> cameraStationsStatusMetadataUpdateJobTrigger(final JobDetail cameraStationsStatusMetadataUpdateJobDetail,
                                                                                 @Value("${roadStationsStatusUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(cameraStationsStatusMetadataUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> tmsStationsStatusMetadataUpdateJobTrigger(final JobDetail tmsStationsStatusMetadataUpdateJobDetail,
                                                                              @Value("${roadStationsStatusUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(tmsStationsStatusMetadataUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> weatherStationsStatusMetadataUpdateJobTrigger(final JobDetail weatherStationsStatusMetadataUpdateJobDetail,
                                                                                  @Value("${roadStationsStatusUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(weatherStationsStatusMetadataUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> locationsMetadataUpdateJobTrigger(final JobDetail locationMetadataUpdateJobDetail,
                                                                      @Value("${locationsMetadataUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(locationMetadataUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> forecastSectionCoordinatesMetadataUpdateJobTrigger(final JobDetail forecastSectionCoordinatesMetadataUpdateJobDetail,
                                                                                       @Value("${forecastSectionCoordinatesUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(forecastSectionCoordinatesMetadataUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> forecastSectionWeatherUpdateJobTrigger(final JobDetail forecastSectionWeatherUpdateJobDetail,
                                                                           @Value("${forecastSectionWeatherUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(forecastSectionWeatherUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> datex2TrafficAlertMessageUpdateJobTrigger(final JobDetail datex2TrafficAlertMessageUpdateJobDetail,
                                                                  @Value("${datex2TrafficAlertMessageUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(datex2TrafficAlertMessageUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> datex2RoadworksMessageUpdateJobTrigger(final JobDetail datex2RoadworksMessageUpdateJobDetail,
                                                                           @Value("${datex2RoadworksMessageUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(datex2RoadworksMessageUpdateJobDetail, scheduleExpression);
    }

    @Bean
    public FactoryBean<? extends Trigger> datex2WeightRestrictionsMessageUpdateJobTrigger(final JobDetail datex2WeightRestrictionsMessageUpdateJobDetail,
                                                                                    @Value("${datex2WeightRestrictionsMessageUpdateJob.frequency}") final String scheduleExpression) {
        return createTrigger(datex2WeightRestrictionsMessageUpdateJobDetail, scheduleExpression);
    }


    private static JobDetailFactoryBean createJobDetail(final Class jobClass) {
        final JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        // job has to be durable to be stored in DB:
        factoryBean.setDurability(true);
        // In case of job executing during the time of a hard shutdown, it will be re-executed when the scheduler is started again
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    private FactoryBean<? extends Trigger> createTrigger(final JobDetail jobDetail, final String expression) {
        try {
            // Try first to create interval trigger and fallback to cron
            long intervalMs = Long.parseLong(expression);
            return  createRepeatingTrigger(jobDetail, intervalMs);
        } catch (NumberFormatException nfe) { // cron expression
            return createCronTrigger(jobDetail, expression);
        }
    }

    /**
     * @param jobDetail
     * @param repeatIntervalMs how often is job repeated in ms. If time <= 0 it's triggered only once.
     * @return
     */
    private SimpleTriggerFactoryBean createRepeatingTrigger(final JobDetail jobDetail, final long repeatIntervalMs) {

        final String jobName = jobDetail.getJobClass().getSimpleName();
        final String jobEnabledProperty = environment.getProperty("digitraffic.job." + jobName + ".enabled");

        final boolean jobEnabled = jobEnabledProperty == null || !"false".equalsIgnoreCase(jobEnabledProperty);


        final SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setRepeatInterval(repeatIntervalMs);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        // In case of misfire: The first misfired execution is run immediately, remaining are discarded.
        // Next execution happens after desired interval. Effectively the first execution time is moved to current time.
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT);

        if (!jobEnabled) {
            factoryBean.setStartTime(QUARTZ_MAX_DATE);
        } else {
            // Delay first execution 5 seconds
            factoryBean.setStartDelay(5000L);
        }
        log.info("Created SimpleTrigger for jobName={} enabled={} with repeatIntervalMs={}", jobName, jobEnabled, repeatIntervalMs);
        return factoryBean;
    }

    /**
     * @param jobDetail
     * @param cronExpression Cron expression for trigger schedule.
     * @return
     */
    private CronTriggerFactoryBean createCronTrigger(final JobDetail jobDetail, final String cronExpression) {

        final String jobName = jobDetail.getJobClass().getSimpleName();
        final String jobEnabledProperty = environment.getProperty("digitraffic.job." + jobName + ".enabled");

        final boolean jobEnabled = jobEnabledProperty == null || !"false".equalsIgnoreCase(jobEnabledProperty);

        final CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (!jobEnabled) {
            factoryBean.setStartTime(QUARTZ_MAX_DATE);
        }
        log.info("Created CronTrigger for jobName={} enabled={} with expression={}", jobName, jobEnabled, cronExpression);
        return factoryBean;
    }
}
