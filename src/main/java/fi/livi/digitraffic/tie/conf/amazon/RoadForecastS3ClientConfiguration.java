package fi.livi.digitraffic.tie.conf.amazon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * S3 client for the road forecast bucket. Separate from the default client,
 * as the bucket is read with its own access key. The client is created only if bucketName, accessKeyId and
 * secretAccessKey are all set; otherwise forecast reading is disabled (see ForecastSectionDataUpdaterV2).
 */
@ConditionalOnExpression("'${config.test}' != 'true' " +
                         "and !'${dt.amazon.s3.roadforecast.bucketName:}'.isBlank() " +
                         "and !'${dt.amazon.s3.roadforecast.accessKeyId:}'.isBlank() " +
                         "and !'${dt.amazon.s3.roadforecast.secretAccessKey:}'.isBlank()")
@Configuration
public class RoadForecastS3ClientConfiguration {

    public static final String BEAN_NAME = "roadForecastS3Client";

    @Bean(BEAN_NAME)
    public S3Client roadForecastS3Client(
            final RoadForecastS3Properties properties,
            @Value("${dt.amazon.s3.roadforecast.accessKeyId}") final String accessKeyId,
            @Value("${dt.amazon.s3.roadforecast.secretAccessKey}") final String secretAccessKey) {
        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
            .region(Region.of(properties.getS3Region()))
            .build();
    }
}
