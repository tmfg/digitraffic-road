package fi.livi.digitraffic.tie.conf;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javax.sql.DataSource;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.quartz.utils.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
    @QuartzDataSource
    public DataSource quartzDataSource(final @Value("${road.datasource.url}") String url,
        final @Value("${road.datasource.username}") String username,
        final @Value("${road.datasource.password}") String password,
        final @Value("quartz.enabled") String quartz) {
        log.error("starting quartz:" + quartz);

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(4);

        config.setMaxLifetime(570000);
        config.setIdleTimeout(500000);
        config.setConnectionTimeout(60000);
        config.setPoolName("quartz_pool");

        // register mbeans for debug
        config.setRegisterMbeans(true);

        // Auto commit must be true for Quartz
        config.setAutoCommit(true);

        return new HikariDataSource(config);
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(final DataSource quartzDataSource,
                                                     final JobFactory jobFactory,
                                                     final Optional<List<Trigger>> triggerBeans) throws IOException {
        final SchedulerFactoryBean factory = new SchedulerFactoryBean() {
            @Override
            protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) throws SchedulerException {
                Scheduler scheduler = super.createScheduler(schedulerFactory, schedulerName);
                triggerBeans.get().forEach(t -> {
                    try {
                        // If trigger is missing but job exists in db, the trigger will not be created. Delete the job to recreate it and the trigger again.
                        if (scheduler.checkExists(t.getJobKey()) && !scheduler.checkExists(t.getKey()) && Key.DEFAULT_GROUP.equals(t.getKey().getGroup())) {
                            log.info("Delete orphan job={}", t.getJobKey());
                            scheduler.deleteJob(t.getJobKey());
                        }
                    } catch (SchedulerException e) {
                        log.error("Deleting job=" + t.getJobKey() + " with missing trigger failed", e);
                    }
                });
                return scheduler;
            }
        };
        // this allows to update triggers in DB when updating settings in config file:
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(quartzDataSource);
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
    public SimpleTriggerFactoryBean cameraMetadataUpdateJobTrigger(final JobDetail cameraMetadataUpdateJobDetail,
                                                                   @Value("${cameraStationUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(cameraMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean tmsStationMetadataUpdateJobTrigger(final JobDetail tmsStationMetadataUpdateJobDetail,
                                                                       @Value("${tmsStationUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(tmsStationMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean weatherStationMetadataUpdateJobTrigger(final JobDetail weatherStationMetadataUpdateJobDetail,
                                                                           @Value("${weatherStationUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(weatherStationMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean cameraStationsStatusMetadataUpdateJobTrigger(final JobDetail cameraStationsStatusMetadataUpdateJobDetail,
                                                                                 @Value("${roadStationsStatusUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(cameraStationsStatusMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean tmsStationsStatusMetadataUpdateJobTrigger(final JobDetail tmsStationsStatusMetadataUpdateJobDetail,
                                                                              @Value("${roadStationsStatusUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(tmsStationsStatusMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean weatherStationsStatusMetadataUpdateJobTrigger(final JobDetail weatherStationsStatusMetadataUpdateJobDetail,
                                                                                  @Value("${roadStationsStatusUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(weatherStationsStatusMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean locationsMetadataUpdateJobTrigger(final JobDetail locationMetadataUpdateJobDetail,
                                                                      @Value("${locationsMetadataUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(locationMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean forecastSectionCoordinatesMetadataUpdateJobTrigger(final JobDetail forecastSectionCoordinatesMetadataUpdateJobDetail,
                                                                                       @Value("${forecastSectionCoordinatesUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(forecastSectionCoordinatesMetadataUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean forecastSectionWeatherUpdateJobTrigger(final JobDetail forecastSectionWeatherUpdateJobDetail,
                                                                           @Value("${forecastSectionWeatherUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(forecastSectionWeatherUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean datex2TrafficAlertMessageUpdateJobTrigger(final JobDetail datex2TrafficAlertMessageUpdateJobDetail,
                                                                  @Value("${datex2TrafficAlertMessageUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(datex2TrafficAlertMessageUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean datex2RoadworksMessageUpdateJobTrigger(final JobDetail datex2RoadworksMessageUpdateJobDetail,
        @Value("${datex2RoadworksMessageUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(datex2RoadworksMessageUpdateJobDetail, frequency);
    }

    @Bean
    public SimpleTriggerFactoryBean datex2WeightRestrictionsMessageUpdateJobTrigger(final JobDetail datex2WeightRestrictionsMessageUpdateJobDetail,
        @Value("${datex2WeightRestrictionsMessageUpdateJob.frequency}") final long frequency) {
        return createRepeatingTrigger(datex2WeightRestrictionsMessageUpdateJobDetail, frequency);
    }


    private static JobDetailFactoryBean createJobDetail(final Class jobClass) {
        final JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        // job has to be durable to be stored in DB:
        factoryBean.setDurability(true);
        return factoryBean;
    }

    /**
     * @param jobDetail
     * @param repeatIntervalMs how often is job repeated in ms. If time <= 0 it's triggered only once.
     * @return
     */
    private SimpleTriggerFactoryBean createRepeatingTrigger(final JobDetail jobDetail, final long repeatIntervalMs) {

        final String jobName = jobDetail.getJobClass().getSimpleName();
        final String jobEnabledProperty = environment.getProperty("quartz." + jobName + ".enabled");

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
        log.info("Created Trigger for jobName={} enabled={}", jobName, jobEnabled);
        return factoryBean;
    }
}
