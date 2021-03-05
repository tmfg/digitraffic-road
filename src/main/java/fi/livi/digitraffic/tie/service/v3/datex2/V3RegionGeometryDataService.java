package fi.livi.digitraffic.tie.service.v3.datex2;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

import fi.livi.digitraffic.tie.dao.v3.RegionGeometryRepository;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region.RegionGeometriesDtoV3;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.region.RegionGeometryDtoV3;
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

    private RegionGeometryRepository regionGeometryRepository;
    private final DataStatusService dataStatusService;

    private Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode = new HashMap<>();
    private List<RegionGeometryDtoV3> regionsInDescOrder = new ArrayList<>();
    private String currentCommitId = null;
    private ZonedDateTime updated;
    private ZonedDateTime checked;

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
    @Scheduled(fixedRate = 3600000, initialDelayString = "${dt.scheduled.job.initialDelay.ms}")
    @Transactional
    public void refreshCache() {
        final StopWatch start = StopWatch.createStarted();
        final String latestCommitId = getLatestCommitId();
        if (StringUtils.equals(currentCommitId, latestCommitId)) {
            log.info("method=refreshCache No changes currentCommitId {} and latestCommitId {} are the same", currentCommitId, latestCommitId);
            return;
        }
        final List<RegionGeometry> regions = regionGeometryRepository.findAllByOrderByIdAsc();
        final Map<Integer, List<RegionGeometry>> locationCodeToRegion = new TreeMap<>();

        regions.forEach(a -> {
            // Add only versions that are valid (type is missing for 1. versions)
            if (a.isValid()) {
                final Integer locationCode = a.getLocationCode();
                if ( !locationCodeToRegion.containsKey(locationCode)) {
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
        this.regionsInDescOrderMappedByLocationCode = locationCodeToRegion;
        this.regionsInDescOrder = convertToDtoList(regionsInDescOrderMappedByLocationCode);
        currentCommitId = latestCommitId;
        updated = dataStatusService.findDataUpdatedTime(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA);
        checked = dataStatusService.findDataUpdatedTime(DataType.TRAFFIC_MESSAGES_REGION_GEOMETRY_DATA_CHECK);

        log.info("method=refreshAreaLocationRegionCache done tookMs={}", start.getTime());
    }

    @Transactional
    public String getLatestCommitId() {
        return regionGeometryRepository.getLatestCommitId();
    }

    public RegionGeometriesDtoV3 findAreaLocationRegions(final Instant effectiveDate, final Integer...ids) {
        final List<RegionGeometryDtoV3> filtered = filterRegions(effectiveDate, ids);
        return new RegionGeometriesDtoV3(filtered, updated, checked);
    }

    public RegionGeometry getAreaLocationRegionEffectiveOn(final int locationCode, final Instant theMoment) {
        final List<RegionGeometry> regions = regionsInDescOrderMappedByLocationCode.get(locationCode);
        if (regions == null) {
            log.warn("method=getAreaLocationRegionEffectiveOn No location with locationCode {} found", locationCode);
            return null;
        } else if (regions.size() == 1) {
            return regions.get(0);
        }
        // Find latest version that is valid on given moment or the first version
        return regions.stream()
            .filter(r -> r.getEffectiveDate().getEpochSecond() <= theMoment.getEpochSecond())
            .findFirst()
            .orElse(regions.get(0));
    }

    public Geometry<?> getGeoJsonGeometryUnion(final Instant effectiveDate, final Integer...ids) {
        final List<org.locationtech.jts.geom.Geometry> geometryCollection = new ArrayList<>();
        for (int id : ids) {
            final RegionGeometry a = getAreaLocationRegionEffectiveOn(id, effectiveDate);
            geometryCollection.add(a.getGeometry());
        }
        final org.locationtech.jts.geom.Geometry union = PostgisGeometryHelper.union(geometryCollection);
        return convertToGeojson(union);
    }

    private List<RegionGeometryDtoV3> convertToDtoList(final Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode) {
        return regionsInDescOrderMappedByLocationCode.values().stream()
            .flatMap(Collection::stream)
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private RegionGeometryDtoV3 convertToDto(final RegionGeometry geometry) {
        final Geometry<?> geoJsonGeometry = convertToGeojson(geometry.getGeometry());
        return new RegionGeometryDtoV3(geometry.getName(), geometry.getLocationCode(), geometry.getType(),
                                       geometry.getEffectiveDate(), geoJsonGeometry);
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

    private List<RegionGeometryDtoV3> filterRegions(final Instant effectiveDate, final Integer...ids) {

        if (ids != null && ids.length > 0) {
            // Both params given
            if (effectiveDate != null) {
                return filterByDateAndIds(effectiveDate, ids);
                // Only ids params given
            }
            return filterByIds(ids);
        // Only effectiveDate param given
        } else if (effectiveDate != null) {
            return filterByDate(effectiveDate);
        }
        // No params -> return all;
        return regionsInDescOrder;
    }

    private List<RegionGeometryDtoV3> filterByDate(final Instant effectiveDate) {
        return regionsInDescOrderMappedByLocationCode.keySet().stream()
            .map(k -> getAreaLocationRegionEffectiveOn(k, effectiveDate))
            .filter(Objects::nonNull)
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private List<RegionGeometryDtoV3> filterByIds(final Integer...ids) {
        final Map<Integer, List<RegionGeometry>> regionsMappedByLocationCode = new HashMap<>();
        for (int id : ids) {
            final List<RegionGeometry> regionVersions = regionsInDescOrderMappedByLocationCode.get(id);
            if (regionVersions != null) {
                regionsMappedByLocationCode.put(id, regionVersions);
            }
        }
        return convertToDtoList(regionsMappedByLocationCode);
    }

    private List<RegionGeometryDtoV3> filterByDateAndIds(final Instant effectiveDate, final Integer...ids) {
        final List<RegionGeometryDtoV3> regions = new ArrayList<>();
        for (int id : ids) {
            final RegionGeometry region = getAreaLocationRegionEffectiveOn(id, effectiveDate);
            if (region != null) {
                regions.add(convertToDto(region));
            }
        }
        return regions;
    }
}
