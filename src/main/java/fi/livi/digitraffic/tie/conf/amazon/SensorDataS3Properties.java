package fi.livi.digitraffic.tie.conf.amazon;

import static java.time.ZoneOffset.UTC;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class SensorDataS3Properties extends S3Properties {
    private static final String FILE_DATE_PATTERN = "yyyy-MM-dd_HH";
    private static final String DIRECTORY_DATE_PATTERN = "yyyy/MM/dd/";
    private static final String FILENAME_PART = "-sensors";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern(FILE_DATE_PATTERN);
    private static final DateTimeFormatter DIRECTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern(DIRECTORY_DATE_PATTERN);

    public static final String CSV = ".csv";
    public static final String ZIP = ".zip";

    private ZonedDateTime refTime;

    public SensorDataS3Properties(final String s3BucketName) {
        super(s3BucketName);

        setRefTime(ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS));
    }

    public void setRefTime(final ZonedDateTime time) {
        refTime = time;
    }

    public final String getFileStorageName() {
        return getFileStorageName(refTime);
    }

    public final String getFileStorageName(final ZonedDateTime time) {
        return time.format(DIRECTORY_DATE_FORMATTER).concat(getFilename(time, ZIP));
    }

    public final String getFilename(final String suffix) { return getFilename(refTime, suffix); }

    public final String getFilename(final ZonedDateTime time, final String suffix) {
        return time.format(FILE_DATE_FORMATTER)
            .concat(FILENAME_PART)
            .concat(suffix);
    }

    public final ZonedDateTime getHistoryStartTime(final String filename) {
        return ZonedDateTime.parse(filename.substring(DIRECTORY_DATE_PATTERN.length(),
            DIRECTORY_DATE_PATTERN.length() + FILE_DATE_PATTERN.length()),
            FILE_DATE_FORMATTER.withZone(UTC));
    }

    public boolean isValidHistoryFile(final String filename) {
        return filename.contains(FILENAME_PART);
    }
}
