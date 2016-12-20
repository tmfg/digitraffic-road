package fi.livi.digitraffic.tie.metadata.service.location;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.AbstractTestBase;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.model.location.Location;

@Rollback
public class LocationUpdaterTest extends AbstractTestBase {
    @Autowired
    private LocationUpdater locationUpdater;

    @Autowired
    private LocationSubtypeRepository locationSubtypeRepository;

    private final static String VERSION = "VERSION";

    @Test(expected = IllegalArgumentException.class)
    @Transactional(readOnly = true)
    public void unknownSubtype() {
        final List<Location> locations = locationUpdater.updateLocations(getPath("/locations/locations_unknown_subtype.csv"), locationSubtypeRepository.findAll(), VERSION);
        Assert.assertThat(locations, Matchers.not(Matchers.empty()));
    }

    @Test
    @Transactional(readOnly = true)
    public void ok() {
        final List<Location> locations = locationUpdater.updateLocations(getPath("/locations/locations_ok.csv"), locationSubtypeRepository.findAll(), VERSION);
        Assert.assertThat(locations, Matchers.not(Matchers.empty()));
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional(readOnly = true)
    public void illegalReference() {
        locationUpdater.updateLocations(getPath("/locations/locations_illegal_geocode.csv"), locationSubtypeRepository.findAll(), VERSION);
    }

    @Test
    @Transactional(readOnly = true)
    public void correctReference() {
        final List<Location> locations = locationUpdater.updateLocations(getPath("/locations/locations_correct_reference.csv"), locationSubtypeRepository.findAll(), VERSION);
        Assert.assertThat(locations, Matchers.hasSize(3));
        Assert.assertThat(locations.get(0).getLocationCode(), Matchers.comparesEqualTo(1));
        Assert.assertThat(locations.get(1).getLocationCode(), Matchers.comparesEqualTo(3));
        Assert.assertThat(locations.get(2).getLocationCode(), Matchers.comparesEqualTo(2));
        Assert.assertThat(locations.get(0).getAreaRef(), Matchers.nullValue());
        Assert.assertThat(locations.get(1).getAreaRef(), Matchers.notNullValue());
        Assert.assertThat(locations.get(1).getAreaRef(), Matchers.comparesEqualTo(2));
        Assert.assertThat(locations.get(2).getAreaRef(), Matchers.notNullValue());
        Assert.assertThat(locations.get(2).getAreaRef(), Matchers.comparesEqualTo(1));
    }

}
