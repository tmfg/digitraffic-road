package fi.livi.digitraffic.tie.metadata.service.location;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

@Service
public class LocationSubtypeUpdater {
    private final LocationSubtypeReader locationSubtypeReader;

    private final LocationSubtypeRepository locationSubtypeRepository;

    public LocationSubtypeUpdater(final LocationSubtypeReader locationSubtypeReader,
                                  final LocationSubtypeRepository locationSubtypeRepository) {
        this.locationSubtypeReader = locationSubtypeReader;
        this.locationSubtypeRepository = locationSubtypeRepository;
    }

    public List<LocationSubtype> updateLocationSubtypes(final Path path) {
        final List<LocationSubtype> oldTypes = locationSubtypeRepository.findAll();
        final List<LocationSubtype> newTypes = locationSubtypeReader.read(path);

        return mergeLocationSubtypes(oldTypes, newTypes);
    }

    private List<LocationSubtype>  mergeLocationSubtypes(final List<LocationSubtype> oldTypes, final List<LocationSubtype> newTypes) {
        final Map<String, LocationSubtype> oldMap = oldTypes.stream().collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));
        final List<LocationSubtype> newList = new ArrayList<>();

        newTypes.stream().forEach(t -> {
            if(!oldMap.containsKey(t.getSubtypeCode())) {
                newList.add(t);
            } else {
                mergeLocationSubtype(oldMap.get(t.getSubtypeCode()), t);
            }

            // remove from oldMap, if added or modified
            oldMap.remove(t.getSubtypeCode());
        });

        // values in oldMap can be removed, they no longes exist
        locationSubtypeRepository.delete(oldMap.values());

        locationSubtypeRepository.save(newList);

        return newTypes;
    }

    private void mergeLocationSubtype(final LocationSubtype oldType, final LocationSubtype newType) {
        BeanUtils.copyProperties(newType, oldType);
    }
}
