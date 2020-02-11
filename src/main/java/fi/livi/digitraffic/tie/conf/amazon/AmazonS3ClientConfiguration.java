package fi.livi.digitraffic.tie.conf.amazon;

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

    @Bean
    public AmazonS3 amazonS3(final @Value("${dt.amazon.s3.weathercam.userAccessKey}") String accessKey,
                             final @Value("${dt.amazon.s3.weathercam.userSecretKey}") String secretKey,
                             final @Value("${dt.amazon.s3.weathercam.region}") String region) {

        final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        final AmazonS3ClientBuilder builder =
            AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region);
        return builder.build();
    }
}
