package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.AmazonS3;
import com.opencsv.bean.CsvToBeanBuilder;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.conf.amazon.SensorDataS3Properties;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dto.weather.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;

public class SensorDataS3WriterTest extends AbstractDaemonTest {
    public static final Logger log=LoggerFactory.getLogger(SensorDataS3WriterTest.class);

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private SensorDataS3Writer writer;

    @Autowired
    private SensorValueHistoryRepository repository;

    @Autowired
    private SensorDataS3Properties sensorDataS3Properties;

    @Autowired
    private RoadStationService roadStationService;

    private SensorValueHistoryBuilder builder;

    @Captor
    ArgumentCaptor<InputStream> inputStreamArgumentCaptor;

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

    @Test
    public void s3Bucket() throws IOException {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime to = now.truncatedTo(ChronoUnit.HOURS);
        final ZonedDateTime from = to.minusHours(1);

        initDBContent(now);

        sensorDataS3Properties.setRefTime(from);

        final int origCount = builder.getElementCountAt(1);
        final int sum = writer.writeSensorData(from, to);

        assertEquals(origCount, sum, "element count mismatch");

        // Check S3 object
        Mockito.verify(amazonS3, Mockito.times(1))
            .putObject(
                Mockito.eq(sensorDataS3Properties.getS3BucketName()),
                Mockito.eq(sensorDataS3Properties.getFileStorageName()),
                inputStreamArgumentCaptor.capture(),
                Mockito.any());
        final byte[] zipBytes = inputStreamArgumentCaptor.getValue().readAllBytes();


//        For debugging, write zip file to project root
//        try {
//            FileUtils.copyInputStreamToFile(new ByteArrayInputStream(zipBytes), new File("temppi.zip"));
//        } catch (final Exception e) {
//            e.printStackTrace();
//        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(zipBytes);
            final ZipInputStream in = new ZipInputStream(bis)) {
            // Get entry
            final ZipEntry entry = in.getNextEntry();

            log.info("entry {}", Objects.requireNonNull(entry).getName());

            final List<WeatherSensorValueHistoryDto> items = new CsvToBeanBuilder<WeatherSensorValueHistoryDto>(new InputStreamReader(in))
                .withType(WeatherSensorValueHistoryDto.class)
                .withSeparator(';')
                .build()
                .parse();

            in.closeEntry();

            assertEquals(origCount, items.size());
            //Convert db data to truncated as csv dates are truncated and order by measured time & sensor id
            final List<SensorValueHistory> expectedHistory =
                // This contains also data outside from-to time frame, so we filter those out
                builder.getGeneratedHistory().stream()
                    .peek(h -> h.setMeasuredTime(h.getMeasuredTime().truncatedTo(ChronoUnit.SECONDS))) // CSV is truncated
                    .filter(h -> h.getMeasuredTime().toEpochSecond() >= from.toEpochSecond() && h.getMeasuredTime().toEpochSecond() < to.toEpochSecond())
                    .sorted(Comparator.comparing(SensorValueHistory::getMeasuredTime).thenComparing(SensorValueHistory::getSensorId).thenComparing(SensorValueHistory::getSensorValue))
                    .toList();
            // Same ordering for csv
            final List<WeatherSensorValueHistoryDto> actualHistory = items.stream()
                .sorted(Comparator.comparing(WeatherSensorValueHistoryDto::getMeasuredTime).thenComparing(WeatherSensorValueHistoryDto::getSensorId).thenComparing(WeatherSensorValueHistoryDto::getSensorValue))
                .toList();
            assertHistory(expectedHistory, actualHistory);
        } catch (final Exception e) {
            log.error("zip error:", e);
            Assertions.fail("failed to process zip: " + sensorDataS3Properties.getFileStorageName());
        }
    }

    private void assertHistory(final List<SensorValueHistory> expectedHistory, final List<WeatherSensorValueHistoryDto> actualHistory) {
        assertEquals(expectedHistory.size(), actualHistory.size());
        final Map<Long, Long> roadStationNaturalIdMaps = roadStationService.getNaturalIdMappings(RoadStationType.WEATHER_STATION);
        for (int i = 0; i < expectedHistory.size(); i++) {
            final SensorValueHistory expected = expectedHistory.get(i);
            final WeatherSensorValueHistoryDto actual = actualHistory.get(i);
            final Long expectedRsNaturalId = roadStationNaturalIdMaps.getOrDefault(expected.getRoadStationId(), -1L);
            assertEquals(expected.getSensorId(), actual.getSensorId());
            assertEquals(expectedRsNaturalId, actual.getRoadStationId());
            assertEquals(expected.getSensorValue(), actual.getSensorValue());
            assertEquals(expected.getMeasuredTime().toInstant().truncatedTo(ChronoUnit.SECONDS), actual.getMeasuredTime());
        }
    }

    @Test
    public void historyCap() {

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
