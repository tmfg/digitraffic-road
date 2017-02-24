package fi.livi.digitraffic.tie.metadata.service.location;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationRepository;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationTypeRepository;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationVersionRepository;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationFeature;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationJson;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationTypesMetadata;
import fi.livi.digitraffic.tie.metadata.model.location.LocationVersion;

@Service
public class LocationService {
    private final LocationTypeRepository locationTypeRepository;
    private final LocationSubtypeRepository locationSubtypeRepository;
    private final LocationRepository locationRepository;
    private final LocationVersionRepository locationVersionRepository;

    public static final String LATEST="latest";

    public LocationService(final LocationTypeRepository locationTypeRepository,
                           final LocationSubtypeRepository locationSubtypeRepository,
                           final LocationRepository locationRepository,
                           final LocationVersionRepository locationVersionRepository) {
        this.locationTypeRepository = locationTypeRepository;
        this.locationSubtypeRepository = locationSubtypeRepository;
        this.locationRepository = locationRepository;
        this.locationVersionRepository = locationVersionRepository;
    }

    @Transactional(readOnly = true)
    public LocationFeatureCollection findLocationsMetadata(final boolean onlyUpdateInfo, final String version) {
        final LocationVersion locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.getVersion();

        if(onlyUpdateInfo) {
            return new LocationFeatureCollection(locationVersion.getUpdated(), lVersion);
        }

        return new LocationFeatureCollection(locationVersion.getUpdated(), lVersion,
                locationRepository.findAllByVersion(lVersion).parallelStream().map(LocationFeature::new).collect(Collectors.toList())
        );
    }

    private static boolean isLatestVersion(final String version) {
        return StringUtils.isEmpty(version) || version.equalsIgnoreCase(LATEST);
    }

    private LocationVersion getLocationVersion(final String version) {
        final LocationVersion locationVersion = isLatestVersion(version) ?
                                                locationVersionRepository.findLatestVersion() :
                                                locationVersionRepository.findOne(version);

        if(locationVersion == null) {
            throw new ObjectNotFoundException(LocationVersion.class, version);
        }

        return locationVersion;
    }

    @Transactional(readOnly = true)
    public LocationFeatureCollection findLocation(final int id, final String version) {
        final LocationVersion locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.getVersion();

        final LocationJson location = locationRepository.findByVersionAndLocationCode(lVersion, id);

        if(location == null) {
            throw new ObjectNotFoundException("Location", id);
        }

        return new LocationFeatureCollection(locationVersion.getUpdated(), lVersion,
                Collections.singletonList(new LocationFeature(location)));
    }

    @Transactional(readOnly = true)
    public LocationTypesMetadata findLocationSubtypes(final boolean lastUpdated, final String version) {
        final LocationVersion locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.getVersion();

        if(lastUpdated) {
            return new LocationTypesMetadata(locationVersion.getUpdated(), lVersion);
        }

        return new LocationTypesMetadata(locationVersion.getUpdated(), lVersion,
                locationTypeRepository.findAllByIdVersion(lVersion),
                locationSubtypeRepository.findAllByIdVersion(lVersion));
    }

    @Transactional(readOnly = true)
    public List<LocationVersion> findLocationVersions() {
        return locationVersionRepository.findAll();
    }
}
