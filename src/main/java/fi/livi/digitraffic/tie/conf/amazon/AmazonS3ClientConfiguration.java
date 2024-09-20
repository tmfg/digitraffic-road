package fi.livi.digitraffic.tie.conf.amazon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@ConditionalOnExpression("'${config.test}' != 'true'")
@ConditionalOnNotWebApplication
@Configuration
public class AmazonS3ClientConfiguration {

    @Bean
    public AmazonS3 s3Client(final @Value("${dt.amazon.s3.region}") String region) {
        return build(region);
    }

    private AmazonS3 build(final String region) {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(region)
                .build();
    }
}
