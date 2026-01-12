package fi.livi.digitraffic.tie.conf.amazon;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AmazonS3ClientTestConfiguration {

    @Bean
    public S3Client s3Client() {
        return Mockito.mock(S3Client.class);
    }

    @Bean
    public S3AsyncClient s3AsyncClient() {
        return Mockito.mock(S3AsyncClient.class);
    }
}
