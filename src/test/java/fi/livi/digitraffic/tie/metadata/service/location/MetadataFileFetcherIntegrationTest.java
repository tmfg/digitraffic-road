package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(MetadataFileFetcher.class)
public class MetadataFileFetcherIntegrationTest {
    @Value("${metadata.tmc.url}")
    private String tmcUrl;

    @Test(expected = FileNotFoundException.class)
    public void testLocationNotFound() throws IOException {
        final MetadataFileFetcher fetcher = new MetadataFileFetcher("http://localhost/not_found");

        final Path path = fetcher.getLocationsFile(new MetadataVersions.MetadataVersion("", ""));
        Assert.assertNotNull(path);
    }
}
