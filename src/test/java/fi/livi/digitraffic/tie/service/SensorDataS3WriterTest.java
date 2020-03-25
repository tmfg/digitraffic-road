package fi.livi.digitraffic.tie.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.conf.amazon.SpringLocalstackDockerRunnerWithVersion;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;
import xyz.fabiano.spring.localstack.LocalstackService;
import xyz.fabiano.spring.localstack.annotation.SpringLocalstackProperties;

@RunWith(SpringLocalstackDockerRunnerWithVersion.class)
@SpringLocalstackProperties(services = { LocalstackService.S3 }, region = "eu-west-1", randomPorts = false)
@TestPropertySource(properties = { "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN" })
public class SensorDataS3WriterTest extends AbstractDaemonTest {
    public static final Logger log=LoggerFactory.getLogger(SensorDataS3WriterTest.class);

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private SensorDataS3Writer writer;

    @Autowired
    private SensorValueHistoryRepository repository;

    @Value("${dt.amazon.s3.sensordata.bucketName}")
    private String bucketName;

    private int count = 0;

    @Before
    public void initS3BucketForSensorData(){
        log.info("Init S3 Bucket {} with S3: {}, {}", bucketName, amazonS3);

        if (amazonS3.doesBucketExistV2(bucketName)) {
            log.info("Bucket {} exists already", bucketName);
        } else {
            amazonS3.createBucket(bucketName);
            log.info("Bucket {} created", bucketName);
        }

        ZonedDateTime time = ZonedDateTime.now();
        int min = time.getMinute();

        // Init same db-content
        SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .setReferenceTime(time)
            .buildRandom(10, 10, 10, 0, min)
            .buildRandom(50, 10, 10, min + 1, min + 61)
            .save();

        count = builder.getElementCountAt(1);
    }

    @Test
    public void s3Bucket() {
        ZonedDateTime now = ZonedDateTime.now();

        int sum = writer.writeSensorData(now.minusHours(1).truncatedTo(ChronoUnit.HOURS), now.truncatedTo(ChronoUnit.HOURS));

        Assert.assertEquals("element count mismatch", count, sum);

        // Disabled
        /**
        ObjectListing list = amazonS3.listObjects(bucketName);

        String objectName = list.getObjectSummaries().get(0).getKey();

        log.info("Read object {} from {}", objectName, bucketName);
        S3Object s3Object = amazonS3.getObject(bucketName, objectName);

        Assert.assertNotNull("S3 object not found", s3Object);
        */
        //TODO! Check object is .zip and document is .csv and actual content is readable
    }
}

