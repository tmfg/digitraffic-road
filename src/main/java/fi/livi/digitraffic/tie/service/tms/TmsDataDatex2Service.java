package fi.livi.digitraffic.tie.service.tms;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon;
import fi.livi.digitraffic.tie.converter.tms.datex2.TmsStationData2Datex2Converter;
import fi.livi.digitraffic.tie.converter.tms.datex2.TmsStationData2Datex2JsonConverter;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasuredDataPublication;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

@ConditionalOnWebApplication
@Service
public class TmsDataDatex2Service {

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final RoadStationSensorService roadStationSensorService;
    private final TmsStationData2Datex2Converter tmsStationData2Datex2Converter;
    private final TmsStationData2Datex2JsonConverter tmsStationData2Datex2JsonConverter;

    public TmsDataDatex2Service(final TmsStationDatex2Service tmsStationDatex2Service,
                                final RoadStationSensorService roadStationSensorService,
                                final TmsStationData2Datex2Converter tmsStationData2Datex2Converter,
                                final TmsStationData2Datex2JsonConverter tmsStationData2Datex2JsonConverter) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.roadStationSensorService = roadStationSensorService;
        this.tmsStationData2Datex2Converter = tmsStationData2Datex2Converter;
        this.tmsStationData2Datex2JsonConverter = tmsStationData2Datex2JsonConverter;
    }

    @Transactional(readOnly = true)
    public MeasuredDataPublication findPublishableTmsDataDatex2() {
        final ZonedDateTime updated = roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION);

        final List<TmsStation> tmsStations = tmsStationDatex2Service.findAllPublishableTmsStations();

        final Map<Long, List<SensorValueDto>> values =
                roadStationSensorService.findAllPublishableRoadStationSensorValuesMappedByNaturalId(
                        RoadStationType.TMS_STATION, TmsStation2Datex2ConverterCommon.ALLOWED_SENSOR_NAMES);

        final Map<TmsStation, List<SensorValueDto>> stations =
                tmsStations.stream().collect(Collectors.toMap(s -> s, s -> values.getOrDefault(s.getRoadStationNaturalId(), Collections.emptyList())));

        return tmsStationData2Datex2Converter.convertToXml(stations, updated.toInstant());
    }

    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredDataPublication findPublishableTmsDataDatex2Json() {
        final ZonedDateTime updated = roadStationSensorService.getLatestSensorValueUpdatedTime(RoadStationType.TMS_STATION);

        final List<TmsStation> tmsStations = tmsStationDatex2Service.findAllPublishableTmsStations();

        final Map<Long, List<SensorValueDto>> values =
                roadStationSensorService.findAllPublishableRoadStationSensorValuesMappedByNaturalId(
                        RoadStationType.TMS_STATION, TmsStation2Datex2ConverterCommon.ALLOWED_SENSOR_NAMES);

        final Map<TmsStation, List<SensorValueDto>> stations =
                tmsStations.stream().collect(Collectors.toMap(s -> s, s -> values.getOrDefault(s.getRoadStationNaturalId(), Collections.emptyList())));

        return tmsStationData2Datex2JsonConverter.convertToJson(stations, updated.toInstant());
    }
}
