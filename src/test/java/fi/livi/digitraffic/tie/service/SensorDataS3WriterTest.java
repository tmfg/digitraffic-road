package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.AmazonS3;
import com.opencsv.bean.CsvToBeanBuilder;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.conf.amazon.SensorDataS3Properties;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dao.weather.WeatherStationRepository;
import fi.livi.digitraffic.tie.dto.weather.WeatherSensorValueHistoryS3CsvDto;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;

public class SensorDataS3WriterTest extends AbstractDaemonTest {
    public static final Logger log = LoggerFactory.getLogger(SensorDataS3WriterTest.class);

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

    @Autowired
    private WeatherStationRepository weatherStationRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    private SensorValueHistoryBuilder builder;

    @Captor
    ArgumentCaptor<InputStream> inputStreamArgumentCaptor;

    @BeforeEach
    public void setUp() {
        TestUtils.truncateWeatherData(entityManager);
    }

    protected void initDBContent(final Instant time) {
        final int min = time.atZone(ZoneOffset.UTC).getMinute();

        final WeatherStation ws = TestUtils.generateDummyWeatherStation();
        final WeatherStation ws2 = TestUtils.generateDummyWeatherStation();
        weatherStationRepository.save(ws);
        weatherStationRepository.save(ws2);

        final List<RoadStationSensor> publishable =
                roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertFalse(publishable.isEmpty());

        roadStationSensorService.updateSensorsOfRoadStation(ws.getRoadStationId(),
                RoadStationType.WEATHER_STATION,
                publishable.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toList()));

        roadStationSensorService.updateSensorsOfRoadStation(ws2.getRoadStationId(),
                RoadStationType.WEATHER_STATION,
                publishable.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toList()));

        final Set<Long> ids =
                publishable.stream().map(RoadStationSensor::getId).collect(Collectors.toSet());
        // Init same db-content
        builder = new SensorValueHistoryBuilder(repository, log)
                .truncate()
                .setReferenceTime(time)
                .buildWithStationId(10, ws.getRoadStationId(), ids, 0, min)
                .buildWithStationId(50, ws2.getRoadStationId(), ids, min + 1, min + 61)
                .save();
    }

    @Test
    public void s3Bucket() throws IOException {
        final Instant now = TimeUtil.withoutMillis(ZonedDateTime.now().withMinute(1).toInstant());
        final Instant to = now.truncatedTo(ChronoUnit.HOURS);
        final Instant from = to.minus(1, ChronoUnit.HOURS);

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
        final Map<Long, Long> roadStationIdToNaturalIdMap =
                roadStationService.getNaturalIdMappings(RoadStationType.WEATHER_STATION);
        final Map<Long, Long> sensorIdToNaturalIdMap =
                roadStationSensorService.getIdToNaturalIdMap(RoadStationType.WEATHER_STATION);
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(zipBytes);
                final ZipInputStream in = new ZipInputStream(bis)) {
            // Get entry
            final ZipEntry entry = in.getNextEntry();

            log.info("entry {}", Objects.requireNonNull(entry).getName());

            final List<WeatherSensorValueHistoryS3CsvDto> items =
                    new CsvToBeanBuilder<WeatherSensorValueHistoryS3CsvDto>(new InputStreamReader(in))
                            .withType(WeatherSensorValueHistoryS3CsvDto.class)
                            .withSeparator(';')
                            .build()
                            .parse();

            in.closeEntry();

            assertEquals(origCount, items.size());
            //Convert db data to truncated as csv dates are truncated and order by measured time & sensor id
            final List<SensorValueHistory> expectedHistory =
                    // This contains also data outside from-to time frame, so we filter those out
                    builder.getGeneratedHistory().stream()
                            .peek(h -> h.setMeasuredTime(
                                    h.getMeasuredTime().truncatedTo(ChronoUnit.SECONDS))) // CSV is truncated
                            .filter(h -> h.getMeasuredTime().toEpochMilli() / 1000 >= from.getEpochSecond() &&
                                    h.getMeasuredTime().getEpochSecond() < to.getEpochSecond())
                            .sorted(Comparator.comparing(
                                            o -> roadStationIdToNaturalIdMap.get(((SensorValueHistory) o).getRoadStationId()))
                                    .thenComparing(
                                            o -> sensorIdToNaturalIdMap.get(((SensorValueHistory) o).getSensorId()))
                                    .thenComparing(o -> ((SensorValueHistory) o).getMeasuredTime())
                                    .thenComparing(o -> ((SensorValueHistory) o).getSensorValue())
                            )
                            .toList();
            // Same ordering for csv
            final List<WeatherSensorValueHistoryS3CsvDto> actualHistory = items.stream()
                    .sorted(Comparator.comparing(WeatherSensorValueHistoryS3CsvDto::getRoadStationNaturalId)
                            .thenComparing(WeatherSensorValueHistoryS3CsvDto::getSensorNaturalId)
                            .thenComparing(WeatherSensorValueHistoryS3CsvDto::getMeasuredTime)
                            .thenComparing(WeatherSensorValueHistoryS3CsvDto::getValue))
                    .toList();
            assertHistory(expectedHistory, actualHistory);
        } catch (final Exception e) {
            log.error("zip error:", e);
            Assertions.fail("failed to process zip: " + sensorDataS3Properties.getFileStorageName());
        }
    }

    private void assertHistory(final List<SensorValueHistory> expectedHistory,
                               final List<WeatherSensorValueHistoryS3CsvDto> actualHistory) {
        assertEquals(expectedHistory.size(), actualHistory.size());
        final Map<Long, Long> roadStationNaturalIdMap =
                roadStationService.getNaturalIdMappings(RoadStationType.WEATHER_STATION);
        final Map<Long, Long> sensorNaturalIdMap =
                roadStationSensorService.getIdToNaturalIdMap(RoadStationType.WEATHER_STATION);
        for (int i = 0; i < expectedHistory.size(); i++) {
            final SensorValueHistory expected = expectedHistory.get(i);
            final WeatherSensorValueHistoryS3CsvDto actual = actualHistory.get(i);
            final Long expectedRsNaturalId = roadStationNaturalIdMap.getOrDefault(expected.getRoadStationId(), -1L);
            final Long expectedSensorNaturalId = sensorNaturalIdMap.get(expected.getSensorId());
            assertEquals(expectedRsNaturalId, actual.getRoadStationNaturalId());
            assertEquals(expectedSensorNaturalId, actual.getSensorNaturalId());
            assertEquals(expected.getSensorValue(), actual.getValue());
            assertEquals(expected.getMeasuredTime().truncatedTo(ChronoUnit.SECONDS), actual.getMeasuredTime());
            assertEquals(expected.getReliability(), actual.getReliability());
        }
    }

    @Test
    public void historyCap() {

        final Instant now = Instant.now();
        final Instant currentTimeWindow = now.minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);

        Instant windowLoop = currentTimeWindow.minus(23, ChronoUnit.HOURS);
        final List<Instant> missingWindows = new ArrayList<>();

        // Create some test keys for missing time windows.
        while (missingWindows.size() < 8) {
            if (ZonedDateTime.ofInstant(windowLoop, ZoneId.of("UTC")).getHour() % 3 == 0) {
                missingWindows.add(windowLoop);
            }
            windowLoop = windowLoop.plus(1, ChronoUnit.HOURS);
        }

        Assertions.assertEquals(8, missingWindows.size());
        missingWindows.forEach(missingWindow -> {
            final int i = writer.writeSensorData(missingWindow, missingWindow.plus(1, ChronoUnit.HOURS));
            Assertions.assertTrue(i > -1, "History update failure");
        });
    }
}
