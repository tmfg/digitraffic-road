package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationRepository;
import fi.livi.digitraffic.tie.metadata.model.location.Location;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;
import fi.livi.digitraffic.tie.metadata.model.location.LocationType;

@Service
public class LocationUpdater {
    private final LocationRepository locationRepository;

    public LocationUpdater(final LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void updateLocations(final Path path, List<LocationType> locationTypes, List<LocationSubtype> locationSubtypes) throws IOException, SAXException {
        final List<Location> oldLocations = locationRepository.findAll();
        final List<Location> newLocations = getLocations(oldLocations, path, locationSubtypes);

        mergeLocations(oldLocations, newLocations);
    }

    public List<Location> getLocations(final List<Location> oldLocations, final Path path, List<LocationSubtype> locationSubtypes) {
        final Map<Integer, Location> locationMap = oldLocations.stream().collect(Collectors.toMap(Location::getLocationCode, Function.identity()));
        final Map<String, LocationSubtype> subtypeMap = locationSubtypes.stream().collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));

        final LocationReader reader = new LocationReader(locationMap, subtypeMap);

        return reader.read(path);
    }

    private void mergeLocations(final List<Location> oldLocations, final List<Location> newLocations) {
        final Map<Integer, Location> oldMap = oldLocations.stream().collect(Collectors.toMap(Location::getLocationCode, Function.identity()));
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
