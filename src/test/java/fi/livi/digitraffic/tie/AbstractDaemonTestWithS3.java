package fi.livi.digitraffic.tie;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;

import fi.livi.digitraffic.tie.conf.amazon.SpringLocalstackDockerRunnerWithVersion;
import xyz.fabiano.spring.localstack.LocalstackService;
import xyz.fabiano.spring.localstack.annotation.SpringLocalstackProperties;

@RunWith(SpringLocalstackDockerRunnerWithVersion.class)
@SpringLocalstackProperties(services = { LocalstackService.S3 }, region = "eu-west-1", randomPorts = false)
public abstract class AbstractDaemonTestWithS3 extends AbstractDaemonTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractDaemonTestWithS3.class);

    @Autowired
    protected AmazonS3 amazonS3;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    protected String weathercamBucketName;

    @Before
    public void initS3BucketForWeatherCam() {

        log.info("Init versioned S3 Bucket {} with S3: {}", weathercamBucketName, amazonS3);

        if (amazonS3.doesBucketExistV2(weathercamBucketName)) {
            log.info("Bucket {} exists already", weathercamBucketName);
        } else {
            amazonS3.createBucket(weathercamBucketName);
            log.info("Bucket {} created", weathercamBucketName);

            // Enable versioning on the bucket.
            BucketVersioningConfiguration configuration =
                new BucketVersioningConfiguration().withStatus("Enabled");

            SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest =
                new SetBucketVersioningConfigurationRequest(weathercamBucketName, configuration);

            amazonS3.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest);

            // 2. Get bucket versioning configuration information.
            BucketVersioningConfiguration conf = amazonS3.getBucketVersioningConfiguration(weathercamBucketName);
            log.info("Bucket {} versioning configuration status: {}", weathercamBucketName, conf.getStatus());
        }
    }
}
