package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.nio.file.Path;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationSubtype;

@ConditionalOnNotWebApplication
@Service
public class LocationSubtypeUpdater {
    private final LocationSubtypeRepository locationSubtypeRepository;

    public LocationSubtypeUpdater(final LocationSubtypeRepository locationSubtypeRepository) {
        this.locationSubtypeRepository = locationSubtypeRepository;
    }

    @Transactional
    public List<LocationSubtype> updateLocationSubtypes(final Path path, final String version) {
        final LocationSubtypeReader locationSubtypeReader = new LocationSubtypeReader(version);
        final List<LocationSubtype> newTypes = locationSubtypeReader.read(path);

        locationSubtypeRepository.saveAll(newTypes);

        return newTypes;
    }
}
