package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.model.trafficmessage.location.Location;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationSubtype;
import fi.livi.digitraffic.common.util.StringUtil;
import jakarta.persistence.EntityManager;

@ConditionalOnNotWebApplication
@Service
public class LocationUpdater {
    private final EntityManager entityManager;

    private final int batchSize;

    public LocationUpdater(final EntityManager entityManager, @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    final int batchSize) {
        this.entityManager = entityManager;
        this.batchSize = batchSize;
    }

    @Transactional
    public ParseResult<Location> updateLocations(final Path path, final String source,
                                                 final List<LocationSubtype> locationSubtypes,
                                                 final String subtypesSource,
                                                 final String version) {
        final Map<String, LocationSubtype> subtypeMap = locationSubtypes.stream()
                .collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));
        final LocationReader reader = new LocationReader(subtypeMap, version, subtypesSource);
        final List<Location> locations = reader.read(path.toFile(), source);

        final List<String> referenceErrors = setReferences(locations, reader.getAreaRefMap(), reader.getLinearRefMap());

        // Persist all successfully-parsed locations. Even if there are parse/reference errors the
        // persist and flush still run here, but the caller (LocationMetadataUpdater.updateAll) will
        // throw an IllegalStateException when it sees hasErrors(), which causes Spring to roll back
        // the whole transaction – so nothing is ever committed to the DB on a partial import.
        for (int i = 0; i < locations.size(); i++) {
            entityManager.persist(locations.get(i));
            if (batchSize > 0 && (i + 1) % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        final List<String> allErrors = new ArrayList<>(reader.getParseErrors());
        allErrors.addAll(referenceErrors);
        return new ParseResult<>(locations, allErrors);
    }

    /**
     * Resolves area and linear cross-references between parsed locations.
     * Returns a list of error messages for every unresolvable reference instead of throwing,
     * so the caller can include them in the full parse report.
     */
    private List<String> setReferences(final List<Location> newLocations,
                                       final Map<Integer, Integer> areaRefMap,
                                       final Map<Integer, Integer> linearRefMap) {
        final List<String> errors = new ArrayList<>();
        final Map<Integer, Location> newMap = newLocations.parallelStream()
                .collect(Collectors.toMap(Location::getLocationCode, Function.identity()));

        areaRefMap.forEach((id, areaRefId) -> {
            final Location location = newMap.get(id);
            if (location == null) {
                errors.add(StringUtil.format("locationCode={} cause=location missing from parsed set (area-ref={})", id, areaRefId));
                return;
            }
            if (newMap.get(areaRefId) == null) {
                errors.add(StringUtil.format("locationCode={} cause=could not find area reference {}", id, areaRefId));
                return;
            }
            location.setAreaRef(areaRefId);
        });

        linearRefMap.forEach((id, linearRefId) -> {
            final Location location = newMap.get(id);
            if (location == null) {
                errors.add(StringUtil.format("locationCode={} cause=location missing from parsed set (linear-ref={})", id, linearRefId));
                return;
            }
            if (newMap.get(linearRefId) == null) {
                errors.add(StringUtil.format("locationCode={} cause=could not find linear reference {}", id, linearRefId));
                return;
            }
            location.setLinearRef(linearRefId);
        });

        return errors;
    }
}
