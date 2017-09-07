package fi.livi.digitraffic.tie.metadata.converter;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.HeaderInformation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.InformationStatusEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteTable;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualString;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Point;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointByCoordinates;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointCoordinates;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

@Component
public class TmsStationMetadata2Datex2Converter {

    public static final String MEASUREMENT_SITE_TABLE_REFERENCE = "TMSMeasurementSiteTable";
    public static final String MEASUREMENT_SITE_TABLE_VERSION = "1";
    public static final String MEASUREMENT_SITE_VERSION = "1";

    private final InformationStatusEnum informationStatus;

    public TmsStationMetadata2Datex2Converter(@Value("${spring.profiles.active}") final String profile) {
        this.informationStatus = profile.equals("koka-prod") ? InformationStatusEnum.REAL : InformationStatusEnum.TEST;
    }

    public D2LogicalModel convert(final List<TmsStation> stations) {

        final MeasurementSiteTable dtMeasurementSiteTable =
            new MeasurementSiteTable().withId(MEASUREMENT_SITE_TABLE_REFERENCE).withVersion(MEASUREMENT_SITE_TABLE_VERSION);

        final D2LogicalModel model = new D2LogicalModel().withPayloadPublication(
            new MeasurementSiteTablePublication()
                .withHeaderInformation(new HeaderInformation()
                                           .withConfidentiality(ConfidentialityValueEnum.NO_RESTRICTION)
                                           .withInformationStatus(informationStatus))
                .withMeasurementSiteTable(dtMeasurementSiteTable));

        for (final TmsStation station : stations) {
            dtMeasurementSiteTable.getMeasurementSiteRecord().add(getMeasurementSiteRecord(station));
        }

        return model;
    }

    private static MeasurementSiteRecord getMeasurementSiteRecord(final TmsStation station) {

        final fi.livi.digitraffic.tie.metadata.geojson.Point point =
            AbstractMetadataToFeatureConverter.getETRS89CoordinatesPoint(station.getRoadStation());

        return new MeasurementSiteRecord()
            .withId(Long.toString(station.getNaturalId()))
            .withVersion(MEASUREMENT_SITE_VERSION)
            .withMeasurementSiteName(getName(station))
            .withMeasurementSiteLocation(
                new Point().withPointByCoordinates(
                    new PointByCoordinates().withPointCoordinates(new PointCoordinates()
                                                                      .withLongitude(point.getLongitude().floatValue())
                                                                      .withLatitude(point.getLatitude().floatValue()))));
    }

    private static MultilingualString getName(final TmsStation station) {
        return new MultilingualString().withValues(new MultilingualString.Values()
                                                       .withValue(new MultilingualStringValue()
                                                                      .withLang("fi")
                                                                      .withValue(station.getRoadStation().getNameFi()))
                                                       .withValue(new MultilingualStringValue()
                                                                      .withLang("sv")
                                                                      .withValue(station.getRoadStation().getNameSv()))
                                                       .withValue(new MultilingualStringValue()
                                                                      .withLang("en")
                                                                      .withValue(station.getRoadStation().getNameEn())));
    }
}
