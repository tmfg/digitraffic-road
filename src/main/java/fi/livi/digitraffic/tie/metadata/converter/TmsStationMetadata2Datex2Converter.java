package fi.livi.digitraffic.tie.metadata.converter;

import java.util.List;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteTable;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualString;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Point;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointByCoordinates;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointCoordinates;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

public class TmsStationMetadata2Datex2Converter {

    public static D2LogicalModel convert(final List<TmsStation> stations) {

        final D2LogicalModel model = new D2LogicalModel();

        final MeasurementSiteTablePublication publication = new MeasurementSiteTablePublication();
        model.setPayloadPublication(publication);

        for (final TmsStation station : stations) {
            publication.getMeasurementSiteTable().add(getMeasurementSite(station));
        }

        return model;
    }

    private static MeasurementSiteTable getMeasurementSite(final TmsStation station) {

        final fi.livi.digitraffic.tie.metadata.geojson.Point point = CoordinateConverter.convertFromETRS89ToWGS84(
            AbstractMetadataToFeatureConverter.getETRS89CoordinatesPoint(station.getRoadStation()));

        return new MeasurementSiteTable().withMeasurementSiteRecord(
            new MeasurementSiteRecord()
                .withMeasurementSiteName(singleValueString(station.getName()))
                .withMeasurementSiteLocation(
                    new Point().withPointByCoordinates(
                        new PointByCoordinates().withPointCoordinates(new PointCoordinates()
                                                                          .withLongitude(point.getLongitude().floatValue())
                                                                          .withLatitude(point.getLatitude().floatValue())))));
    }

    private static MultilingualString singleValueString(final String str) {
        return new MultilingualString().withValues(new MultilingualString.Values().withValue(new MultilingualStringValue()
                                                                                                 .withLang("fi")
                                                                                                 .withValue(str)));
    }
}
