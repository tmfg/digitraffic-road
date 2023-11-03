package fi.livi.digitraffic.tie.service.trafficmessage.v1.location;

import static fi.livi.digitraffic.tie.helper.DateHelper.withoutMillis;
import static org.springframework.data.domain.Sort.Order.asc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationRepository;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationTypeRepository;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationVersionRepository;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationFeatureV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationSubtypeDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationTypeDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationTypesDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationVersionDtoV1;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationVersion;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@Service
public class LocationWebServiceV1 {
    private final LocationTypeRepository locationTypeRepository;
    private final LocationSubtypeRepository locationSubtypeRepository;
    private final LocationRepository locationRepository;
    private final LocationVersionRepository locationVersionRepository;

    public static final String LATEST="latest";

    public LocationWebServiceV1(final LocationTypeRepository locationTypeRepository,
                                final LocationSubtypeRepository locationSubtypeRepository,
                                final LocationRepository locationRepository,
                                final LocationVersionRepository locationVersionRepository) {
        this.locationTypeRepository = locationTypeRepository;
        this.locationSubtypeRepository = locationSubtypeRepository;
        this.locationRepository = locationRepository;
        this.locationVersionRepository = locationVersionRepository;
    }

    @PerformanceMonitor(maxInfoExcecutionTime = 100000, maxWarnExcecutionTime = 3000)
    @Transactional(readOnly = true)
    public LocationFeatureCollectionV1 findLocations(final boolean onlyUpdateInfo, final String version) {
        final LocationVersionDtoV1 locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.version;
        final Instant updated = locationVersion.getDataUpdatedTime();
        if (onlyUpdateInfo) {
            return new LocationFeatureCollectionV1(updated, lVersion);
        }

        final List<LocationFeatureV1> features =
            locationRepository.findAllByVersion(lVersion)
                .parallel().map((LocationDtoV1 l) -> new LocationFeatureV1(l, updated, lVersion))
                .collect(Collectors.toList());
        Collections.sort(features);
        return new LocationFeatureCollectionV1(updated, lVersion, features);
    }

    @Transactional(readOnly = true)
    public LocationFeatureV1 getLocationById(final int id, final String version) {
        final LocationVersionDtoV1 locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.version;

        final LocationDtoV1 location = locationRepository.findByVersionAndLocationCode(lVersion, id);

        if (location == null) {
            throw new ObjectNotFoundException("Location", id);
        }

        return new LocationFeatureV1(location, locationVersion.getDataUpdatedTime(), lVersion);
    }

    @Transactional(readOnly = true)
    public LocationTypesDtoV1 findLocationTypes(final boolean lastUpdated, final String version) {
        final LocationVersionDtoV1 locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.version;

        if(lastUpdated) {
            return new LocationTypesDtoV1(locationVersion.getDataUpdatedTime(), lVersion);
        }

        final List<LocationTypeDtoV1> types =
            locationTypeRepository.findAllByIdVersionOrderByIdTypeCode(lVersion);
        Collections.sort(types);
        final List<LocationSubtypeDtoV1> suptypes =
            locationSubtypeRepository.findAllByIdVersionOrderByIdSubtypeCode(lVersion);
        Collections.sort(suptypes);
        return new LocationTypesDtoV1(
            locationVersion.getDataUpdatedTime(),
            lVersion,
            types,
            suptypes);
    }

    @Transactional(readOnly = true)
    public List<LocationVersionDtoV1> findLocationVersions() {
        return locationVersionRepository.findAll(Sort.by(asc("version")))
            .stream()
            .map(v -> new LocationVersionDtoV1(v.getVersion(), v.getModified()))
            .sorted()
            .collect(Collectors.toList());
    }

    private static boolean isLatestVersion(final String version) {
        return StringUtils.isEmpty(version) || version.equalsIgnoreCase(LATEST);
    }

    private LocationVersionDtoV1 getLocationVersion(final String version) {
        final LocationVersion locationVersion = isLatestVersion(version) ?
                                                locationVersionRepository.findLatestVersion() :
                                                locationVersionRepository.findById(version).orElse(null);

        if(locationVersion == null) {
            throw new ObjectNotFoundException("LocationVersion", version);
        }

        return new LocationVersionDtoV1(locationVersion.getVersion(), withoutMillis(locationVersion.getModified()));
    }

}
