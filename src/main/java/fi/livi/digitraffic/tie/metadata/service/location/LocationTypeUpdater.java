package fi.livi.digitraffic.tie.metadata.service.location;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationTypeRepository;
import fi.livi.digitraffic.tie.metadata.model.location.LocationType;

@Service
public class LocationTypeUpdater {
    private final LocationTypeReader locationTypeReader;

    private final LocationTypeRepository locationTypeRepository;

    public LocationTypeUpdater(final LocationTypeReader locationTypeReader,
                               final LocationTypeRepository locationTypeRepository) {
        this.locationTypeReader = locationTypeReader;
        this.locationTypeRepository = locationTypeRepository;
    }

    public void updateLocationTypes(final Path path) {
        final List<LocationType> oldTypes = locationTypeRepository.findAll();
        final List<LocationType> newTypes = locationTypeReader.readLocationTypes(path);

        mergeLocationTypes(oldTypes, newTypes);
    }

    private void mergeLocationTypes(final List<LocationType> oldTypes, final List<LocationType> newTypes) {
        final Map<String, LocationType> oldMap = oldTypes.stream().collect(Collectors.toMap(LocationType::getTypeCode, Function.identity()));
        final List<LocationType> newList = new ArrayList<>();

        newTypes.stream().forEach(t -> {
            if(!oldMap.containsKey(t.getTypeCode())) {
                newList.add(t);
            } else {
                mergeLocationType(oldMap.get(t.getTypeCode()), t);
            }

            // remove from oldMap, if added or modified
            oldMap.remove(t.getTypeCode());
        });

        // values in oldMap can be removed, they no longes exist
        locationTypeRepository.delete(oldMap.values());

        locationTypeRepository.save(newList);
    }

    private void mergeLocationType(final LocationType oldType, final LocationType newType) {
        oldType.setDescriptionFi(newType.getDescriptionFi());
        oldType.setDescriptionEn(newType.getDescriptionEn());
    }
}
