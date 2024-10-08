package fi.livi.digitraffic.tie.service.trafficmessage.location;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.github.tomakehurst.wiremock.WireMockServer;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;

public class LocationMetadataUpdaterTest extends AbstractServiceTest {
    private static final Logger log = LoggerFactory.getLogger(LocationMetadataUpdaterTest.class);

    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @Value("${metadata.tmc.url}")
    private String tmcUrl;

    private WireMockServer wm;

    @BeforeEach
    public void initData() throws IOException {
        wm = new WireMockServer(options().port(8897));
        wm.start();
        log.info("WireMockServer options: {}", wm.getOptions());
        log.info("tmcUrl: {}", tmcUrl);
        final String body =
                TestUtils.loadResource("classpath:/locations/latest.txt").getContentAsString(StandardCharsets.UTF_8);
        wm.stubFor(get(urlEqualTo("/tmc/noncertified/latest.txt"))
                .willReturn(aResponse()
                        .withGzipDisabled(true)
                        .withBody(body)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                        .withStatus(200)));
    }

    @AfterEach
    public void cleanUp() {
        wm.stop();
        wm = null;
    }

    @Test
    @Disabled
    public void findAndUpdate() throws IOException {
        locationMetadataUpdater.findAndUpdate();
        verify(metadataFileFetcherSpy).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateVersionsDiffer() throws IOException {
        final MetadataVersions mv = mock(MetadataVersions.class);
        when(mv.getLocationsVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1"));
        when(mv.getLocationTypeVersion()).thenReturn(new MetadataVersions.MetadataVersion("b", "2"));

        when(metadataFileFetcherSpy.getLatestVersions()).thenReturn(mv);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcherSpy, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateNoUpdateNeeded() throws IOException {
        final MetadataVersions mv = mock(MetadataVersions.class);
        when(mv.getLocationsVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1.1"));
        when(mv.getLocationTypeVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1.1"));

        when(metadataFileFetcherSpy.getLatestVersions()).thenReturn(mv);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcherSpy, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateException() throws IOException {
        try {
            when(metadataFileFetcherSpy.getLatestVersions()).thenThrow(new IllegalArgumentException("TEST"));

            locationMetadataUpdater.findAndUpdate();

            fail();
        } catch(final IllegalArgumentException iae) {
            assertEquals(iae.getMessage(), "TEST");
        }

        verify(metadataFileFetcherSpy, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateVersionsEmpty() throws IOException {
        when(metadataFileFetcherSpy.getLatestVersions()).thenReturn(null);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcherSpy, never()).getFilePaths(any(MetadataVersions.class));
    }
}
