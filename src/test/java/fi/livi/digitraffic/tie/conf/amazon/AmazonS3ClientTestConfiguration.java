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

import xyz.fabiano.spring.localstack.legacy.LocalstackDocker;
import xyz.fabiano.spring.localstack.support.AmazonDockerClientsHolder;

@ConditionalOnNotWebApplication
@Configuration
public class AmazonS3ClientTestConfiguration {
    @Bean
    public LocalstackDocker localstackDocker() {
        return LocalstackDocker.getLocalstackDocker();
    }

    @Bean
    public AmazonDockerClientsHolder amazonDockerClientsHolder(final LocalstackDocker localstackDocker) {
        return new AmazonDockerClientsHolder(localstackDocker);
    }

//    @ConditionalOnExpression("'${spring.localstack.enabled}' == 'false'")
    @Bean
    public AmazonS3 amazonS3(final @Value("${dt.amazon.s3.weathercam.region}") String region) {

        final AWSCredentials credentials = new BasicAWSCredentials( "dummy",  "dummy");
        final AmazonS3ClientBuilder builder =
            AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region);
        return builder.build();
    }
}
