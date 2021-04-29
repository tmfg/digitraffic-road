package fi.livi.digitraffic.tie.conf.amazon;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@ConditionalOnNotWebApplication
@Configuration
public class AmazonS3ClientTestConfiguration {

    @ConditionalOnProperty(name = "testcontainers.disabled", havingValue = "true")
    @Configuration
    public static class MockConfiguration {
        @Bean
        public AmazonS3 amazonS3() {
            return Mockito.mock(AmazonS3.class);
        }
    }

    @Testcontainers
    @Configuration
    @ConditionalOnExpression("'${testcontainers.disabled}' != 'true'")
    public static class LocalStackConfiguration {
        @Container
        private static final LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.10"))
                .withServices(S3)
                .withEnv("DEFAULT_REGION", "eu-central-1");

        @Bean
        public AmazonS3 amazonS3() {
            if(!localStack.isRunning()) {
                localStack.start();
            }

            return AmazonS3ClientBuilder.standard()
                .withCredentials(localStack.getDefaultCredentialsProvider())
                .withEndpointConfiguration(localStack.getEndpointConfiguration(S3))
                .build();
        }

    }
}
