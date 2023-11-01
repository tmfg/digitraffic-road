package fi.livi.digitraffic.tie.service.trafficmessage.v1.location;

import static fi.livi.digitraffic.tie.service.trafficmessage.v1.location.LocationWebServiceV1.LATEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationTypesDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationVersionDtoV1;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

public class LocationWebServiceV1Test extends AbstractServiceTest {
    @Autowired
    private LocationWebServiceV1 locationService;

    private static final String VERSION0 = "1.0";
    private static final String VERSION1 = "1.1";

    @Test
    public void findLocationOkLatest() {
        assertNotNull(locationService.getLocationById(12187, LATEST));
    }

    @Test
    public void findLocationOkEmpty() {
        assertNotNull(locationService.getLocationById(12187, ""));
    }

    @Test
    public void findLocationOkVersion1() {
        assertNotNull(locationService.getLocationById(1, VERSION1));
    }

    @Test
    public void findLocationOkVersion2() {
        assertNotNull(locationService.getLocationById(1, VERSION0));
    }

    @Test
    public void findLocationNotFound() {
        assertThrows(ObjectNotFoundException.class, () -> locationService.getLocationById(-222, LATEST));
    }

    @Test
    public void findLocationNotFoundFromOtherVersion() {
        assertThrows(ObjectNotFoundException.class, () -> locationService.getLocationById(12187, VERSION0));
    }

    @Test
    public void findLocationVersionNotFound() {
        assertThrows(ObjectNotFoundException.class, () -> locationService.getLocationById(12187, "not_found"));
    }

    @Test
    public void findLocationSubtypesFromLatest() {
        final LocationTypesDtoV1 metadata = locationService.findLocationTypes(false, LATEST);
        assertNotNull(metadata);
        assertNotNull(metadata.getDataUpdatedTime());
        assertNotNull(metadata.version);
        assertThat(metadata.locationSubtypes, hasSize(7));
        assertThat(metadata.locationTypes, hasSize(7));
    }

    @Test
    public void findLocationSubtypesFromVersion2() {
        final LocationTypesDtoV1 metadata = locationService.findLocationTypes(false, VERSION0);
        assertNotNull(metadata);
        assertNotNull(metadata.getDataUpdatedTime());
        assertNotNull(metadata.version);
        assertThat(metadata.locationSubtypes, hasSize(1));
        assertThat(metadata.locationTypes, hasSize(1));
    }

    @Test
    public void findLocationSubtypesLastUpdated() {
        final LocationTypesDtoV1 metadata = locationService.findLocationTypes(true, LATEST);
        assertNotNull(metadata);
        assertNotNull(metadata.getDataUpdatedTime());
        assertNotNull(metadata.version);
        assertNull(metadata.locationSubtypes);
        assertNull(metadata.locationTypes);
    }

    @Test
    public void findLocations() {
        final LocationFeatureCollectionV1 metadata = locationService.findLocations(false, LATEST);
        assertNotNull(metadata);
        assertNotNull(metadata.dataUpdatedTime);
        assertNotNull(metadata.locationsVersion);
        assertThat(metadata.getFeatures(), not(empty()));
    }

    @Test
    public void findLocationsVersion2() {
        final LocationFeatureCollectionV1 metadata = locationService.findLocations(false, VERSION0);
        assertNotNull(metadata);
        assertNotNull(metadata.dataUpdatedTime);
        assertNotNull(metadata.locationsVersion);
        assertThat(metadata.getFeatures(), hasSize(1));
    }

    @Test
    public void findLocationsLastUpdated() {
        final LocationFeatureCollectionV1 metadata = locationService.findLocations(true, LATEST);
        assertNotNull(metadata);
        assertNotNull(metadata.dataUpdatedTime);
        assertNotNull(metadata.locationsVersion);
        assertNull(metadata.getFeatures());
    }

    @Test
    public void findLocationVersions() {
        final List<LocationVersionDtoV1> locationVersions = locationService.findLocationVersions();
        assertNotNull(locationVersions);
        assertThat(locationVersions, hasSize(2));
    }
}
