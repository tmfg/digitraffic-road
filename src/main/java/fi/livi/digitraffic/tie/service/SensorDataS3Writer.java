package fi.livi.digitraffic.tie.service;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import fi.livi.digitraffic.tie.conf.amazon.SensorDataS3Properties;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.SensorValueHistory;

@Component
@ConditionalOnNotWebApplication
public class SensorDataS3Writer {
    private static final Logger log = LoggerFactory.getLogger(SensorDataS3Writer.class);

    private static final String CSV_HEADER = "RoadStationId;SensorId;SensorValue;MeasuredTime;TimeWindowStart;TimeWindowEnd\n";

    private static final int BUFFER_SIZE = 65536;

    private final RoadStationService roadStationService;
    private final SensorValueHistoryRepository repository;
    private final SensorDataS3Properties s3Properties;
    private final AmazonS3 s3Client;

    private Map<Long, Long> maps;

    public SensorDataS3Writer(final RoadStationService roadStationService,
                              final SensorValueHistoryRepository repository,
                              final SensorDataS3Properties sensorDataS3Properties,
                              final AmazonS3 sensorDataS3Client) {
        this.roadStationService = roadStationService;
        this.repository = repository;
        this.s3Properties = sensorDataS3Properties;
        this.s3Client = sensorDataS3Client;
    }

    public boolean updateSensorDataS3History(final ZonedDateTime currentTimeWindow) {
        boolean historyUpdated = false;

        // Get last modified
        Optional<S3ObjectSummary> latest = s3Client.listObjectsV2(s3Properties.getS3BucketName()).getObjectSummaries()
            .stream()
            //.peek(System.out::println)
            .max((o1, o2) -> o1.getLastModified().compareTo(o2.getLastModified()));

        if (latest.isPresent() && s3Properties.isValidHistoryFile(latest.get().getKey())) {
            ZonedDateTime nextTimeWindow = s3Properties.getHistoryStartTime(latest.get().getKey()).plusHours(1);

            log.info("compare last item {} to new {}", nextTimeWindow, currentTimeWindow);

            while (nextTimeWindow.isBefore(currentTimeWindow)) {
                historyUpdated = true;

                log.warn("Missing history item: {} - {}", nextTimeWindow, nextTimeWindow.plusHours(1));

                try {
                    writeSensorData(nextTimeWindow, nextTimeWindow.plusHours(1));
                } catch (Exception e) {
                    log.error("Failed to fix missing history: " + nextTimeWindow, e);
                }

                // Move to next hour
                nextTimeWindow = nextTimeWindow.plusHours(1);
            }
        }

        return historyUpdated;
    }

    @Transactional
    public int writeSensorData(final ZonedDateTime from, final ZonedDateTime to) {
        // Set current time window start
        s3Properties.setRefTime(from);

        // Get road_station_id -> natural_id mappings. NOTE! Only weather stations
        maps = roadStationService.getNaturalIdMappings(RoadStationType.WEATHER_STATION);

        // Just collecting log data
        final AtomicInteger counter = new AtomicInteger(0);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);

        try (ZipOutputStream zos = new ZipOutputStream(bos);
            OutputStreamWriter osw = new OutputStreamWriter(zos)) {

            // .csv file
            zos.putNextEntry(new ZipEntry(s3Properties.getFilename(SensorDataS3Properties.CSV)));

            // csv-builder
            StatefulBeanToCsv csvWriter = new StatefulBeanToCsvBuilder<SensorValueHistory>(osw)
                .withSeparator(';')
                .withApplyQuotesToAll(false)
                .build();

            csvWriter.write(repository.streamAllByMeasuredTimeGreaterThanEqualAndMeasuredTimeLessThanOrderByMeasuredTimeAsc(from, to)
                .map(item -> {
                    counter.getAndIncrement();

                    return mapNaturalId(item);
                })
             );

            osw.flush();
            zos.closeEntry();
            zos.close();

            final InputStream inputStream = bos.toInputStream();

            final String fileName = s3Properties.getFilename(SensorDataS3Properties.ZIP);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/zip");
            metadata.setContentLength(bos.size());

            // Write to S3
            s3Client.putObject(s3Properties.getS3BucketName(), s3Properties.getFileStorageName(fileName), inputStream, metadata);

            // Local copy-to-file hack
            //FileUtils.copyInputStreamToFile(inputStream, new File(fileName));

            log.info("Collected addCount={} , window {} - {} , file {}", counter.get(), from, to, s3Properties.getFileStorageName(fileName));

            return counter.get();
        } catch (Exception e) {
            log.error("Failed to process sensor data archive", e);
        }

        return -1;
    }

    private SensorValueHistory mapNaturalId(SensorValueHistory item) {
        Long id = maps.get(item.getRoadStationId());

        item.setRoadStationId(id != null ? id : -1);

        return item;
    }
}
