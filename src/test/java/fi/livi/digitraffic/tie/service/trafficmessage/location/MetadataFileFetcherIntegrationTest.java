package fi.livi.digitraffic.tie.service.trafficmessage.location;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import fi.livi.digitraffic.tie.model.DataType;

@Import(MetadataFileFetcher.class)
public class MetadataFileFetcherIntegrationTest {
    @Test
    public void testLocationNotFound() {
        final MetadataFileFetcher fetcher = new MetadataFileFetcher("https://tie.digitraffic.fi/tmc/noncertified/not_found");
        final MetadataVersions versions = new MetadataVersions();
        versions.addVersion(DataType.LOCATIONS_METADATA, "", "");
        versions.addVersion(DataType.LOCATION_TYPES_METADATA, "", "");

        assertThrows(FileNotFoundException.class, () -> fetcher.getFilePaths(versions));
    }
}
