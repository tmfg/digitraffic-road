package fi.livi.digitraffic.tie.service.tms;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.converter.tms.datex2.json.TmsStationMetadata2Datex2JsonConverter;
import fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStationMetadata2Datex2XmlConverter;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;

@ConditionalOnWebApplication
@Service
public class TmsStationDatex2Service {

    private final TmsStationMetadata2Datex2XmlConverter tmsStationMetadata2Datex2XmlConverter;
    private final TmsStationMetadata2Datex2JsonConverter tmsStationMetadata2Datex2JsonConverter;
    private final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1;

    public TmsStationDatex2Service(final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1,
                                   final TmsStationMetadata2Datex2XmlConverter tmsStationMetadata2Datex2XmlConverter,
                                   final TmsStationMetadata2Datex2JsonConverter tmsStationMetadata2Datex2JsonConverter) {
        this.tmsStationMetadataWebServiceV1 = tmsStationMetadataWebServiceV1;
        this.tmsStationMetadata2Datex2XmlConverter = tmsStationMetadata2Datex2XmlConverter;
        this.tmsStationMetadata2Datex2JsonConverter = tmsStationMetadata2Datex2JsonConverter;
    }

    @Transactional(readOnly = true)
    public MeasurementSiteTablePublication findAllPublishableTmsStationsAsDatex2Xml(final RoadStationState roadStationState) {
        final List<TmsStation> stations = tmsStationMetadataWebServiceV1.findPublishableStations(roadStationState);
        return tmsStationMetadata2Datex2XmlConverter.convertToXml(stations, tmsStationMetadataWebServiceV1.getMetadataLastUpdated());
    }

    @Transactional(readOnly = true)
    public MeasurementSiteTablePublication getPublishableTmsStationAsDatex2Xml(final long id) {
        final TmsStation station = tmsStationMetadataWebServiceV1.getPublishableStationById(id);
        return tmsStationMetadata2Datex2XmlConverter.convertToXml(Collections.singletonList(station), station.getMaxModified());
    }

    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.tms.datex2.v3_5.json.MeasurementSiteTablePublication findAllPublishableTmsStationsAsDatex2Json(final RoadStationState roadStationState) {
        final List<TmsStation> stations = tmsStationMetadataWebServiceV1.findPublishableStations(roadStationState);
        return tmsStationMetadata2Datex2JsonConverter.convertToJson(stations, tmsStationMetadataWebServiceV1.getMetadataLastUpdated());
    }

    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.tms.datex2.v3_5.json.MeasurementSiteTablePublication getPublishableTmsStationAsDatex2Json(final long id) {
        final TmsStation station = tmsStationMetadataWebServiceV1.getPublishableStationById(id);
        return tmsStationMetadata2Datex2JsonConverter.convertToJson(Collections.singletonList(station), station.getMaxModified());
    }
}
