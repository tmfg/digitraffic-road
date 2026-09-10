package fi.livi.digitraffic.tie.service.tms;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.converter.tms.datex2.json.TmsStationData2Datex2JsonConverter;
import fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStationData2DatexII37XmlConverter;
import fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStationData2DatexII35XmlConverter;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.MeasuredDataPublication;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.roadstation.v1.RoadStationSensorServiceV1;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;

@ConditionalOnWebApplication
@Service
public class TmsDataDatex2Service {
    private final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1;
    private final RoadStationSensorServiceV1 roadStationSensorServiceV1;
    private final TmsStationData2DatexII35XmlConverter tmsStationData2DatexII35XmlConverter;
    private final TmsStationData2Datex2JsonConverter tmsStationData2Datex2JsonConverter;
    private final TmsStationData2DatexII37XmlConverter tmsStationData2DatexII37XmlConverter;

    public TmsDataDatex2Service(final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1,
                                final RoadStationSensorServiceV1 roadStationSensorServiceV1,
                                final TmsStationData2DatexII35XmlConverter tmsStationData2DatexII35XmlConverter,
                                final TmsStationData2Datex2JsonConverter tmsStationData2Datex2JsonConverter,
                                final TmsStationData2DatexII37XmlConverter tmsStationData2DatexII37XmlConverter) {
        this.tmsStationMetadataWebServiceV1 = tmsStationMetadataWebServiceV1;
        this.roadStationSensorServiceV1 = roadStationSensorServiceV1;
        this.tmsStationData2DatexII35XmlConverter = tmsStationData2DatexII35XmlConverter;
        this.tmsStationData2Datex2JsonConverter = tmsStationData2Datex2JsonConverter;
        this.tmsStationData2DatexII37XmlConverter = tmsStationData2DatexII37XmlConverter;
    }

    @Transactional(readOnly = true)
    public MeasuredDataPublication findAllPublishableTmsStationsDataAsDatex2Xml() {
        final Instant updated = roadStationSensorServiceV1.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION);
        final List<TmsStation> tmsStations = tmsStationMetadataWebServiceV1.findPublishableStations(RoadStationState.ACTIVE);

        final Map<Long, List<SensorValueDtoV1>> values =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValuesMappedByNaturalId(
                        RoadStationType.TMS_STATION, TmsDatex2Common.ALLOWED_SENSOR_NAMES);

        final Map<TmsStation, List<SensorValueDtoV1>> stations =
                tmsStations.stream().collect(Collectors.toMap(s -> s, s -> values.getOrDefault(s.getRoadStationNaturalId(), Collections.emptyList())));

        return tmsStationData2DatexII35XmlConverter.convertToXml(stations, updated);
    }

    @Transactional(readOnly = true)
    public MeasuredDataPublication getPublishableTmsStationDataAsDatex2Xml(final long id) {
        final TmsStation tmsStation = tmsStationMetadataWebServiceV1.getPublishableStationById(id);
        final List<SensorValueDtoV1> sensorValues =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValues(id, RoadStationType.TMS_STATION);

        return tmsStationData2DatexII35XmlConverter.convertToXml(Collections.singletonMap(tmsStation, sensorValues), SensorValueDtoV1.getStationLatestUpdated(sensorValues));
    }


    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.tms.datex2.v3_5.json.MeasuredDataPublication findAllPublishableTmsStationsDataAsDatex2Json() {
        final Instant updated = roadStationSensorServiceV1.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION);
        final List<TmsStation> tmsStations = tmsStationMetadataWebServiceV1.findPublishableStations(RoadStationState.ACTIVE);

        final Map<Long, List<SensorValueDtoV1>> values =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValuesMappedByNaturalId(
                        RoadStationType.TMS_STATION, TmsDatex2Common.ALLOWED_SENSOR_NAMES);

        final Map<TmsStation, List<SensorValueDtoV1>> stations =
                tmsStations.stream().collect(Collectors.toMap(s -> s, s -> values.getOrDefault(s.getRoadStationNaturalId(), Collections.emptyList())));

        return tmsStationData2Datex2JsonConverter.convertToJson(stations, updated);
    }

    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.tms.datex2.v3_5.json.MeasuredDataPublication getPublishableTmsStationDataAsDatex2Json(final long id) {
        final TmsStation tmsStation = tmsStationMetadataWebServiceV1.getPublishableStationById(id);
        final List<SensorValueDtoV1> sensorValues =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValues(id, RoadStationType.TMS_STATION);

        return tmsStationData2Datex2JsonConverter.convertToJson(Collections.singletonMap(tmsStation, sensorValues), SensorValueDtoV1.getStationLatestUpdated(sensorValues));
    }

    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasuredDataPublication findAllPublishableTmsStationsDataAsDatexII37Xml() {
        final Instant updated = roadStationSensorServiceV1.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION);
        final List<TmsStation> tmsStations = tmsStationMetadataWebServiceV1.findPublishableStations(RoadStationState.ACTIVE);

        final Map<Long, List<SensorValueDtoV1>> values =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValuesMappedByNaturalId(
                        RoadStationType.TMS_STATION, TmsDatex2Common.ALLOWED_SENSOR_NAMES);

        final Map<TmsStation, List<SensorValueDtoV1>> stations =
                tmsStations.stream().collect(Collectors.toMap(s -> s, s -> values.getOrDefault(s.getRoadStationNaturalId(), Collections.emptyList())));

        return tmsStationData2DatexII37XmlConverter.convertToXml(stations, updated);
    }

    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasuredDataPublication getPublishableTmsStationDataAsDatexII37Xml(final long id) {
        final TmsStation tmsStation = tmsStationMetadataWebServiceV1.getPublishableStationById(id);
        final List<SensorValueDtoV1> sensorValues =
                roadStationSensorServiceV1.findAllPublishableRoadStationSensorValues(id, RoadStationType.TMS_STATION);

        return tmsStationData2DatexII37XmlConverter.convertToXml(Collections.singletonMap(tmsStation, sensorValues), SensorValueDtoV1.getStationLatestUpdated(sensorValues));
    }
}
