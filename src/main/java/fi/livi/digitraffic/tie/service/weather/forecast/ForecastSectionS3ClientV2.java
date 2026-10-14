package fi.livi.digitraffic.tie.service.weather.forecast;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.conf.amazon.RoadForecastS3ClientConfiguration;
import fi.livi.digitraffic.tie.conf.amazon.RoadForecastS3Properties;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionDataDtoV2;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads road forecast files from S3. File name:
 * {@code <prefix><data set file name prefix><forecast_time>_<created_time>.json}, timestamps {@code yyyyMMdd'T'HHmmss'Z'}.
 * Data set file name prefixes are defined in {@link ForecastDataSetV2}.
 */
@ConditionalOnNotWebApplication
@Component
public class ForecastSectionS3ClientV2 {
    private static final Logger log = LoggerFactory.getLogger(ForecastSectionS3ClientV2.class);

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    // File name part after the data set prefix: <forecast_time>_<created_time>.json
    // group 1: time the forecast is for (start of the forecast), group 2: time when the forecast was created
    // Matches the part after the prefix of ie. roadweather_forecast_public_full_20261014T080000Z_20261014T064827Z.json
    // -> 20261014T080000Z_20261014T064827Z.json
    private static final Pattern FILE_NAME_TIMES_PATTERN = Pattern.compile("^(\\d{8}T\\d{6}Z)_(\\d{8}T\\d{6}Z)\\.json$");

    /** @param forecastStartTime time the forecast is for (start of the forecast)
     *  @param createdTime time when the forecast was created, the latest file is chosen by this */
    public record ForecastFile(String key, Instant forecastStartTime, Instant createdTime) {}

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final String bucketName;
    private final String prefix;
    private final Duration window;
    private List<String> missingSettings = List.of();

    @Autowired
    public ForecastSectionS3ClientV2(@Qualifier(RoadForecastS3ClientConfiguration.BEAN_NAME) final ObjectProvider<S3Client> s3ClientProvider,
                                     final ObjectMapper objectMapper,
                                     final Environment environment,
                                     final RoadForecastS3Properties properties) {
        this(s3ClientProvider.getIfAvailable(), objectMapper, properties);
        this.missingSettings = Stream.of("bucketName", "region", "accessKeyId", "secretAccessKey")
            .map(name -> "dt.amazon.s3.roadforecast." + name)
            .filter(property -> !StringUtils.isNotBlank(environment.getProperty(property)))
            .toList();
    }

    ForecastSectionS3ClientV2(final S3Client s3Client,
                                     final ObjectMapper objectMapper,
                                     final RoadForecastS3Properties properties) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.bucketName = properties.getS3BucketName();
        this.prefix = properties.getPrefix();
        this.window = properties.getWindow();
    }

    public boolean isConfigured() {
        return s3Client != null && bucketName != null && !bucketName.isBlank();
    }

    /** Names the missing settings for logging. */
    public String describeMissingConfiguration() {
        return missingSettings.isEmpty() ? "unknown reason" : "missing or empty properties: " + String.join(", ", missingSettings);
    }

    /**
     * @return the file with the latest production time within the window, or empty if none found
     */
    public Optional<ForecastFile> findLatestFile(final ForecastDataSetV2 dataSet, final Instant now) {
        final String keyPrefix = prefix + dataSet.fileNamePrefix;
        // startAfter is just a key to start listing from, it does not have to exist
        final String startAfter = keyPrefix + TIMESTAMP_FORMAT.format(now.minus(window));

        final List<ForecastFile> files = new ArrayList<>();
        String continuationToken = null;
        do {
            final ListObjectsV2Response response;
            try {
                response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(keyPrefix)
                    .startAfter(startAfter)
                    .continuationToken(continuationToken)
                    .build());
            } catch (final SdkException e) {
                throw new IllegalStateException("Listing forecast files from S3 failed bucket=" + bucketName + " prefix=" + keyPrefix
                    + ": " + e.getMessage(), e);
            }
            for (final S3Object object : response.contents()) {
                parse(keyPrefix, object.key()).ifPresent(files::add);
            }
            continuationToken = Boolean.TRUE.equals(response.isTruncated()) ? response.nextContinuationToken() : null;
        } while (continuationToken != null);

        return files.stream().max(Comparator.comparing(ForecastFile::createdTime).thenComparing(ForecastFile::key));
    }

    static Optional<ForecastFile> parse(final String keyPrefix, final String key) {
        // Strip the data set prefix, an empty string never matches
        final Matcher m = FILE_NAME_TIMES_PATTERN.matcher(key.startsWith(keyPrefix) ? key.substring(keyPrefix.length()) : "");
        if (!m.matches()) {
            log.warn("method=parse Skipping S3 key with unexpected name key={}", key);
            return Optional.empty();
        }
        try {
            return Optional.of(new ForecastFile(key, Instant.from(TIMESTAMP_FORMAT.parse(m.group(1))), Instant.from(TIMESTAMP_FORMAT.parse(m.group(2)))));
        } catch (final RuntimeException e) {
            log.warn("method=parse Skipping S3 key with invalid timestamps key={}", key);
            return Optional.empty();
        }
    }

    public ForecastSectionDataDtoV2 read(final ForecastFile file) {
        final byte[] bytes;
        try {
            bytes = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(file.key()).build(),
                                       ResponseTransformer.toBytes()).asByteArray();
        } catch (final SdkException e) {
            throw new IllegalStateException("Reading forecast file from S3 failed bucket=" + bucketName + " key=" + file.key()
                + ": " + e.getMessage(), e);
        }
        return objectMapper.readValue(bytes, ForecastSectionDataDtoV2.class);
    }
}
