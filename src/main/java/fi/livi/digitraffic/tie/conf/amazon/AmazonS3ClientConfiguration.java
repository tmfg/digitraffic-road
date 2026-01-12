package fi.livi.digitraffic.tie.conf.amazon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.StandardRetryStrategy;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Duration;

@ConditionalOnExpression("'${config.test}' != 'true'")
@Configuration
public class AmazonS3ClientConfiguration {

    @Bean
    public S3Client s3Client(final @Value("${dt.amazon.s3.region}") String region) {
        return build(region);
    }

    private S3Client build(final String region) {
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region))
                .build();
    }

    @Bean
    public S3AsyncClient s3AsyncClient(final WeathercamS3Properties weathercamS3Properties) {
        final SdkAsyncHttpClient httpClient =
                NettyNioAsyncHttpClient.builder()
                        .maxConcurrency(80)
                        .maxPendingConnectionAcquires(200)
                        .connectionTimeout(Duration.ofSeconds(5))
                        .readTimeout(Duration.ofSeconds(45))
                        .writeTimeout(Duration.ofSeconds(45))
                        .build();

        final StandardRetryStrategy retryStrategy = StandardRetryStrategy.builder()
                .maxAttempts(3) // 1 initial + 2 retries
                .build();

        final ClientOverrideConfiguration overrideConfig =
                ClientOverrideConfiguration.builder()
                        .apiCallAttemptTimeout(Duration.ofSeconds(40))
                        .apiCallTimeout(Duration.ofSeconds(80))
                        .retryStrategy(retryStrategy)
                        .build();

        return S3AsyncClient.builder()
                .region(Region.of(weathercamS3Properties.s3Region))
                .httpClient(httpClient)
                .overrideConfiguration(overrideConfig)
                .build();
    }
}
