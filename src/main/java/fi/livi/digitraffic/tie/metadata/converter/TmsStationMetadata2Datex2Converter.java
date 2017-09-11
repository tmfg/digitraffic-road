package fi.livi.digitraffic.tie.metadata.converter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.CountryEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Exchange;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.HeaderInformation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.InformationStatusEnum;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.InternationalIdentifier;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteTable;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualString;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Point;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointByCoordinates;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PointCoordinates;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

@Component
public class TmsStationMetadata2Datex2Converter {

    public static final String MEASUREMENT_SITE_TABLE_VERSION = "1";
    public static final String MEASUREMENT_SITE_RECORD_VERSION = "1";

    private final InformationStatusEnum informationStatus;

    public TmsStationMetadata2Datex2Converter(@Value("${spring.profiles.active}") final String profile) {
        this.informationStatus = profile.equals("koka-prod") ? InformationStatusEnum.REAL : InformationStatusEnum.TEST;
    }

    public D2LogicalModel convert(final List<TmsStation> stations) {

        final MeasurementSiteTablePublication measurementSiteTablePublication = new MeasurementSiteTablePublication()
            .withHeaderInformation(new HeaderInformation()
                                       .withConfidentiality(ConfidentialityValueEnum.NO_RESTRICTION)
                                       .withInformationStatus(informationStatus));

        final D2LogicalModel model = new D2LogicalModel()
            .withExchange(new Exchange().withSupplierIdentification(new InternationalIdentifier().withCountry(CountryEnum.FI).withNationalIdentifier("FI")))
            .withPayloadPublication(measurementSiteTablePublication);

        long measurementSiteTableId = 0;
        for (final TmsStation station : stations) {
            final MeasurementSiteTable stationTable =
                new MeasurementSiteTable()
                    .withId(Long.toString(measurementSiteTableId))
                    .withMeasurementSiteTableIdentification(Long.toString(station.getNaturalId()))
                    .withVersion(MEASUREMENT_SITE_TABLE_VERSION);

            final List<RoadStationSensor> sensors =
                station.getRoadStation().getRoadStationSensors().stream().sorted(RoadStationSensor::compareTo).collect(Collectors.toList());

            for (final RoadStationSensor sensor : sensors) {
                stationTable.getMeasurementSiteRecord().add(getMeasurementSiteRecord(station, sensor));
            }
            measurementSiteTablePublication.getMeasurementSiteTable().add(stationTable);
            measurementSiteTableId++;
        }

        return model;
    }

    private static MeasurementSiteRecord getMeasurementSiteRecord(final TmsStation station, final RoadStationSensor sensor) {

        final fi.livi.digitraffic.tie.metadata.geojson.Point point =
            AbstractMetadataToFeatureConverter.getETRS89CoordinatesPoint(station.getRoadStation());

        return new MeasurementSiteRecord()
            .withId(Long.toString(sensor.getId()))
            .withMeasurementSiteIdentification(Long.toString(sensor.getNaturalId()))
            .withVersion(MEASUREMENT_SITE_RECORD_VERSION)
            .withMeasurementSiteName(getName(sensor))
            .withMeasurementSiteLocation(
                new Point().withPointByCoordinates(
                    new PointByCoordinates().withPointCoordinates(new PointCoordinates()
                                                                      .withLongitude(point != null && point.getLongitude() != null ? point.getLongitude().floatValue() : 0)
                                                                      .withLatitude(point != null && point.getLatitude() != null ? point.getLatitude().floatValue() : 0))));
    }

    private static MultilingualString getName(final RoadStationSensor sensor) {
        return new MultilingualString().withValues(new MultilingualString.Values()
                                                       .withValue(new MultilingualStringValue()
                                                                      .withLang("fi")
                                                                      .withValue(sensor.getNameFi())));
    }
}
