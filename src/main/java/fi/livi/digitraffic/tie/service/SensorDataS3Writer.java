package fi.livi.digitraffic.tie.service;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fi.livi.digitraffic.tie.service.aws.S3Service;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import fi.livi.digitraffic.tie.conf.amazon.SensorDataS3Properties;
import fi.livi.digitraffic.tie.dao.roadstation.v1.RoadStationSensorValueHistoryDtoRepositoryV1;
import fi.livi.digitraffic.tie.dto.weather.WeatherSensorValueHistoryS3CsvDto;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;

@Component
@ConditionalOnNotWebApplication
public class SensorDataS3Writer {
    private static final Logger log = LoggerFactory.getLogger(SensorDataS3Writer.class);

    //    private static final String[] CSV_HEADER =
    //            new String[] { "RoadStationId", "SensorId", "Value", "Measured", "TimeWindowStart", "TimeWindowEnd" };

    private static final int BUFFER_SIZE = 65536;

    private final RoadStationSensorValueHistoryDtoRepositoryV1 repository;
    private final SensorDataS3Properties s3Properties;
    private final S3Service s3Service;

    public SensorDataS3Writer(final RoadStationSensorValueHistoryDtoRepositoryV1 repository,
                              final SensorDataS3Properties sensorDataS3Properties,
                              final S3Service s3Service) {
        this.repository = repository;
        this.s3Properties = sensorDataS3Properties;
        this.s3Service = s3Service;
    }

    /**
     * @param from inclusive
     * @param to   exclusive
     * @return written sensor values count
     */
    @Transactional(readOnly = true)
    public int writeSensorData(final Instant from, final Instant to) {
        // Set current time window start
        s3Properties.setRefTime(from);

        // Just collecting log data
        final AtomicInteger counter = new AtomicInteger(0);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);

        try (final ZipOutputStream zos = new ZipOutputStream(bos);
                final OutputStreamWriter osw = new OutputStreamWriter(zos)) {

            // .csv file
            zos.putNextEntry(new ZipEntry(s3Properties.getFilename(SensorDataS3Properties.CSV)));

            // csv-builder
            final StatefulBeanToCsv<WeatherSensorValueHistoryS3CsvDto> csvWriter =
                    new StatefulBeanToCsvBuilder<WeatherSensorValueHistoryS3CsvDto>(osw)
                            .withSeparator(';')
                            .withApplyQuotesToAll(false)
                            .build();

            csvWriter.write(
                    repository.findAllPublicPublishableRoadStationSensorValuesBetween(
                                    RoadStationType.WEATHER_STATION,
                                    from, to).stream()
                            .map(item -> {
                                counter.getAndIncrement();
                                // Use natural ids as API uses natural ids
                                return new WeatherSensorValueHistoryS3CsvDto(
                                        item.getRoadStationNaturalId(),
                                        item.getSensorNaturalId(),
                                        item.getValue(),
                                        item.getMeasuredTime(),
                                        item.getReliability());
                            })
            );

            osw.flush();
            zos.closeEntry();
            zos.close();

            // Write to S3
            s3Service.putObject(s3Properties.getS3BucketName(), s3Properties.getFileStorageName(), "application/zip", bos.toByteArray());

            // Local copy-to-file hack
            // FileUtils.copyInputStreamToFile(inputStream, new File(fileName));

            log.info("method=writeSensorData Collected addCount={} , window {} - {} , file {}", counter.get(), from, to,
                    s3Properties.getFileStorageName());

            return counter.get();
        } catch (final Exception e) {
            log.error("Failed to process sensor data archive", e);
        }

        return -1;
    }
}
