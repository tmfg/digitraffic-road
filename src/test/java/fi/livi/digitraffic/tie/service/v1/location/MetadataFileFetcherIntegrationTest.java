package fi.livi.digitraffic.tie.service.v1.location;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(MetadataFileFetcher.class)
public class MetadataFileFetcherIntegrationTest {
    @Test
    public void testLocationNotFound() {
        final MetadataFileFetcher fetcher = new MetadataFileFetcher("https://aineistot.vayla.fi/not_found");

        assertThrows(FileNotFoundException.class, () -> {
            fetcher.getLocationsFile(new MetadataVersions.MetadataVersion("", ""));
        });
    }
}
