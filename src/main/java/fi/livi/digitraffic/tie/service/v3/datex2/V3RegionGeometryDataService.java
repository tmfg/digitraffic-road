package fi.livi.digitraffic.tie.service.v3.datex2;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region.RegionGeometryFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region.RegionGeometryProperties;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;
import fi.livi.digitraffic.tie.service.DataStatusService;

@ConditionalOnWebApplication
@Service
public class V3RegionGeometryDataService {
    private static final Logger log = LoggerFactory.getLogger(V3RegionGeometryDataService.class);
    private final GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
    private final ObjectReader geometryReader;

    private final RegionGeometryRepository regionGeometryRepository;
    private final DataStatusService dataStatusService;

    private RegionStatus regionStatus = new RegionStatus();


    @Autowired
    public V3RegionGeometryDataService(final RegionGeometryRepository regionGeometryRepository,
                                       final DataStatusService dataStatusService,
                                       final ObjectMapper objectMapper) {
        this.regionGeometryRepository = regionGeometryRepository;
        this.dataStatusService = dataStatusService;
        geometryReader = objectMapper.readerFor(Geometry.class);
        // Don't add crs to geometries as it's always EPSG:4326
        geoJsonWriter.setEncodeCRS(false);
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

            regions.forEach(a -> {
                // Add only versions that are valid (type is missing for 1. versions)
                if (a.isValid()) {
                    final Integer locationCode = a.getLocationCode();
                    if (!locationCodeToRegion.containsKey(locationCode)) {
                        locationCodeToRegion.put(locationCode, new ArrayList<>());
                    }
                    // Add latest as 1st element -> will be in desc order
                    locationCodeToRegion.get(locationCode).add(0, a);
                    log.info("method=refreshCache Added version {}", a.toString());
                } else {
                    log.info("method=refreshCache Not adding version with illegal type {}", a.toString());
                }
            });
            removeNeverValidValues(locationCodeToRegion);
            regionStatus = new RegionStatus(locationCodeToRegion,
                latestCommitId,
                dataStatusService.findDataUpdatedTime(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA),
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
    public RegionGeometryFeatureCollection findAreaLocationRegions(final boolean onlyUpdateInfo, final Instant effectiveDate, final Integer...ids) {
        return new RegionGeometryFeatureCollection(regionStatus.updated, regionStatus.checked,
            onlyUpdateInfo ? Collections.emptyList() : filterRegionsAndConvertToDto(effectiveDate, ids));
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
                geometryCollection.add(region.getGeometry());
            }
        }
        final org.locationtech.jts.geom.Geometry union = PostgisGeometryHelper.union(geometryCollection);
        return convertToGeojson(union);
    }

    private List<RegionGeometryFeature> convertToDtoList(final Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode) {
        return regionsInDescOrderMappedByLocationCode.values().stream()
            .flatMap(Collection::stream)
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private RegionGeometryFeature convertToDto(final RegionGeometry geometry) {
        return new RegionGeometryFeature(
            convertToGeojson(geometry.getGeometry()),
            new RegionGeometryProperties(geometry.getName(), geometry.getLocationCode(), geometry.getType(), geometry.getEffectiveDate()));
    }

    private Geometry<?> convertToGeojson(final org.locationtech.jts.geom.Geometry geometry) {
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

    private List<RegionGeometryFeature> filterRegionsAndConvertToDto(final Instant effectiveDate, final Integer...ids) {

        if (ids != null && ids.length > 0) {
            // Both params given
            if (effectiveDate != null) {
                return filterByEffectiveDateAndLocationCodesAndConvertToDto(effectiveDate, ids);
            }
            // Only ids params given
            return filterByIdsAndConvertToDto(ids);
            // Only effectiveDate param given
        } else if (effectiveDate != null) {
            return filterByDateAndConvertToDto(effectiveDate);
        }
        // No params -> return all;
        return regionStatus.allRegionsDtosInDescOrder;
    }

    private List<RegionGeometryFeature> filterByDateAndConvertToDto(final Instant effectiveDate) {
        return regionStatus.regionsInDescOrderMappedByLocationCode.keySet().stream()
            .map(k -> getAreaLocationRegionEffectiveOn(k, effectiveDate))
            .filter(Objects::nonNull)
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private List<RegionGeometryFeature> filterByIdsAndConvertToDto(final Integer...ids) {
        final Map<Integer, List<RegionGeometry>> regionsMappedByLocationCode = new HashMap<>();
        for (int id : ids) {
            final List<RegionGeometry> regionVersions = regionStatus.getRegionVersionsInDescOrder(id);
            if (regionVersions != null) {
                regionsMappedByLocationCode.put(id, regionVersions);
            }
        }
        return convertToDtoList(regionsMappedByLocationCode);
    }

    private List<RegionGeometryFeature> filterByEffectiveDateAndLocationCodesAndConvertToDto(final Instant effectiveDate, final Integer...locationCodes) {
        final List<RegionGeometryFeature> regions = new ArrayList<>();
        for (int locationCode : locationCodes) {
            final RegionGeometry region = getAreaLocationRegionEffectiveOn(locationCode, effectiveDate);
            if (region != null) {
                regions.add(convertToDto(region));
            }
        }
        return regions;
    }

    private class RegionStatus {

        private final Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode;
        public final List<RegionGeometryFeature> allRegionsDtosInDescOrder;
        public final String currentCommitId;
        public final ZonedDateTime updated;
        public final ZonedDateTime checked;

        public RegionStatus() {
            regionsInDescOrderMappedByLocationCode = new HashMap<>();
            allRegionsDtosInDescOrder = new ArrayList<>();
            currentCommitId = null;
            updated = null;
            checked = null;
        }

        public RegionStatus(
            final Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode,
            final String currentCommitId, final ZonedDateTime updated,
            final ZonedDateTime checked) {
            this.regionsInDescOrderMappedByLocationCode = regionsInDescOrderMappedByLocationCode;
            this.currentCommitId = currentCommitId;
            this.updated = updated;
            this.checked = checked;
            this.allRegionsDtosInDescOrder = convertToDtoList(regionsInDescOrderMappedByLocationCode);
        }

        public List<RegionGeometry> getRegionVersionsInDescOrder(final int locationCode) {
            return regionsInDescOrderMappedByLocationCode != null ?
                   regionsInDescOrderMappedByLocationCode.get(locationCode) : null;
        }
    }
}
