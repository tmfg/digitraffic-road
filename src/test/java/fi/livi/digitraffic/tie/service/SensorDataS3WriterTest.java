package fi.livi.digitraffic.tie.service;

import java.io.ByteArrayInputStream;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.conf.amazon.SensorDataS3Properties;
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

    @Autowired
    SensorDataS3Properties sensorDataS3Properties;

    private int count = 0;

    @Before
    public void initS3BucketForSensorData() {
        log.info("Init S3 Bucket {} with S3: {}, {}", sensorDataS3Properties.getS3BucketName(), amazonS3);

        if (amazonS3.doesBucketExistV2(sensorDataS3Properties.getS3BucketName())) {
            log.info("Bucket {} exists already", sensorDataS3Properties.getS3BucketName());
        } else {
            amazonS3.createBucket(sensorDataS3Properties.getS3BucketName());
            log.info("Bucket {} created", sensorDataS3Properties.getS3BucketName());
        }
    }

    protected void initDBContent(final ZonedDateTime time) {
        int min = time.getMinute();

        // Init same db-content
        SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .setReferenceTime(time)
            .buildRandom(10, 10, 10, 0, min)
            .buildRandom(50, 10, 10, min + 1, min + 61)
            .save();

        count = builder.getElementCountAt(1);
    }

    protected void createS3Object(final ZonedDateTime time) {
        String filename = sensorDataS3Properties.getFilename(time, ".zip");
        String storename = sensorDataS3Properties.getFileStorageName(time, filename);

        String dummyContent = "Lorem ipsum";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/zip");
        metadata.setContentLength(dummyContent.getBytes().length);

        PutObjectResult result = amazonS3.putObject(sensorDataS3Properties.getS3BucketName(),
            storename,
            new ByteArrayInputStream(dummyContent.getBytes()),
            metadata);

        log.info("Store object: {}, result: {}", storename, result.toString());
    }

    protected void cleanBucket() {
        ListObjectsV2Result result = amazonS3.listObjectsV2(sensorDataS3Properties.getS3BucketName());

        if (!result.getObjectSummaries().isEmpty()) {
            result.getObjectSummaries().stream().forEach(elem -> {
                amazonS3.deleteObject(elem.getBucketName(), elem.getKey());
            });
        }
    }

    @Test
    public void s3Bucket() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime to = now.truncatedTo(ChronoUnit.HOURS);
        ZonedDateTime from = to.minusHours(1);

        initDBContent(now);

        sensorDataS3Properties.setRefTime(from);

        int sum = writer.writeSensorData(from, to);

        Assert.assertEquals("element count mismatch", count, sum);

        ObjectListing list = amazonS3.listObjects(sensorDataS3Properties.getS3BucketName());

        Assert.assertFalse("No elements", list.getObjectSummaries().isEmpty());

        String objectName = list.getObjectSummaries().get(0).getKey();

        log.info("Read object {} from {}", objectName, sensorDataS3Properties.getS3BucketName());
        S3Object s3Object = amazonS3.getObject(sensorDataS3Properties.getS3BucketName(), objectName);

        Assert.assertNotNull("S3 object not found", s3Object);

        //TODO! Check object is .zip and document is .csv and actual content is readable
    }

    @Test
    public void historyCap() {
        ZonedDateTime now = ZonedDateTime.now();
        // Current time window
        ZonedDateTime from = now.minusHours(1).truncatedTo(ChronoUnit.HOURS);

        cleanBucket();

        // Initial case when bucket is empty
        Assert.assertFalse("No history update when initial state", writer.updateSensorDataS3History(from));

        int minusHours = RandomUtils.nextInt(3, 7);

        // Create test file (now - n hours)
        createS3Object(now.minusHours(minusHours));

        // Test missing files update
        Assert.assertTrue("Missing files update failed", writer.updateSensorDataS3History(from));

        // No updates if no missing files
        Assert.assertFalse("Invalid history update", writer.updateSensorDataS3History(from));
    }
}

