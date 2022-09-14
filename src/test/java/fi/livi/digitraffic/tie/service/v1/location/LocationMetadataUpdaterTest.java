package fi.livi.digitraffic.tie.service.v1.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;

public class LocationMetadataUpdaterTest extends AbstractServiceTest {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

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
