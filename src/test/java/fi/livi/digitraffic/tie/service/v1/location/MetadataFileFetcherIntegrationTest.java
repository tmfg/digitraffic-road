package fi.livi.digitraffic.tie.service.v1.location;

import java.io.FileNotFoundException;
import java.io.IOException;

import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import org.junit.jupiter.api.Test;import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(MetadataFileFetcher.class)
public class MetadataFileFetcherIntegrationTest {
    @Test
    public void testLocationNotFound() throws IOException {
        final MetadataFileFetcher fetcher = new MetadataFileFetcher("https://aineistot.vayla.fi/not_found");

        assertThrows(FileNotFoundException.class, () -> {
            fetcher.getLocationsFile(new MetadataVersions.MetadataVersion("", ""));
        });
    }
}
