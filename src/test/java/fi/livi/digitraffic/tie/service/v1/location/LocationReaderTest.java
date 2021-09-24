package fi.livi.digitraffic.tie.service.v1.location;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.v1.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.model.v1.location.Location;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;

public class LocationReaderTest extends AbstractJpaTest {
    @Autowired
    private LocationSubtypeRepository locationSubtypeRepository;

    private Map<String, LocationSubtype> subtypeMap;

    private final static String VERSION = "VERSION";

    @BeforeEach
    public void setUp() {
        final List<LocationSubtype> locationSubtypes = locationSubtypeRepository.findAll();

        subtypeMap = locationSubtypes.stream()
                .filter(s -> s.getId().getVersion().equals("1.1"))
                .collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));
    }

    @Test
    public void emptyLocationsFile() {
        final LocationReader reader = new LocationReader(subtypeMap, VERSION);

        final List<Location> locations = reader.read(TestUtils.getPath("/locations/locations_empty.csv"));
        assertThat(locations, Matchers.empty());
    }

    @Test
    public void illegalGeocode() {
        final LocationReader reader = new LocationReader(subtypeMap, VERSION);
        final LocationReader spyReader = Mockito.spy(reader);

        final List<Location> locations = spyReader.read(TestUtils.getPath("/locations/locations_illegal_geocode.csv"));
        assertThat(locations, Matchers.hasSize(2));
        assertThat(locations.get(0).getGeocode(), Matchers.equalTo("test"));
        assertThat(locations.get(1).getGeocode(), Matchers.emptyOrNullString());

        //Mockito.verify(spyReader).log.error(anyString());
    }

    @Test
    public void illegalSubtype() {
        final LocationReader reader = new LocationReader(subtypeMap, VERSION);

        assertThrows(IllegalArgumentException.class, () -> {
            reader.read(TestUtils.getPath("/locations/locations_illegal_subtype.csv"));
        });
    }
}
