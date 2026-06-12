package fi.livi.digitraffic.tie.service.trafficmessage.location;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.model.trafficmessage.location.Location;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationSubtype;

@Transactional
public class LocationUpdaterTest extends AbstractServiceTest {

    @Autowired
    private LocationUpdater locationUpdater;

    @Autowired
    private LocationSubtypeRepository locationSubtypeRepository;

    private static final String VERSION = "1.1";
    private static final String TEST_SUBTYPES_SOURCE = "SUBTYPES.DAT";

    private List<LocationSubtype> getSubtypes() {
        return locationSubtypeRepository.findAll().stream()
                .filter(s -> s.getId().getVersion().equals(VERSION))
                .collect(Collectors.toList());
    }

    @Test
    public void unknownSubtype() {
        final ParseResult<Location> result = locationUpdater.updateLocations(
                TestUtils.getPath("/locations/locations_unknown_subtype.csv"), "test-source", getSubtypes(), TEST_SUBTYPES_SOURCE, VERSION);
        assertTrue(result.hasErrors(), "Expected parse errors for unknown subtype");
    }

    @Test
    public void illegalReference() {
        final ParseResult<Location> result = locationUpdater.updateLocations(
                TestUtils.getPath("/locations/locations_illegal_geocode.csv"), "test-source", getSubtypes(), TEST_SUBTYPES_SOURCE, VERSION);
        assertTrue(result.hasErrors(), "Expected reference errors for missing area reference");
    }

    @Test
    public void missingAreaReference() {
        final ParseResult<Location> result = locationUpdater.updateLocations(
                TestUtils.getPath("/locations/locations_illegal_reference.csv"), "test-source", getSubtypes(), TEST_SUBTYPES_SOURCE, VERSION);
        assertTrue(result.hasErrors(), "Expected reference errors for missing area reference");
        assertTrue(result.parseErrors().stream().anyMatch(e -> e.contains("area reference")),
                "Expected area reference error in: " + result.parseErrors());
    }

    @Test
    public void illegalSubtype() {
        final ParseResult<Location> result = locationUpdater.updateLocations(
                TestUtils.getPath("/locations/locations_illegal_subtype.csv"), "test-source", getSubtypes(), TEST_SUBTYPES_SOURCE, VERSION);
        assertTrue(result.hasErrors(), "Expected parse errors for unknown subtype Z3.0");
        assertTrue(result.parseErrors().stream().anyMatch(e -> e.contains("Z3.0")),
                "Expected Z3.0 error in: " + result.parseErrors());
    }

    @Test
    public void ok() {
        final List<Location> locations = locationUpdater.updateLocations(TestUtils.getPath("/locations/locations_ok.csv"), "test-source", getSubtypes(), TEST_SUBTYPES_SOURCE, VERSION).items();
        assertThat(locations, Matchers.not(Matchers.empty()));
    }


    @Test
    public void correctAreaReference() {
        final List<Location> locations = locationUpdater.updateLocations(TestUtils.getPath("/locations/locations_correct_area_reference.csv"), "test-source", getSubtypes(), TEST_SUBTYPES_SOURCE, VERSION).items();
        assertThat(locations, Matchers.hasSize(3));
        assertThat(locations.getFirst().getLocationCode(), Matchers.comparesEqualTo(21));
        assertThat(locations.get(1).getLocationCode(), Matchers.comparesEqualTo(23));
        assertThat(locations.get(2).getLocationCode(), Matchers.comparesEqualTo(22));
        assertThat(locations.getFirst().getAreaRef(), Matchers.nullValue());
        assertThat(locations.get(1).getAreaRef(), Matchers.comparesEqualTo(22));
        assertThat(locations.get(1).getLinearRef(), Matchers.nullValue());
        assertThat(locations.get(2).getAreaRef(), Matchers.comparesEqualTo(21));
        assertThat(locations.get(2).getLinearRef(), Matchers.nullValue());
    }

    @Test
    public void correctLinearReference() {
        final List<Location> locations = locationUpdater.updateLocations(TestUtils.getPath("/locations/locations_correct_linear_reference.csv"), "test-source", getSubtypes(), TEST_SUBTYPES_SOURCE, VERSION).items();
        assertThat(locations, Matchers.hasSize(3));
        assertThat(locations.getFirst().getLocationCode(), Matchers.comparesEqualTo(31));
        assertThat(locations.get(1).getLocationCode(), Matchers.comparesEqualTo(33));
        assertThat(locations.get(2).getLocationCode(), Matchers.comparesEqualTo(32));
        assertThat(locations.getFirst().getLinearRef(), Matchers.nullValue());
        assertThat(locations.get(1).getLinearRef(), Matchers.comparesEqualTo(32));
        assertThat(locations.get(1).getAreaRef(), Matchers.nullValue());
        assertThat(locations.get(2).getLinearRef(), Matchers.comparesEqualTo(31));
        assertThat(locations.get(2).getAreaRef(), Matchers.nullValue());
    }

}
