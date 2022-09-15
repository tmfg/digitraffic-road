package fi.livi.digitraffic.tie.converter.tms.v1;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.StationSensorConverterService;
import fi.livi.digitraffic.tie.converter.roadstation.v1.AbstractRoadstationToFeatureConverterV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureDetailedV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationFeatureSimpleV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationPropertiesDetailedV1;
import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.TmsStation;

@Component
public class TmsStationToFeatureConverterV1 extends AbstractRoadstationToFeatureConverterV1 {

    private static final Logger log = LoggerFactory.getLogger(TmsStationToFeatureConverterV1.class);

    private final StationSensorConverterService stationSensorConverterService;

    @Autowired
    private TmsStationToFeatureConverterV1(final CoordinateConverter coordinateConverter, final StationSensorConverterService stationSensorConverterService) {
        super(coordinateConverter);
        this.stationSensorConverterService = stationSensorConverterService;
    }

    public TmsStationFeatureCollectionSimpleV1 convertToSimpleFeatureCollection(final List<TmsStation> stations,
                                                                                final Instant lastUpdated) {

        final List<TmsStationFeatureSimpleV1> features =
            stations.stream()
                .map(this::convertToSimpleFeature)
                .collect(Collectors.toList());

        return new TmsStationFeatureCollectionSimpleV1(lastUpdated, features);
    }

    private TmsStationFeatureSimpleV1 convertToSimpleFeature(final TmsStation station) {
        if (log.isDebugEnabled()) {
            log.debug("method=convertToSimpleFeature " + station);
        }

        final TmsStationPropertiesSimpleV1 properties =
            new TmsStationPropertiesSimpleV1(station.getRoadStationNaturalId(), station.getNaturalId());

        // RoadStation properties
        final RoadStation rs = station.getRoadStation();
        setRoadStationPropertiesSimple(properties, rs, station.getModified());

        return new TmsStationFeatureSimpleV1(getGeometry(rs), properties);
    }

    public TmsStationFeatureDetailedV1 convertToDetailedFeature(final TmsStation station,
                                                                final Double freeFlowSpeed1,
                                                                final Double freeFlowSpeed2) {

        if (log.isDebugEnabled()) {
            log.debug("method=convertToDetailedFeature " + station);
        }

        final List<Long> sensorsNatualIds =
            stationSensorConverterService.getPublishableSensorsNaturalIdsByRoadStationId(station.getRoadStationId(), RoadStationType.TMS_STATION);

        final TmsStationPropertiesDetailedV1 properties =
            new TmsStationPropertiesDetailedV1(station.getRoadStationNaturalId(), station.getNaturalId(),
                station.getDirection1Municipality(), station.getDirection1MunicipalityCode(),
                station.getDirection2Municipality(), station.getDirection2MunicipalityCode(),
                station.getTmsStationType(), station.getCalculatorDeviceType(),
                sensorsNatualIds, freeFlowSpeed1, freeFlowSpeed2);

        // RoadStation properties
        final RoadStation rs = station.getRoadStation();
        setRoadStationPropertiesDetailed(properties, rs, station.getModified());

        return new TmsStationFeatureDetailedV1(getGeometry(rs), properties);
    }
}
