package fi.livi.digitraffic.tie.service.trafficmessage.v1;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.dao.trafficmessage.RegionGeometryRepository;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AreaType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryProperties;
import fi.livi.digitraffic.tie.helper.GeometryConstants;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@Service
public class RegionGeometryDataServiceV1 {
    private static final Logger log = LoggerFactory.getLogger(RegionGeometryDataServiceV1.class);

    private final static GeoJsonWriter geoJsonWriter;
    private final static ObjectReader geometryReader;

    private final RegionGeometryRepository regionGeometryRepository;
    private final DataStatusService dataStatusService;

    private RegionStatus regionStatus = new RegionStatus();

    static {
        geoJsonWriter = new GeoJsonWriter(GeometryConstants.COORDINATE_SCALE_6_DIGITS);
        // Don't add crs to geometries as it's always EPSG:4326
        geoJsonWriter.setEncodeCRS(false);
        geometryReader = new ObjectMapper().readerFor(Geometry.class);
    }

    @Autowired
    public RegionGeometryDataServiceV1(final RegionGeometryRepository regionGeometryRepository,
                                       final DataStatusService dataStatusService) {
        this.regionGeometryRepository = regionGeometryRepository;
        this.dataStatusService = dataStatusService;
    }

    // Update Every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void refreshCache() {
        final StopWatch start = StopWatch.createStarted();
        try {
            final String latestCommitId = getLatestCommitId();
            if (StringUtils.equals(regionStatus.currentCommitId, latestCommitId)) {
                log.info("method=refreshCache No changes currentCommitId {} and latestCommitId {} are the same", regionStatus.currentCommitId, latestCommitId);
                return;
            }
            final List<RegionGeometry> regions = regionGeometryRepository.findAllByOrderByIdAsc();
            // Use TreeMap to preserve asc order by location code
            final Map<Integer, List<RegionGeometry>> locationCodeToRegion = new TreeMap<>();

            regions.forEach(regionGeometry -> {
                // Add only versions that are valid (type is missing for 1. versions)
                if (regionGeometry.isValid()) {
                    final Integer locationCode = regionGeometry.getLocationCode();
                    if (!locationCodeToRegion.containsKey(locationCode)) {
                        locationCodeToRegion.put(locationCode, new ArrayList<>());
                    }
                    // Add latest as 1st element -> will be in desc order
                    locationCodeToRegion.get(locationCode).add(0, regionGeometry);
                    log.info("method=refreshCache Added version {}", regionGeometry);
                } else {
                    log.info("method=refreshCache Not adding version with illegal type {}", regionGeometry);
                }
            });
            removeNeverValidValues(locationCodeToRegion);
            regionStatus = new RegionStatus(locationCodeToRegion,
                latestCommitId,
                dataStatusService.findDataUpdatedInstant(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA),
                dataStatusService.findDataUpdatedTime(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA_CHECK));

        } catch (final Exception e) {
            log.error("method=refreshAreaLocationRegionCache failed", e);
        }
        log.info("method=refreshAreaLocationRegionCache done tookMs={}", start.getTime());
    }

    @Transactional
    public String getLatestCommitId() {
        return regionGeometryRepository.getLatestCommitId();
    }

    @NotTransactionalServiceMethod
    public RegionGeometryFeatureCollection findAreaLocationRegions(final boolean onlyUpdateInfo, final boolean includeGeometry, final Instant effectiveDate, final Integer id) {
        final List<RegionGeometryFeature> geometries =
            onlyUpdateInfo ? Collections.emptyList() : filterRegionsAndConvertToDto(effectiveDate, includeGeometry,
                                                                                    (id != null ? new Integer[] { id } : new Integer[0]));
        if (geometries.isEmpty() && id != null && !onlyUpdateInfo) {
            throw new ObjectNotFoundException("RegionGeometry", id);
        }
        return new RegionGeometryFeatureCollection(regionStatus.updated, geometries);
    }

    @NotTransactionalServiceMethod
    public RegionGeometry getAreaLocationRegionEffectiveOn(final int locationCode, final Instant theMoment) {
        final List<RegionGeometry> regionsInDescOrder = regionStatus.getRegionVersionsInDescOrder(locationCode);
        if (CollectionUtils.isEmpty(regionsInDescOrder)) {
            log.warn("method=getAreaLocationRegionEffectiveOn No location with locationCode {} found", locationCode);
            return null;
        }
        // Find latest version that is valid on given moment or the first version
        return regionsInDescOrder.stream()
            .filter(r -> r.getEffectiveDate().getEpochSecond() <= theMoment.getEpochSecond())
            .findFirst()
            .orElse(regionsInDescOrder.get(0));
    }

    @NotTransactionalServiceMethod
    public Geometry<?> getGeoJsonGeometryUnion(final Instant effectiveDate, final Integer...ids) {
        final List<org.locationtech.jts.geom.Geometry> geometryCollection = new ArrayList<>();
        for (int id : ids) {
            final RegionGeometry region = getAreaLocationRegionEffectiveOn(id, effectiveDate);
            if (region != null) {
                final org.locationtech.jts.geom.Geometry geometry = region.getGeometry();
                if(geometry.isValid()) {
                    geometryCollection.add(geometry);
                } else {
                    // Try to make geometry valid by adding 0 buffer around it
                    geometryCollection.add(geometry.buffer(0));
                    log.warn("RegionGeometry is not valid id: {} locationCode: {} name: {} effectiveDate: {}", region.getId(), region.getLocationCode(), region.getName(), region.getEffectiveDate());
                }
            }
        }
        final org.locationtech.jts.geom.Geometry union = PostgisGeometryUtils.union(geometryCollection);
        return convertToGeojson(union);
    }

    public static List<RegionGeometryFeature> convertToDtoList(final Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode, final boolean includeGeometry) {
        return regionsInDescOrderMappedByLocationCode.values().stream()
            .flatMap(Collection::stream)
            .map((RegionGeometry geometry) -> convertToDto(geometry, includeGeometry))
            .collect(Collectors.toList());
    }

    private static RegionGeometryFeature convertToDto(final RegionGeometry geometry, final boolean includeGeometry) {
        return new RegionGeometryFeature(
            includeGeometry ? convertToGeojson(geometry.getGeometry()) : null,
            new RegionGeometryProperties(geometry.getName(), geometry.getLocationCode(), AreaType.valueOf(geometry.getType().name()), geometry.getEffectiveDate()));
    }

    private static Geometry<?> convertToGeojson(final org.locationtech.jts.geom.Geometry geometry) {
        final String geoJson = geoJsonWriter.write(geometry);
        try {
            return geometryReader.readValue(geoJson);
        } catch (final JsonProcessingException e) {
            log.error(MessageFormat.format("method=convertToGeojson Failed to convert {0} to GeoJSON", geoJson), e);
            throw new RuntimeException(e);
        }
    }

    private void removeNeverValidValues(final Map<Integer, List<RegionGeometry>> locationCodeToRegion) {
        locationCodeToRegion.forEach((key, value) -> {
            final Iterator<RegionGeometry> iter = value.iterator();
            RegionGeometry latest = iter.next();
            // Remove next elemets that are effective from the same date or later than latest value, when done
            // do same for the next effective element that is earlier than latest
            while (iter.hasNext()) {
                final RegionGeometry next = iter.next();
                if (next.getEffectiveDate().getEpochSecond() >= latest.getEffectiveDate().getEpochSecond()) {
                    iter.remove();
                } else {
                    latest = next;
                }
            }
        });
    }

    private List<RegionGeometryFeature> filterRegionsAndConvertToDto(final Instant effectiveDate, final boolean includeGeometry, final Integer... ids) {

        if (ids != null && ids.length > 0) {
            // Both params given
            if (effectiveDate != null) {
                return filterByEffectiveDateAndLocationCodesAndConvertToDto(effectiveDate, includeGeometry, ids);
            }
            // Only ids params given
            return filterByIdsAndConvertToDto(includeGeometry, ids);
            // Only effectiveDate param given
        } else if (effectiveDate != null) {
            return filterByDateAndConvertToDto(effectiveDate, includeGeometry);
        }
        // No params -> return all;
        if (includeGeometry) {
            return regionStatus.allRegionsDtosInDescOrder;
        } else {
            return regionStatus.allRegionsDtosInDescOrderWithoutGeometry;
        }
    }

    private List<RegionGeometryFeature> filterByDateAndConvertToDto(final Instant effectiveDate, final boolean includeGeometry) {
        return regionStatus.regionsInDescOrderMappedByLocationCode.keySet().stream()
            .map(k -> getAreaLocationRegionEffectiveOn(k, effectiveDate))
            .filter(Objects::nonNull)
            .map((RegionGeometry geometry) -> convertToDto(geometry, includeGeometry))
            .collect(Collectors.toList());
    }

    private List<RegionGeometryFeature> filterByIdsAndConvertToDto(final boolean includeGeometry, final Integer...ids) {
        final Map<Integer, List<RegionGeometry>> regionsMappedByLocationCode = new HashMap<>();
        for (int id : ids) {
            final List<RegionGeometry> regionVersions = regionStatus.getRegionVersionsInDescOrder(id);
            if (regionVersions != null) {
                regionsMappedByLocationCode.put(id, regionVersions);
            }
        }
        return convertToDtoList(regionsMappedByLocationCode, includeGeometry);
    }

    private List<RegionGeometryFeature> filterByEffectiveDateAndLocationCodesAndConvertToDto(final Instant effectiveDate,
                                                                                             final boolean includeGeometry,
                                                                                             final Integer... locationCodes) {
        final List<RegionGeometryFeature> regions = new ArrayList<>();
        for (int locationCode : locationCodes) {
            final RegionGeometry region = getAreaLocationRegionEffectiveOn(locationCode, effectiveDate);
            if (region != null) {
                regions.add(convertToDto(region, includeGeometry));
            }
        }
        return regions;
    }

    private static class RegionStatus {

        private final Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode;
        public final List<RegionGeometryFeature> allRegionsDtosInDescOrder;
        private final List<RegionGeometryFeature> allRegionsDtosInDescOrderWithoutGeometry;
        public final String currentCommitId;
        public final Instant updated;
        public final ZonedDateTime checked;

        public RegionStatus() {
            regionsInDescOrderMappedByLocationCode = new HashMap<>();
            allRegionsDtosInDescOrder = new ArrayList<>();
            allRegionsDtosInDescOrderWithoutGeometry = new ArrayList<>();
            currentCommitId = null;
            updated = null;
            checked = null;
        }

        public RegionStatus(
            final Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode,
            final String currentCommitId, final Instant updated,
            final ZonedDateTime checked) {
            this.regionsInDescOrderMappedByLocationCode = regionsInDescOrderMappedByLocationCode;
            this.currentCommitId = currentCommitId;
            this.updated = updated;
            this.checked = checked;
            this.allRegionsDtosInDescOrder = convertToDtoList(regionsInDescOrderMappedByLocationCode, true);
            this.allRegionsDtosInDescOrderWithoutGeometry = convertToDtoList(regionsInDescOrderMappedByLocationCode, false);
        }

        public List<RegionGeometry> getRegionVersionsInDescOrder(final int locationCode) {
            return regionsInDescOrderMappedByLocationCode != null ?
                   regionsInDescOrderMappedByLocationCode.get(locationCode) : null;
        }
    }
}
