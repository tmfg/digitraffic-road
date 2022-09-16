package fi.livi.digitraffic.tie.conf.amazon;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.s3.AmazonS3;

@ConditionalOnNotWebApplication
@Configuration
public class AmazonS3ClientTestConfiguration {

    @Bean
    public AmazonS3 amazonS3() {
        return Mockito.mock(AmazonS3.class);
    }

}
