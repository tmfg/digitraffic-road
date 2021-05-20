package fi.livi.digitraffic.tie.service.v1.location;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v1.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.model.v1.location.Location;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocationUpdaterTest extends AbstractServiceTest {

    @Autowired
    private LocationUpdater locationUpdater;

    @Autowired
    private LocationSubtypeRepository locationSubtypeRepository;

    private static final String VERSION = "1.1";

    private List<LocationSubtype> getSubtypes() {
        return locationSubtypeRepository.findAll().stream()
                .filter(s -> s.getId().getVersion().equals(VERSION))
                .collect(Collectors.toList());
    }

    @Test
    public void unknownSubtype() {
        assertThrows(IllegalArgumentException.class, () -> {
            final List<Location> locations = locationUpdater.updateLocations(getPath("/locations/locations_unknown_subtype.csv"), getSubtypes(), VERSION);
        });
    }

    @Test
    public void ok() {
        final List<Location> locations = locationUpdater.updateLocations(getPath("/locations/locations_ok.csv"), getSubtypes(), VERSION);
        assertThat(locations, Matchers.not(Matchers.empty()));
    }

    @Test
    public void illegalReference() {
        assertThrows(IllegalArgumentException.class, () -> {
            locationUpdater.updateLocations(getPath("/locations/locations_illegal_geocode.csv"), getSubtypes(), VERSION);
        });
    }

    @Test
    public void correctAreaReference() {
        final List<Location> locations = locationUpdater.updateLocations(getPath("/locations/locations_correct_area_reference.csv"), getSubtypes(), VERSION);
        assertThat(locations, Matchers.hasSize(3));
        assertThat(locations.get(0).getLocationCode(), Matchers.comparesEqualTo(21));
        assertThat(locations.get(1).getLocationCode(), Matchers.comparesEqualTo(23));
        assertThat(locations.get(2).getLocationCode(), Matchers.comparesEqualTo(22));
        assertThat(locations.get(0).getAreaRef(), Matchers.nullValue());
        assertThat(locations.get(1).getAreaRef(), Matchers.comparesEqualTo(22));
        assertThat(locations.get(1).getLinearRef(), Matchers.nullValue());
        assertThat(locations.get(2).getAreaRef(), Matchers.comparesEqualTo(21));
        assertThat(locations.get(2).getLinearRef(), Matchers.nullValue());
    }

    @Test
    public void correctLinearReference() {
        final List<Location> locations = locationUpdater.updateLocations(getPath("/locations/locations_correct_linear_reference.csv"), getSubtypes(), VERSION);
        assertThat(locations, Matchers.hasSize(3));
        assertThat(locations.get(0).getLocationCode(), Matchers.comparesEqualTo(31));
        assertThat(locations.get(1).getLocationCode(), Matchers.comparesEqualTo(33));
        assertThat(locations.get(2).getLocationCode(), Matchers.comparesEqualTo(32));
        assertThat(locations.get(0).getLinearRef(), Matchers.nullValue());
        assertThat(locations.get(1).getLinearRef(), Matchers.comparesEqualTo(32));
        assertThat(locations.get(1).getAreaRef(), Matchers.nullValue());
        assertThat(locations.get(2).getLinearRef(), Matchers.comparesEqualTo(31));
        assertThat(locations.get(2).getAreaRef(), Matchers.nullValue());
    }

}
