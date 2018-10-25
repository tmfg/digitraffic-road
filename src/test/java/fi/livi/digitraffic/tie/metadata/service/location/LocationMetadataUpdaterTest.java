package fi.livi.digitraffic.tie.metadata.service.location;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.service.IllegalArgumentException;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LocationMetadataUpdaterTest extends AbstractTest {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @SpyBean
    private MetadataFileFetcher metadataFileFetcher;

    @Test
    public void findAndUpdate() throws IOException {
        locationMetadataUpdater.findAndUpdate();
        verify(metadataFileFetcher).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateVersionsDiffer() throws IOException {
        final MetadataVersions mv = mock(MetadataVersions.class);
        when(mv.getLocationsVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1"));
        when(mv.getLocationTypeVersion()).thenReturn(new MetadataVersions.MetadataVersion("b", "2"));

        when(metadataFileFetcher.getLatestVersions()).thenReturn(mv);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateNoUpdateNeeded() throws IOException {
        final MetadataVersions mv = mock(MetadataVersions.class);
        when(mv.getLocationsVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1.1"));
        when(mv.getLocationTypeVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1.1"));

        when(metadataFileFetcher.getLatestVersions()).thenReturn(mv);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateException() throws IOException {
        try {
            when(metadataFileFetcher.getLatestVersions()).thenThrow(new IllegalArgumentException("TEST"));

            locationMetadataUpdater.findAndUpdate();

            fail();
        } catch(final IllegalArgumentException iae) {
            assertEquals(iae.getMessage(), "TEST");
        }

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateVersionsEmpty() throws IOException {
        when(metadataFileFetcher.getLatestVersions()).thenReturn(null);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }
}
