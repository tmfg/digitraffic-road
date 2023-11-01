package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.opencsv.bean.CsvToBeanBuilder;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.conf.amazon.SensorDataS3Properties;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dto.weather.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;

public class SensorDataS3WriterTest extends AbstractDaemonTest {
    public static final Logger log=LoggerFactory.getLogger(SensorDataS3WriterTest.class);

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private SensorDataS3Writer writer;

    @Autowired
    private SensorValueHistoryRepository repository;

    private final SensorDataS3Properties sensorDataS3Properties = new SensorDataS3Properties("fakeTestBucket");

    private SensorValueHistoryBuilder builder;

    protected void initDBContent(final ZonedDateTime time) {
        final int min = time.getMinute();

        // Init same db-content
        builder = new SensorValueHistoryBuilder(repository, log)
            .truncate()
            .setReferenceTime(time)
            .buildRandom(10, 10, 10, 0, min)
            .buildRandom(50, 10, 10, min + 1, min + 61)
            .save();
    }

    @Disabled("ks. DPO-1835")
    @Test
    public void s3Bucket() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime to = now.truncatedTo(ChronoUnit.HOURS);
        final ZonedDateTime from = to.minusHours(1);

        initDBContent(now);

        sensorDataS3Properties.setRefTime(from);

        final int origCount = builder.getElementCountAt(1);
        final int sum = writer.writeSensorData(from, to);

        assertEquals(origCount, sum, "element count mismatch");

        //repository.findAll().stream().forEach(item -> log.info("item: {}, measured: {}", item.getRoadStationId(), item.getMeasuredTime()));
        // Check S3 object

        final ObjectListing list = amazonS3.listObjects(sensorDataS3Properties.getS3BucketName());

        assertFalse(list.getObjectSummaries().isEmpty(), "No elements");

        final String objectName = list.getObjectSummaries().get(0).getKey();

        log.info("Read object {} from {}", objectName, sensorDataS3Properties.getS3BucketName());
        final S3Object s3Object = amazonS3.getObject(sensorDataS3Properties.getS3BucketName(), objectName);

        assertNotNull(s3Object, "S3 object not found");
/**
        try {
            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(s3Object.getObjectContent().readAllBytes()), new File("temppi.zip"));
        } catch (Exception e) {
            e.printStackTrace();
        }
 */
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(s3Object.getObjectContent().readAllBytes());
            final ZipInputStream in = new ZipInputStream(bis)) {
            // Get entry
            final ZipEntry entry = in.getNextEntry();

            log.info("entry {}", entry.getName());

            final List<WeatherSensorValueHistoryDto> items = new CsvToBeanBuilder<WeatherSensorValueHistoryDto>(new InputStreamReader(in))
                .withType(WeatherSensorValueHistoryDto.class)
                .withSeparator(',')
                .build()
                .parse();

            in.closeEntry();

            log.info("Gotta items {}", items);
        } catch (final Exception e) {
            log.error("zip error:", e);
            //Assert.fail("failed to process zip: " + objectName);
        }
        //TODO! Check object is .zip and document is .csv and actual content is readable
    }

    @Test
    public void historyCap() {
        ReflectionTestUtils.setField(writer, "s3Properties", sensorDataS3Properties);

        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime currentTimeWindow = now.minusHours(1).truncatedTo(ChronoUnit.HOURS);

        ZonedDateTime windowLoop = currentTimeWindow.minusHours(23);
        final List<ZonedDateTime> missingWindows = new ArrayList<>();

        // Create some test keys for missing time windows.
        while (missingWindows.size() < 8) {
            if (windowLoop.getHour() % 3 == 0) {
                missingWindows.add(windowLoop);
            }
            windowLoop = windowLoop.plusHours(1);
        }

        Assertions.assertEquals(8, missingWindows.size());
        missingWindows.forEach(missingWindow -> {
            final int i = writer.writeSensorData(missingWindow, missingWindow.plusHours(1));
            Assertions.assertTrue(i > -1, "History update failure");
        });
    }
}
