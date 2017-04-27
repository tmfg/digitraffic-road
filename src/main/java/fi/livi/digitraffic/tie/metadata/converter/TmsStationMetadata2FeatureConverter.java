package fi.livi.digitraffic.tie.metadata.converter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationProperties;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

@Component
public final class TmsStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {
    private static final Log log = LogFactory.getLog( TmsStationMetadata2FeatureConverter.class );

    private final RoadStationSensorRepository roadStationSensorRepository;
    @Autowired
    private TmsStationMetadata2FeatureConverter(final CoordinateConverter coordinateConverter,
        final RoadStationSensorRepository roadStationSensorRepository) {
        super(coordinateConverter);
        this.roadStationSensorRepository = roadStationSensorRepository;
    }

    public TmsStationFeatureCollection convert(final List<TmsStation> stations, final ZonedDateTime lastUpdated) {
        final TmsStationFeatureCollection collection = new TmsStationFeatureCollection(lastUpdated);
        final Map<Long, List<Long>> sensorMap = createSensorMap();

        for(final TmsStation tms : stations) {
            try {
                collection.add(convert(sensorMap, tms));
            } catch (final NonPublicRoadStationException nprse) {
                //Skip non public roadstation
                log.warn("Skipping: " + nprse.getMessage());
            }
        }
        return collection;
    }

    private Map<Long, List<Long>> createSensorMap() {
        final List<Object[]> list = roadStationSensorRepository.listRoadStationSensors();
        final Map<Long, List<Long>> sensorMap = new HashMap<>();

        list.stream().forEach(oo -> {
            final Long rsId = ((BigDecimal)oo[0]).longValue();
            final String sensorList = (String)oo[1];

            sensorMap.put(rsId, sensorList(sensorList));
        });

        return sensorMap;
    }

    public TmsStationFeature convert(final TmsStation tms) throws NonPublicRoadStationException {
        final Map<Long, List<Long>> sensorMap = new HashMap<>();

        if(tms.getRoadStationId() != null) {
            final String sensorList = roadStationSensorRepository.listRoadStationSensors(tms.getRoadStationId());

            sensorMap.put(tms.getRoadStationId(), sensorList(sensorList));
        }

        return convert(sensorMap, tms);
    }

    private static List<Long> sensorList(final String sensorList) {
        return Stream.of(sensorList.split(",")).map(Long::valueOf).collect(Collectors.toList());
    }

    public TmsStationFeature convert(final Map<Long, List<Long>> sensorMap, final TmsStation tms) throws NonPublicRoadStationException {
        final TmsStationFeature f = new TmsStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + tms);
        }
        f.setId(tms.getRoadStationNaturalId());

        final TmsStationProperties properties = f.getProperties();

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

        if (tms.getRoadStation() != null) {
            final List<Long> sensorList = sensorMap.get(tms.getRoadStationId());
            properties.setStationSensors(ObjectUtils.firstNonNull(sensorList, Collections.emptyList()));
        }
        // RoadStation properties
        final RoadStation rs = tms.getRoadStation();
        setRoadStationProperties(properties, rs);

        setCoordinates(f, rs);

        return f;
    }
}
