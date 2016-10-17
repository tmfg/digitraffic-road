package fi.livi.digitraffic.tie.metadata.service.location;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        oldLocation.setLocationSubtype(newLocation.getLocationSubtype());
        oldLocation.setRoadName(newLocation.getRoadName());
        oldLocation.setFirstName(newLocation.getFirstName());
        oldLocation.setSecondName(newLocation.getSecondName());
        oldLocation.setAreaRef(newLocation.getAreaRef());
        oldLocation.setLinearRef(newLocation.getLinearRef());
        oldLocation.setNegOffset(newLocation.getNegOffset());
        oldLocation.setPosOffset(newLocation.getPosOffset());
        oldLocation.setUrban(newLocation.getUrban());
        oldLocation.setWsg84Lat(newLocation.getWsg84Lat());
        oldLocation.setWsg84Long(newLocation.getWsg84Long());
    }
}
