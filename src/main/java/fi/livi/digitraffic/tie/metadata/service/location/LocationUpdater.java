package fi.livi.digitraffic.tie.metadata.service.location;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationRepository;
import fi.livi.digitraffic.tie.metadata.model.location.Location;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

@Service
public class LocationUpdater {
    private final LocationRepository locationRepository;

    public LocationUpdater(final LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void updateLocations(final Path path, final List<LocationSubtype> locationSubtypes) {
        final List<Location> oldLocations = locationRepository.findAll();
        final Map<Integer, Location> oldMap = oldLocations.parallelStream().collect(Collectors.toMap(Location::getLocationCode, Function.identity()));
        final List<Location> newLocations = getLocations(oldMap, path, locationSubtypes);

        mergeLocations(oldMap, newLocations);
    }

    public List<Location> getLocations(final Map<Integer, Location> oldMap, final Path path, final List<LocationSubtype> locationSubtypes) {
        final Map<String, LocationSubtype> subtypeMap = locationSubtypes.stream().collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));

        final LocationReader reader = new LocationReader(oldMap, subtypeMap);

        return reader.read(path);
    }

    private void mergeLocations(final Map<Integer, Location> oldMap, final List<Location> newLocations) {
        final List<Location> newList = new ArrayList<>();

        newLocations.stream().forEach(l -> {
            if(!oldMap.containsKey(l.getLocationCode())) {
                newList.add(l);
            } else {
                mergeLocation(oldMap.get(l.getLocationCode()), l);
            }

            // remove from oldMap, if added or modified
            oldMap.remove(l.getLocationCode());
        });

        // values in oldMap can be removed, they no longes exist
        locationRepository.delete(oldMap.values());
        
        locationRepository.save(newList);
    }

    private void mergeLocation(final Location oldLocation, final Location newLocation) {
        BeanUtils.copyProperties(newLocation, oldLocation);
    }
}
