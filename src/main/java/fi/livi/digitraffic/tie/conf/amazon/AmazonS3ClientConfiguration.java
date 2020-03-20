package fi.livi.digitraffic.tie.conf.amazon;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@ConditionalOnExpression("'${config.test}' != 'true'")
@ConditionalOnNotWebApplication
@Configuration
public class AmazonS3ClientConfiguration {
    @Qualifier("weathercamS3")
    @Bean
    public AmazonS3 weathercamS3(final @Value("${dt.amazon.s3.weathercam.userAccessKey}") String accessKey,
                                    final @Value("${dt.amazon.s3.weathercam.userSecretKey}") String secretKey,
                                    final @Value("${dt.amazon.s3.weathercam.region}") String region) {
        return build(accessKey, secretKey, region);
    }

    @Qualifier("sensorDataS3")
    @Bean
    public AmazonS3 sensorDataS3(final @Value("${dt.amazon.s3.sensordata.userAccessKey}") String accessKey,
                                    final @Value("${dt.amazon.s3.sensordata.userSecretKey}") String secretKey,
                                    final @Value("${dt.amazon.s3.sensordata.region}") String region) {
        return build(accessKey, secretKey, region);
    }

    private AmazonS3 build(final String accessKey, final String secretKey, final String region) {
        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .withRegion(region)
            .build();
    }
}
