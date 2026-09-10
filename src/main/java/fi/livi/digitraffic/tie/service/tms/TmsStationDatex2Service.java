package fi.livi.digitraffic.tie.service.tms;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.RoadStationState;
import fi.livi.digitraffic.tie.converter.tms.datex2.json.TmsStationMetadata2Datex2JsonConverter;
import fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStationMetadataToDatexII35XmlConverter;
import fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStationMetadataToDatexII37XmlConverter;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.service.tms.v1.TmsStationMetadataWebServiceV1;

@ConditionalOnWebApplication
@Service
public class TmsStationDatex2Service {

    private final TmsStationMetadataToDatexII35XmlConverter tmsStationMetadataToDatexII35XmlConverter;
    private final TmsStationMetadataToDatexII37XmlConverter tmsStationMetadataToDatexII37XmlConverter;
    private final TmsStationMetadata2Datex2JsonConverter tmsStationMetadata2Datex2JsonConverter;
    private final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1;

    public TmsStationDatex2Service(final TmsStationMetadataWebServiceV1 tmsStationMetadataWebServiceV1,
                                   final TmsStationMetadataToDatexII35XmlConverter tmsStationMetadataToDatexII35XmlConverter,
                                   final TmsStationMetadataToDatexII37XmlConverter tmsStationMetadataToDatexII37XmlConverter,
                                   final TmsStationMetadata2Datex2JsonConverter tmsStationMetadata2Datex2JsonConverter) {
        this.tmsStationMetadataWebServiceV1 = tmsStationMetadataWebServiceV1;
        this.tmsStationMetadataToDatexII35XmlConverter = tmsStationMetadataToDatexII35XmlConverter;
        this.tmsStationMetadataToDatexII37XmlConverter = tmsStationMetadataToDatexII37XmlConverter;
        this.tmsStationMetadata2Datex2JsonConverter = tmsStationMetadata2Datex2JsonConverter;
    }

    @Transactional(readOnly = true)
    public MeasurementSiteTablePublication findAllPublishableTmsStationsAsDatexII35Xml(final RoadStationState roadStationState) {
        final List<TmsStation> stations = tmsStationMetadataWebServiceV1.findPublishableStations(roadStationState);
        return tmsStationMetadataToDatexII35XmlConverter.convertToXml(stations, tmsStationMetadataWebServiceV1.getMetadataLastUpdated());
    }

    @Transactional(readOnly = true)
    public MeasurementSiteTablePublication getPublishableTmsStationAsDatexII35Xml(final long id) {
        final TmsStation station = tmsStationMetadataWebServiceV1.getPublishableStationById(id);
        return tmsStationMetadataToDatexII35XmlConverter.convertToXml(Collections.singletonList(station), station.getMaxModified());
    }

    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementSiteTablePublication findAllPublishableTmsStationsAsDatexII37Xml(final RoadStationState roadStationState) {
        final List<TmsStation> stations = tmsStationMetadataWebServiceV1.findPublishableStations(roadStationState);
        return tmsStationMetadataToDatexII37XmlConverter.convertToXml(stations, tmsStationMetadataWebServiceV1.getMetadataLastUpdated());
    }

    @Transactional(readOnly = true)
    public fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementSiteTablePublication getPublishableTmsStationAsDatexII37Xml(final long id) {
        final TmsStation station = tmsStationMetadataWebServiceV1.getPublishableStationById(id);
        return tmsStationMetadataToDatexII37XmlConverter.convertToXml(Collections.singletonList(station), station.getMaxModified());
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
