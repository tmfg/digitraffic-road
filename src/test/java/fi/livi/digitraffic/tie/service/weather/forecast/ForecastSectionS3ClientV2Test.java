package fi.livi.digitraffic.tie.service.weather.forecast;

import fi.livi.digitraffic.tie.conf.amazon.RoadForecastS3Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import tools.jackson.databind.json.JsonMapper;

public class ForecastSectionS3ClientV2Test {
    private static final String P = "forecast/roadweather_forecast_public_";

    private final S3Client s3 = mock(S3Client.class);
    private final ForecastSectionS3ClientV2 client =
        new ForecastSectionS3ClientV2(s3, JsonMapper.builder().build(), props("bucket"));

    private static S3Object obj(final String key) {
        return S3Object.builder().key(key).build();
    }

    @Test
    public void picksLatestProductionTimeAndSkipsInvalidKeys() {
        final String latest = P + "20261007T120000Z_20261007T113000Z.json";
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(
            obj(P + "20261007T130000Z_20261007T110000Z.json"), // later first timestep, earlier production
            obj(latest),
            obj("forecast/roadweather_forecast_public_garbage.json"),
            obj(P + "20261007T120000Z_20261007T113000Z.json.tmp")).build());

        final var file = client.findLatestFile(Instant.parse("2026-10-07T12:00:00Z")).orElseThrow();
        assertEquals(latest, file.key());
        assertEquals(Instant.parse("2026-10-07T11:30:00Z"), file.productionTime());
        assertEquals(Instant.parse("2026-10-07T12:00:00Z"), file.firstTimestep());

        final ArgumentCaptor<ListObjectsV2Request> req = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(s3).listObjectsV2(req.capture());
        assertEquals("bucket", req.getValue().bucket());
        assertEquals(P, req.getValue().prefix());
        assertEquals(P + "20261006T120000Z", req.getValue().startAfter());
    }

    @Test
    public void followsPagination() {
        when(s3.listObjectsV2(any(ListObjectsV2Request.class)))
            .thenReturn(ListObjectsV2Response.builder().isTruncated(true).nextContinuationToken("t")
                            .contents(obj(P + "20261007T120000Z_20261007T110000Z.json")).build())
            .thenReturn(ListObjectsV2Response.builder().isTruncated(false)
                            .contents(obj(P + "20261007T130000Z_20261007T120000Z.json")).build());

        final var file = client.findLatestFile(Instant.parse("2026-10-07T12:00:00Z")).orElseThrow();
        assertEquals(Instant.parse("2026-10-07T12:00:00Z"), file.productionTime());
    }

    @Test
    public void emptyWindowReturnsEmpty() {
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().build());
        assertTrue(client.findLatestFile(Instant.now()).isEmpty());
    }

    @Test
    public void notConfiguredWithoutClientOrBucket() {
        final var mapper = JsonMapper.builder().build();
        assertFalse(new ForecastSectionS3ClientV2((S3Client) null, mapper, props("bucket")).isConfigured());
        assertFalse(new ForecastSectionS3ClientV2(s3, mapper, props("")).isConfigured());
        assertTrue(client.isConfigured());
    }

    private static RoadForecastS3Properties props(final String bucket) {
        return new RoadForecastS3Properties(bucket, "eu-north-1", "forecast/", 24);
    }
}
