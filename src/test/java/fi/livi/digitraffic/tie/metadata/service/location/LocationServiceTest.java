package fi.livi.digitraffic.tie.metadata.service.location;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.AbstractTestBase;
import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationTypesMetadata;

public class LocationServiceTest extends AbstractTestBase {
    @Autowired
    private LocationService locationService;

    @Test
    public void findLocationOk() {
        assertNotNull(locationService.findLocation(12187));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findLocationNotFound() {
        assertNotNull(locationService.findLocation(-222));
    }

    @Test
    public void findLocationSubtypes() {
        final LocationTypesMetadata metadata = locationService.findLocationSubtypes(false);
        assertNotNull(metadata);
        assertNotNull(metadata.typesUpdated);
        assertNotNull(metadata.typesVersion);
        assertThat(metadata.locationSubtypes, not(empty()));
        assertThat(metadata.locationTypes, not(empty()));
    }

    @Test
    public void findLocationSubtypesLastUpdated() {
        final LocationTypesMetadata metadata = locationService.findLocationSubtypes(true);
        assertNotNull(metadata);
        assertNotNull(metadata.typesUpdated);
        assertNotNull(metadata.typesVersion);
        assertNull(metadata.locationSubtypes);
        assertNull(metadata.locationTypes);
    }

    @Test
    public void findLocationsMetadata() {
        final LocationFeatureCollection metadata = locationService.findLocationsMetadata(false);
        assertNotNull(metadata);
        assertNotNull(metadata.locationsUpdateTime);
        assertNotNull(metadata.locationsVersion);
        assertThat(metadata.features, not(empty()));
    }

    @Test
    public void findLocationsMetadataLastUpdated() {
        final LocationFeatureCollection metadata = locationService.findLocationsMetadata(true);
        assertNotNull(metadata);
        assertNotNull(metadata.locationsUpdateTime);
        assertNotNull(metadata.locationsVersion);
        assertNull(metadata.features);
    }
}
