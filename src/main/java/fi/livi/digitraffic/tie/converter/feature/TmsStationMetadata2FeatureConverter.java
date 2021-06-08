package fi.livi.digitraffic.tie.converter.feature;

import static fi.livi.digitraffic.tie.dao.v1.RoadStationSensorRepository.TMS_STATION_TYPE;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.StationSensorConverter;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsFreeFlowSpeedDto;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationProperties;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.TmsStation;

@Component
public final class TmsStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {
    private static final Logger log = LoggerFactory.getLogger( TmsStationMetadata2FeatureConverter.class);

    private final StationSensorConverter stationSensorConverter;

    @Autowired
    private TmsStationMetadata2FeatureConverter(final CoordinateConverter coordinateConverter, final StationSensorConverter stationSensorConverter) {
        super(coordinateConverter);
        this.stationSensorConverter = stationSensorConverter;
    }

    private enum FreeFlowSpeed {
        FREE_FLOW_SPEED_1,
        FREE_FLOW_SPEED_2
    }

    public TmsStationFeatureCollection convert(final List<TmsStation> stations,
                                               final List<TmsFreeFlowSpeedDto> freeFlowSpeeds,
                                               final ZonedDateTime lastUpdated,
                                               final ZonedDateTime dataLastCheckedTime) {

        final Map<Long, List<Long>> sensorMap = stationSensorConverter.createPublishableSensorMap(TMS_STATION_TYPE);
        final Map<Long, Pair<Double, Double>> rsNaturalIdToFreeFlosSpeedsMap =
            freeFlowSpeeds.stream()
                .collect(Collectors.toMap(TmsFreeFlowSpeedDto::getRoadStationNaturalId,
                                          ffs -> Pair.of(ffs.getFreeFlowSpeed1OrNull(),
                                                         ffs.getFreeFlowSpeed2OrNull())));

        final List<TmsStationFeature> features =
            stations.stream()
                .filter(tms -> tms.getRoadStation().isPublicNow())
                .map(tms -> convert(sensorMap, tms,
                                    getFreeFlowSpeed(FreeFlowSpeed.FREE_FLOW_SPEED_1, tms.getRoadStationNaturalId(), rsNaturalIdToFreeFlosSpeedsMap),
                                    getFreeFlowSpeed(FreeFlowSpeed.FREE_FLOW_SPEED_1, tms.getRoadStationNaturalId(), rsNaturalIdToFreeFlosSpeedsMap)))
                .collect(Collectors.toList());

        return new TmsStationFeatureCollection(lastUpdated, dataLastCheckedTime, features);
    }

    private Double getFreeFlowSpeed(final FreeFlowSpeed freeFlowSpeed, final long roadStationNaturalId, final Map<Long, Pair<Double, Double>> rsNaturalIdToFreeFlosSpeedsMap) {
        final Pair<Double, Double> fsSpeeds = rsNaturalIdToFreeFlosSpeedsMap.get(roadStationNaturalId);
        if (fsSpeeds != null) {
            if (FreeFlowSpeed.FREE_FLOW_SPEED_1.equals(freeFlowSpeed)) {
                return fsSpeeds.getLeft();
            } else if (FreeFlowSpeed.FREE_FLOW_SPEED_2.equals(freeFlowSpeed)) {
                return fsSpeeds.getRight();
            }
            throw new IllegalArgumentException("Unknown enum value " + freeFlowSpeed);
        }
        return null;
    }


    public TmsStationFeature convert(final TmsStation tms, final Double freeFlowSpeed1, final Double freeFlowSpeed2) {
        final Map<Long, List<Long>> sensorMap = stationSensorConverter.createPublishableSensorMap(tms.getRoadStationId(), TMS_STATION_TYPE);
        return convert(sensorMap, tms, freeFlowSpeed1, freeFlowSpeed2);
    }

    public TmsStationFeature convert(final Map<Long, List<Long>> sensorMap, final TmsStation tms, final Double freeFlowSpeed1, final Double freeFlowSpeed2) {
        final TmsStationFeature f;
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + tms);
        }

        final TmsStationProperties properties = new TmsStationProperties();

        // Tms station properties
        properties.setId(tms.getId());
        properties.setLotjuId(tms.getLotjuId());
        properties.setTmsNaturalId(tms.getNaturalId());
        properties.setDirection1Municipality(tms.getDirection1Municipality());
        properties.setDirection1MunicipalityCode(tms.getDirection1MunicipalityCode());
        properties.setDirection2Municipality(tms.getDirection2Municipality());
        properties.setDirection2MunicipalityCode(tms.getDirection2MunicipalityCode());
        properties.setTmsStationType(tms.getTmsStationType());
        properties.setCalculatorDeviceType(tms.getCalculatorDeviceType());
        properties.setName(tms.getName());

        properties.setFreeFlowSpeed1(freeFlowSpeed1);
        properties.setFreeFlowSpeed2(freeFlowSpeed2);

        if (tms.getRoadStation() != null) {
            final List<Long> sensorList = sensorMap.get(tms.getRoadStationId());
            properties.setStationSensors(ObjectUtils.firstNonNull(sensorList, Collections.emptyList()));
        }
        // RoadStation properties
        final RoadStation rs = tms.getRoadStation();
        if (rs == null) {
            log.error("Null roadStation: {}", tms);
        } else {
            setRoadStationProperties(properties, rs);
        }
        return new TmsStationFeature(getGeometry(rs), properties, tms.getRoadStationNaturalId());
    }
}
