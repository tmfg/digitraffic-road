package fi.livi.digitraffic.tie.metadata.service.location;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationRepository;
import fi.livi.digitraffic.tie.metadata.model.location.Location;

@Service
public class LocationUpdater {
    private final LocationRepository locationRepository;

    private final LocationReader locationReader;

    public LocationUpdater(final LocationRepository locationRepository,
                           final LocationReader locationReader) {
        this.locationRepository = locationRepository;
        this.locationReader = locationReader;
    }

    public void updateLocations(final Path path) {
        final List<Location> oldLocations = locationRepository.findAll();
        final List<Location> newLocations = locationReader.readLocations(oldLocations, path);

        mergeLocations(oldLocations, newLocations);
    }

    private void mergeLocations(final List<Location> oldLocations, final List<Location> newLocations) {


    }
}
