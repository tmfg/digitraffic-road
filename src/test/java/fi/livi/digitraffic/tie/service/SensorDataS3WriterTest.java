package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.opencsv.bean.CsvToBeanBuilder;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithS3;
import fi.livi.digitraffic.tie.conf.amazon.SensorDataS3Properties;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dto.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;

@TestPropertySource(properties = { "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN" })
public class SensorDataS3WriterTest extends AbstractDaemonTestWithS3 {
    public static final Logger log=LoggerFactory.getLogger(SensorDataS3WriterTest.class);

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private SensorDataS3Writer writer;

    @Autowired
    private SensorValueHistoryRepository repository;

    @Autowired
    private SensorDataS3Properties sensorDataS3Properties;

    private SensorValueHistoryBuilder builder;

    @BeforeEach
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
        builder = new SensorValueHistoryBuilder(repository, log)
            .setReferenceTime(time)
            .buildRandom(10, 10, 10, 0, min)
            .buildRandom(50, 10, 10, min + 1, min + 61)
            .save();
    }

    protected void createS3Object(final ZonedDateTime time) {
        String filename = sensorDataS3Properties.getFileStorageName(time);

        String dummyContent = "Lorem ipsum";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/zip");
        metadata.setContentLength(dummyContent.getBytes().length);

        PutObjectResult result = amazonS3.putObject(sensorDataS3Properties.getS3BucketName(),
            filename,
            new ByteArrayInputStream(dummyContent.getBytes()),
            metadata);

        log.info("Store object: {}, result: {}", filename, result.toString());
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

        int origCount = builder.getElementCountAt(1);
        int sum = writer.writeSensorData(from, to);

        assertEquals(origCount, sum, "element count mismatch");

        //repository.findAll().stream().forEach(item -> log.info("item: {}, measured: {}", item.getRoadStationId(), item.getMeasuredTime()));
        // Check S3 object

        ObjectListing list = amazonS3.listObjects(sensorDataS3Properties.getS3BucketName());

        assertFalse(list.getObjectSummaries().isEmpty(), "No elements");

        String objectName = list.getObjectSummaries().get(0).getKey();

        log.info("Read object {} from {}", objectName, sensorDataS3Properties.getS3BucketName());
        S3Object s3Object = amazonS3.getObject(sensorDataS3Properties.getS3BucketName(), objectName);

        assertNotNull(s3Object, "S3 object not found");
/**
        try {
            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(s3Object.getObjectContent().readAllBytes()), new File("temppi.zip"));
        } catch (Exception e) {
            e.printStackTrace();
        }
 */
        try (ByteArrayInputStream bis = new ByteArrayInputStream(s3Object.getObjectContent().readAllBytes());
            ZipInputStream in = new ZipInputStream(bis)) {
            // Get entry
            ZipEntry entry = in.getNextEntry();

            log.info("entry {}", entry.getName());

            List<WeatherSensorValueHistoryDto> items = new CsvToBeanBuilder<WeatherSensorValueHistoryDto>(new InputStreamReader(in))
                .withType(WeatherSensorValueHistoryDto.class)
                .withSeparator(',')
                .build()
                .parse();

            in.closeEntry();

            log.info("Gotta items {}", items);
        } catch (Exception e) {
            log.error("zip error:", e);
            //Assert.fail("failed to process zip: " + objectName);
        }
        //TODO! Check object is .zip and document is .csv and actual content is readable
    }

    @Test
    public void historyCap() {
        ZonedDateTime now = ZonedDateTime.now();
        // Current time window
        ZonedDateTime from = now.minusHours(1).truncatedTo(ChronoUnit.HOURS);

        cleanBucket();

        int minusHours = RandomUtils.nextInt(3, 7);

        // Create test file (now - n hours)
        createS3Object(now.minusHours(minusHours));

        // Test missing files update
        assertTrue(writer.updateSensorDataS3History(from), "History update failure");

        // No updates if no missing files
        //Assert.assertFalse("Invalid history update", writer.updateSensorDataS3History(from));
    }
}

