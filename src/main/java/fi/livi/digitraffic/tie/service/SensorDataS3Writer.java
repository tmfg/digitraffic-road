package fi.livi.digitraffic.tie.service;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.time.ZonedDateTime;
import java.util.Map;
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
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import fi.livi.digitraffic.tie.conf.amazon.SensorDataS3Properties;
import fi.livi.digitraffic.tie.dao.roadstation.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.dto.weather.WeatherSensorValueHistoryDto;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;

@Component
@ConditionalOnNotWebApplication
public class SensorDataS3Writer {
    private static final Logger log = LoggerFactory.getLogger(SensorDataS3Writer.class);

    private static final String CSV_HEADER = "RoadStationId;SensorId;SensorValue;MeasuredTime;TimeWindowStart;TimeWindowEnd\n";
    private static final String NOT_FOUND = "404";
    private static final int BUFFER_SIZE = 65536;

    private final RoadStationService roadStationService;
    private final SensorValueHistoryRepository repository;
    private final SensorDataS3Properties s3Properties;
    private final AmazonS3 s3Client;

    // <road_station_id, natural_id>
    private Map<Long, Long> roadStationNaturalIdMaps;

    public SensorDataS3Writer(final RoadStationService roadStationService,
                              final SensorValueHistoryRepository repository,
                              final SensorDataS3Properties sensorDataS3Properties,
                              final AmazonS3 sensorDataS3Client) {
        this.roadStationService = roadStationService;
        this.repository = repository;
        this.s3Properties = sensorDataS3Properties;
        this.s3Client = sensorDataS3Client;
    }


    @Transactional(readOnly = true)
    public int writeSensorData(final ZonedDateTime from, final ZonedDateTime to) {
        // Set current time window start
        s3Properties.setRefTime(from);

        // Sensor values are stored using internal road_station_id from road_station-table. But API uses natural road_station ids ->
        // Map internal road_station_id to naturalId from road_station-table
        roadStationNaturalIdMaps = roadStationService.getNaturalIdMappings(RoadStationType.WEATHER_STATION);

        // Just collecting log data
        final AtomicInteger counter = new AtomicInteger(0);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);

        try (final ZipOutputStream zos = new ZipOutputStream(bos);
            final OutputStreamWriter osw = new OutputStreamWriter(zos)) {

            // .csv file
            zos.putNextEntry(new ZipEntry(s3Properties.getFilename(SensorDataS3Properties.CSV)));

            // csv-builder
            final StatefulBeanToCsv csvWriter = new StatefulBeanToCsvBuilder<WeatherSensorValueHistoryDto>(osw)
                .withSeparator(';')
                .withApplyQuotesToAll(false)
                .build();

            csvWriter.write(repository.streamAllByMeasuredTimeGreaterThanEqualAndMeasuredTimeLessThanOrderByMeasuredTimeAsc(from, to)
                .map(item -> {
                    counter.getAndIncrement();

                    // Internal road_station_id must be mapped back to road_station's natural_id (API uses natural ids)
                    return new WeatherSensorValueHistoryDto(mapToNaturalId(item),
                        item.getSensorId(),
                        item.getSensorValue(),
                        item.getMeasuredTime().toInstant());
                })
             );

            osw.flush();
            zos.closeEntry();
            zos.close();

            final InputStream inputStream = bos.toInputStream();

            final String fileName = s3Properties.getFileStorageName();

            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/zip");
            metadata.setContentLength(bos.size());

            // Write to S3
            s3Client.putObject(s3Properties.getS3BucketName(), fileName, inputStream, metadata);

            // Local copy-to-file hack
            //FileUtils.copyInputStreamToFile(inputStream, new File(fileName));

            log.info("method=writeSensorData Collected addCount={} , window {} - {} , file {}", counter.get(), from, to, fileName);

            return counter.get();
        } catch (final Exception e) {
            log.error("Failed to process sensor data archive", e);
        }

        return -1;
    }

    private long mapToNaturalId(final SensorValueHistory item) {
        final Long naturalId = roadStationNaturalIdMaps.get(item.getRoadStationId());

        // NOTE! -1 is just for fail safe
        return naturalId != null ? naturalId : -1;
    }
}
