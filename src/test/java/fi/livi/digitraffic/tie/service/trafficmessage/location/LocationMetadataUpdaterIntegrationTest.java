package fi.livi.digitraffic.tie.service.trafficmessage.location;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import com.github.tomakehurst.wiremock.WireMockServer;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationRepository;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationTypeRepository;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationVersionRepository;

/**
 * Integration test that exercises the full {@link LocationMetadataUpdater#findAndUpdate()} pipeline
 * over HTTP using WireMock – the same transport used in production.
 *
 * <p>WireMock serves:
 * <ul>
 *   <li>{@code latest.txt} – version manifest (1.11.45)</li>
 *   <li>{@code FI_LC_noncertified_1_11_45.zip} – TYPES.DAT + SUBTYPES.DAT</li>
 *   <li>{@code FI_LC_noncertified_simple_1_11_45.zip} – Finnish locations CSV</li>
 * </ul>
 *
 * <p>Each test is {@link Transactional} and rolls back after completion, so tests are fully
 * independent even when they write to the same DB tables.
 */
@Transactional
public class LocationMetadataUpdaterIntegrationTest extends AbstractServiceTest {

    private static final Logger log = getLogger(LocationMetadataUpdaterIntegrationTest.class);

    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;
    @Autowired
    private LocationVersionRepository locationVersionRepository;
    @Autowired
    private LocationTypeRepository locationTypeRepository;
    @Autowired
    private LocationSubtypeRepository locationSubtypeRepository;
    @Autowired
    private LocationRepository locationRepository;

    private static final String VERSION = "1.11.45";
    private static final String VERSION_ = VERSION.replace('.', '_');
    private static final String DAT_ZIP_NAME = "FI_LC_noncertified_" + VERSION_ + ".zip";
    private static final String FI_CSV_ZIP_NAME = "FI_LC_noncertified_simple_" + VERSION_ + ".zip";
    private static final String SWE_ZIP_NAME = "FI_LC_noncertified_simple_" + VERSION_ + "_swe.zip";

    private static final String CLASSPATH_ZIP_RESOURCES = "classpath:/locations/tmc/noncertified/";
    private static final String TMC_PATH = "/tmc/noncertified/";

    private WireMockServer wm;

    @BeforeEach
    public void setUp() throws IOException {
        wm = new WireMockServer(options().port(8897));
        wm.start();

        // Serve the real latest.txt from classpath – same file the production URL would return
        wm.stubFor(get(urlEqualTo(TMC_PATH + "latest.txt"))
                .willReturn(aResponse()
                        .withGzipDisabled(true)
                        .withBody(TestUtils.loadResource(CLASSPATH_ZIP_RESOURCES + "latest.txt")
                                .getContentAsString(StandardCharsets.UTF_8))
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                        .withStatus(200)));

        stubZip(FI_CSV_ZIP_NAME);
        stubZip(DAT_ZIP_NAME);
    }

    @AfterEach
    public void tearDown() {
        wm.stop();
        wm = null;
    }

    /**
     * Full pipeline test with Finnish CSV: verifies types, subtypes and locations are persisted
     * and the version is recorded in DB.
     */
    @Test
    public void findAndUpdateImportsAllLocations() throws IOException {
        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcherSpy).getFilePaths(any(MetadataVersions.class));

        final var saved = locationVersionRepository.findLatestVersion();
        assertNotNull(saved);
        assertEquals(VERSION, saved.getVersion());

        assertFalse(locationTypeRepository.findAll().isEmpty());
        assertFalse(locationSubtypeRepository.findAll().stream()
                .filter(s -> s.getId().getVersion().equals(VERSION)).toList().isEmpty());
        assertFalse(locationRepository.findAllByVersion(VERSION).toList().isEmpty());
    }

    /**
     * Full pipeline test with Swedish CSV: overrides the {@code latest.txt} stub to point the
     * {@code csv} entry at the swe ZIP (keeping the same version and dat ZIP). Each test rolls
     * back its transaction so there is no duplicate-key conflict with
     * {@link #findAndUpdateImportsAllLocations}.
     */
    @Test
    public void findAndUpdateImportsSweLocations() throws IOException {
        // Override latest.txt to swap the csv entry to the Swedish ZIP
        stubLatest(DAT_ZIP_NAME, SWE_ZIP_NAME);
        stubZip(SWE_ZIP_NAME);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcherSpy).getFilePaths(any(MetadataVersions.class));

        final var saved = locationVersionRepository.findLatestVersion();
        assertNotNull(saved);
        assertEquals(VERSION, saved.getVersion());

        assertFalse(locationTypeRepository.findAll().isEmpty());
        assertFalse(locationRepository.findAllByVersion(VERSION).toList().isEmpty());
    }

    // ── WireMock helpers ──────────────────────────────────────────────────────

    private void stubLatest(final String datZipName, final String csvZipName) {
        final String body = "format\tfilename\tversion\n"
                + "dat\t" + datZipName + "\t" + VERSION + "\n"
                + "csv\t" + csvZipName + "\t" + VERSION + "\n";
        log.info("method=stubLatest {}latest.txt with body:\n{}", TMC_PATH, body);
        wm.stubFor(get(urlEqualTo(TMC_PATH + "latest.txt"))
                .willReturn(aResponse()
                        .withGzipDisabled(true)
                        .withBody(body)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                        .withStatus(200)));
    }

    private void stubZip(final String zipName) throws IOException {
        wm.stubFor(get(urlEqualTo(TMC_PATH + zipName))
                .willReturn(aResponse()
                        .withGzipDisabled(true)
                        .withBody(TestUtils.loadResource(CLASSPATH_ZIP_RESOURCES + zipName).getContentAsByteArray())
                        .withStatus(200)));
    }
}
