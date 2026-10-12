package fi.livi.digitraffic.tie.conf.amazon;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.s3.S3Client;

class RoadForecastS3ClientConfigurationTest {

    private static final String PREFIX = "dt.amazon.s3.roadforecast.";

    @Configuration
    @EnableConfigurationProperties(RoadForecastS3Properties.class)
    static class PropertiesOnly {}

    private static final String[] ALL_SETTINGS = {
        PREFIX + "bucketName=bucket", PREFIX + "region=eu-north-1",
        PREFIX + "accessKeyId=key", PREFIX + "secretAccessKey=secret", "config.test=false" };

    private final ApplicationContextRunner daemonRunner = new ApplicationContextRunner()
        .withUserConfiguration(PropertiesOnly.class, RoadForecastS3ClientConfiguration.class);

    @Test
    void clientIsCreatedInDaemonWhenAllSettingsAreSet() {
        daemonRunner.withPropertyValues(ALL_SETTINGS)
            .run(ctx -> assertThat(ctx).hasSingleBean(S3Client.class));
    }

    @Test
    void clientIsNotCreatedInWebApplication() {
        new WebApplicationContextRunner()
            .withUserConfiguration(PropertiesOnly.class, RoadForecastS3ClientConfiguration.class)
            .withPropertyValues(ALL_SETTINGS)
            .run(ctx -> {
                assertThat(ctx).hasNotFailed();
                assertThat(ctx).doesNotHaveBean(S3Client.class);
            });
    }

    @Test
    void clientIsNotCreatedWhenRegionIsMissing() {
        daemonRunner.withPropertyValues(PREFIX + "bucketName=bucket", PREFIX + "accessKeyId=key",
                PREFIX + "secretAccessKey=secret", "config.test=false")
            .run(ctx -> {
                assertThat(ctx).hasNotFailed();
                assertThat(ctx).doesNotHaveBean(S3Client.class);
            });
    }

    @Test
    void clientIsNotCreatedWhenCredentialsAreMissing() {
        daemonRunner.withPropertyValues(PREFIX + "bucketName=bucket", PREFIX + "region=eu-north-1", "config.test=false")
            .run(ctx -> {
                assertThat(ctx).hasNotFailed();
                assertThat(ctx).doesNotHaveBean(S3Client.class);
            });
    }

    @Test
    void clientIsNotCreatedInTestConfig() {
        daemonRunner.withPropertyValues(ALL_SETTINGS).withPropertyValues("config.test=true")
            .run(ctx -> assertThat(ctx).doesNotHaveBean(S3Client.class));
    }
}
