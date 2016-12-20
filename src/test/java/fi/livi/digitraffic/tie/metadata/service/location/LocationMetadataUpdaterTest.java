package fi.livi.digitraffic.tie.metadata.service.location;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.AbstractTestBase;

public class LocationMetadataUpdaterTest extends AbstractTestBase {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @SpyBean
    private MetadataFileFetcher metadataFileFetcher;

    @Test
    @Rollback(true)
    @Transactional
    public void findAndUpdate() throws IOException {
        locationMetadataUpdater.findAndUpdate();
        verify(metadataFileFetcher).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    @Rollback(true)
    @Transactional
    public void findAndUpdateVersionsDiffer() throws IOException {
        final MetadataVersions mv = mock(MetadataVersions.class);
        when(mv.getLocationsVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1"));
        when(mv.getLocationTypeVersion()).thenReturn(new MetadataVersions.MetadataVersion("b", "2"));

        when(metadataFileFetcher.getLatestVersions()).thenReturn(mv);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }
}
