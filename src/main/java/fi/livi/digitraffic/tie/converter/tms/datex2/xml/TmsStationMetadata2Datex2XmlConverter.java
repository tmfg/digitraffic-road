package fi.livi.digitraffic.tie.converter.tms.datex2.xml;

import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.filterSortAndFillInMissingSensors;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.getHeaderInformation;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.getInternationalIdentifier;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.getMeasurementSiteName;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.getMultilingualString;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.resolvePeriodSecondsFromSensorName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.external.datex2.v3_5.ComputationMethodEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.InformationStatusEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasurementSite;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasurementSiteTable;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasurementSpecificCharacteristics;
import fi.livi.digitraffic.tie.external.datex2.v3_5.PointByCoordinates;
import fi.livi.digitraffic.tie.external.datex2.v3_5.PointCoordinates;
import fi.livi.digitraffic.tie.external.datex2.v3_5.PointLocation;
import fi.livi.digitraffic.tie.external.datex2.v3_5.VehicleCharacteristics;
import fi.livi.digitraffic.tie.external.datex2.v3_5.VehicleTypeEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5._ComputationMethodEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5._MeasurementSiteIndexMeasurementSpecificCharacteristics;
import fi.livi.digitraffic.tie.external.datex2.v3_5._VehicleTypeEnum;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.tms.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationMetadata2Datex2XmlConverter {

    private final InformationStatusEnum informationStatus;

    public TmsStationMetadata2Datex2XmlConverter(@Value("${dt.domain.url}") final String appUrl) {
        this.informationStatus = appUrl.toLowerCase().contains("test") ? InformationStatusEnum.TEST : InformationStatusEnum.REAL;
    }

    public MeasurementSiteTablePublication convertToXml(final List<TmsStation> stations, final Instant metadataLastUpdated) {

        final MeasurementSiteTablePublication measurementSiteTablePublication =
            new MeasurementSiteTablePublication()
                .withPublicationTime(metadataLastUpdated)
                .withPublicationCreator(getInternationalIdentifier())
                .withLang("fi")
                .withHeaderInformation(getHeaderInformation(informationStatus));

        // https://docs.datex2.eu/levels/mastering/roadtrafficdata/
        final MeasurementSiteTable siteTable =
            new MeasurementSiteTable()
                .withId(TmsDatex2Common.MEASUREMENT_SITE_TABLE_IDENTIFIER)
                .withVersion(String.valueOf(metadataLastUpdated))
                .withMeasurementSiteTableIdentification(TmsDatex2Common.MEASUREMENT_SITE_TABLE_IDENTIFIER);

        stations.forEach(station ->
                siteTable.getMeasurementSites().add(
                        getMeasurementSiteRecord(
                                station,
                                station.getRoadStation().getRoadStationSensors().stream()
                                        .sorted(Comparator.comparingLong(RoadStationSensor::getNaturalId))
                                        .toList()
                        )
                )
        );




        measurementSiteTablePublication.getMeasurementSiteTables().add(siteTable);

        return measurementSiteTablePublication;
    }

    private static MeasurementSite getMeasurementSiteRecord(final TmsStation station, final List<RoadStationSensor> sensors) {
        final fi.livi.digitraffic.tie.metadata.geojson.Point point = TmsDatex2Common.resolveETRS89PointLocation(station.getRoadStation());

        final String measurementEquipmentType =
                station.getCalculatorDeviceType() != null ? station.getCalculatorDeviceType().getValue() : null;
        final MeasurementSite measurementSite =
                new MeasurementSite()
                        .withId(station.getRoadStationNaturalId().toString())
                        .withMeasurementSiteRecordVersionTime(station.getMaxModified())
                        .withVersion(station.getMaxModified().toString()) // not required
                        .withMeasurementSiteIdentification(String.valueOf(station.getRoadStationNaturalId()))
                        .withMeasurementSiteName(getMeasurementSiteName(station))
                        .withMeasurementSiteLocation(
                                new PointLocation()
                                        .withPointByCoordinates(
                                                new PointByCoordinates().withPointCoordinates(
                                                        new PointCoordinates()
                                                                .withLongitude(point != null && point.getLongitude() != null ? point.getLongitude().floatValue() : 0)
                                                                .withLatitude(point != null && point.getLatitude() != null ? point.getLatitude().floatValue() : 0))))
                        .withMeasurementSpecificCharacteristics(
                                getMeasurementSpecificCharacteristics(filterSortAndFillInMissingSensors(sensors)));
                        //.withMeasurementSiteNumberOfLanes(); unknown
        if (measurementEquipmentType != null) {
            measurementSite.withMeasurementEquipmentTypeUsed(getMultilingualString("fi", measurementEquipmentType));
        }

        return measurementSite;
    }

    private static List<_MeasurementSiteIndexMeasurementSpecificCharacteristics> getMeasurementSpecificCharacteristics(final List<RoadStationSensor> sensors) {

        final List<MeasurementSpecificCharacteristics> measurementSpecificCharacteristics =
                sensors.stream().map(TmsStationMetadata2Datex2XmlConverter::createMeasurementSpecificCharacteristics).toList();

        final List<_MeasurementSiteIndexMeasurementSpecificCharacteristics> indexedMeasurementSpecificCharacteristics = new ArrayList<>();
        for (int i = 0; i < measurementSpecificCharacteristics.size(); i++) {
            indexedMeasurementSpecificCharacteristics.add(
                    new _MeasurementSiteIndexMeasurementSpecificCharacteristics(measurementSpecificCharacteristics.get(i), i+1));
        }
        return indexedMeasurementSpecificCharacteristics;
    }

    private static MeasurementSpecificCharacteristics createMeasurementSpecificCharacteristics(final RoadStationSensor sensor) {

        final ComputationMethodEnum computationMethod =
                sensor.isMovingMeasurement() ? ComputationMethodEnum.MOVING_AVERAGE_OF_SAMPLES :
                ComputationMethodEnum.ARITHMETIC_AVERAGE_OF_SAMPLES_IN_A_TIME_PERIOD;

        final Integer periodSeconds = resolvePeriodSecondsFromSensorName(sensor.getNameFi());

        return new MeasurementSpecificCharacteristics()
                // accuracy is % value.
                .withAccuracy((float) TmsDatex2Common.getSensorValueAccuracyPercentage())
                .withComputationMethod(new _ComputationMethodEnum( computationMethod, null))
                // Not recommended to use like this
                //.withSpecificMeasurementValueType(new _MeasuredOrDerivedDataTypeEnum(dataType, sensor.getNameFi()))
                .withPeriod(periodSeconds != null ? Float.valueOf(periodSeconds) : null)
                .withSpecificVehicleCharacteristics(new VehicleCharacteristics()
                        .withVehicleTypes(Collections.singletonList(new _VehicleTypeEnum().withValue(VehicleTypeEnum.ANY_VEHICLE))));
    }
}
