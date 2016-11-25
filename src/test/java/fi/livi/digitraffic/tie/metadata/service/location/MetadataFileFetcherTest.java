package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import fi.livi.digitraffic.tie.base.AbstractTestBase;

@Ignore
public class MetadataFileFetcherTest extends AbstractTestBase {
    @Value("${metadata.tms.url}")
    private String tmsUrl;

    @Test(expected = FileNotFoundException.class)
    public void testLocationNotFound() throws IOException {
        final MetadataFileFetcher fetcher = new MetadataFileFetcher("http://localhost/not_found");

        final Path path = fetcher.getLocationsFile(new MetadataVersions.MetadataVersion("", ""));
        Assert.assertNotNull(path);
    }
}
