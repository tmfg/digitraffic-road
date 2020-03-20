package fi.livi.digitraffic.tie.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

import fi.livi.digitraffic.tie.conf.amazon.S3Properties;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.model.SensorValueHistory;

@Component
@ConditionalOnNotWebApplication
public class SensorDataS3Writer {
    private static final Logger log = LoggerFactory.getLogger(SensorDataS3Writer.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH");
    private static final SimpleDateFormat DIRECTORY_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd/");
    private static final String CSV_HEADER = "RoadStationId;SensorId;SensorValue;MeasuredTime;TimeWindowStart;TimeWindowEnd\n";

    private static final int BUFFER_SIZE = 65536;

    private final SensorValueHistoryRepository repository;
    private final S3Properties s3Properties;
    //private final AmazonS3 s3Client;

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        FILE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        DIRECTORY_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public SensorDataS3Writer(final SensorValueHistoryRepository repository,
                              @Qualifier("sensorDataProperties") final S3Properties s3Properties/**,
                              @Qualifier("sensorDataS3") final AmazonS3 s3Client*/) {
        this.repository = repository;
        this.s3Properties = s3Properties;
        //this.s3Client = s3Client;
    }

    @Transactional
    public boolean writeSensorData(final ZonedDateTime from, final ZonedDateTime to) {
        final String filename = FILE_DATE_FORMAT.format(Date.from(from.toInstant())).concat("-sensors");
        final String directorPrefix = DIRECTORY_DATE_FORMAT.format(Date.from(from.toInstant()));

        final ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);

        // Just collecting log data
        final AtomicInteger counter = new AtomicInteger(0);

        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            zos.putNextEntry(new ZipEntry(filename.concat(".csv")));

            zos.write(CSV_HEADER.getBytes());

            repository.streamAllByMeasuredTimeGreaterThanEqualAndMeasuredTimeLessThanOrderByMeasuredTimeAsc(from, to).forEach(item -> {
                try {
                    zos.write(writeLine(item).getBytes());

                    counter.getAndIncrement();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            zos.closeEntry();
            zos.close();

            final InputStream inputStream = bos.toInputStream();

            //ObjectMetadata metadata = new ObjectMetadata();
            //metadata.setContentType("application/zip");
            //metadata.setContentLength(bos.size());

            // Write to S3
            //s3Client.putObject(s3Properties.getS3BucketName(), filename, inputStream, metadata);

            // Local copy-to-file hack
            FileUtils.copyInputStreamToFile(inputStream, new File(filename.concat(".zip")));

            log.info("Collected values={}, window {} - {}, file {}", counter.get(), from, to, directorPrefix.concat(filename.concat(".zip")));

            return true;
        } catch (Exception e) {
            log.error("Failed to process ", e);

            //throw e;
        }

        return false;
    }

    private String writeLine(SensorValueHistory history) {
        StringBuilder builder = new StringBuilder();
        builder.append(history.getRoadStationId());
        builder.append(";");
        builder.append(history.getSensorId());
        builder.append(";");
        builder.append(history.getSensorValue());
        builder.append(";");
        builder.append(DATE_FORMAT.format(Date.from(history.getMeasuredTime().toInstant())));
        builder.append(";");

        if (history.getTimeWindowStart() != null) {
            builder.append(DATE_FORMAT.format(Date.from(history.getTimeWindowStart().toInstant())));
        }

        builder.append(";");

        if (history.getTimeWindowEnd() != null) {
            builder.append(DATE_FORMAT.format(Date.from(history.getTimeWindowEnd().toInstant())));
        }

        builder.append("\n");

        return builder.toString();
    }
}
