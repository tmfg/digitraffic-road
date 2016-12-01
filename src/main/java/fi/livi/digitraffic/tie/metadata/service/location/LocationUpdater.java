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

    public List<Location> updateLocations(final Path path, final List<LocationSubtype> locationSubtypes) {
        final List<Location> oldLocations = locationRepository.findAll();
        final Map<Integer, Location> oldMap = oldLocations.parallelStream().collect(Collectors.toMap(Location::getLocationCode, Function.identity()));
        final List<Location> newLocations = getLocations(path, locationSubtypes);

        mergeLocations(oldMap, newLocations);

        return newLocations;
    }

    public List<Location> getLocations(final Path path, final List<LocationSubtype> locationSubtypes) {
        final Map<String, LocationSubtype> subtypeMap = locationSubtypes.stream().collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));

        final LocationReader reader = new LocationReader(subtypeMap);
        final List<Location> locations = reader.read(path);

        setReferences(locations, reader.getAreaRefMap(), reader.getLinearRefMap());

        return locations;
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

    private void setReferences(final List<Location> newLocations, final Map<Integer, Integer> areaRefMap, final Map<Integer, Integer> linearRefMap) {
        final Map<Integer, Location> newMap = newLocations.parallelStream().collect(Collectors.toMap(Location::getLocationCode, Function.identity()));

        areaRefMap.forEach((id, areaRefId) -> {
            final Location location = newMap.get(id);
            final Location areaRef = newMap.get(areaRefId);

            if(location == null) {
                throw new IllegalArgumentException();
            }

            if(areaRef == null) {
                throw new IllegalArgumentException("could not find area reference " + areaRefId);
            }

            location.setAreaRef(areaRef);
        });

        linearRefMap.forEach((id, linearRefId) -> {
            final Location location = newMap.get(id);
            final Location linearRef = newMap.get(linearRefId);

            if(location == null) {
                throw new IllegalArgumentException();
            }

            if(linearRef == null) {
                throw new IllegalArgumentException("could not find linear reference " + linearRefId);
            }

            location.setLinearRef(linearRef);
        });
    }

    private void mergeLocation(final Location oldLocation, final Location newLocation) {
        BeanUtils.copyProperties(newLocation, oldLocation, "locationCode");
    }
}
