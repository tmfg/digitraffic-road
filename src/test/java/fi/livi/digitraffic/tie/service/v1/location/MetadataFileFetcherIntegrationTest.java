package fi.livi.digitraffic.tie.service.v1.location;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(MetadataFileFetcher.class)
public class MetadataFileFetcherIntegrationTest {
    @Test
    public void testLocationNotFound() {
        final MetadataFileFetcher fetcher = new MetadataFileFetcher("https://tie.digitraffic.fi/tmc/noncertified/not_found");

        assertThrows(FileNotFoundException.class, () -> {
            fetcher.getLocationsFile(new MetadataVersions.MetadataVersion("", ""));
        });
    }
}
