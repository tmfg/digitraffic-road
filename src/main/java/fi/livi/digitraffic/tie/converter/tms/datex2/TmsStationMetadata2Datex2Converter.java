package fi.livi.digitraffic.tie.converter.tms.datex2;

import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.MEASUREMENT_SITE_TABLE_IDENTIFIER;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.filterSortAndFillInMissingSensors;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.getHeaderInformation;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.getInternationalIdentifier;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.getMeasurementSiteName;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.getMultilingualString;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.resolveETRS89PointLocation;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.resolvePeriodSecondsFromSensorName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.external.datex2.v3_5.ComputationMethodEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.InformationStatusEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasuredOrDerivedDataTypeEnum;
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
import fi.livi.digitraffic.tie.external.datex2.v3_5._MeasuredOrDerivedDataTypeEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5._MeasurementSiteIndexMeasurementSpecificCharacteristics;
import fi.livi.digitraffic.tie.external.datex2.v3_5._VehicleTypeEnum;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.tms.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationMetadata2Datex2Converter {
    private static final Logger log = LoggerFactory.getLogger(TmsStationMetadata2Datex2Converter.class);

    private final InformationStatusEnum informationStatus;

    public TmsStationMetadata2Datex2Converter(@Value("${dt.domain.url}") final String appUrl) {
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
                .withId(MEASUREMENT_SITE_TABLE_IDENTIFIER)
                .withVersion(String.valueOf(metadataLastUpdated))
                .withMeasurementSiteTableIdentification(MEASUREMENT_SITE_TABLE_IDENTIFIER);

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
        final fi.livi.digitraffic.tie.metadata.geojson.Point point = resolveETRS89PointLocation(station.getRoadStation());

        final String measurementEquipmentType =
                station.getCalculatorDeviceType() != null ? station.getCalculatorDeviceType().getValue() : null;
        final MeasurementSite measurementSite =
                new MeasurementSite()
                        .withId(station.getRoadStationNaturalId().toString())
                        .withMeasurementSiteRecordVersionTime(station.getMaxModified())
                        .withVersion(station.getMaxModified().toString()) // not required
                        .withMeasurementSiteIdentification(String.valueOf(station.getNaturalId()))
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
                sensors.stream().map(TmsStationMetadata2Datex2Converter::createMeasurementSpecificCharacteristics).toList();

        final List<_MeasurementSiteIndexMeasurementSpecificCharacteristics> indexedMeasurementSpecificCharacteristics = new ArrayList<>();
        for (int i = 0; i < measurementSpecificCharacteristics.size(); i++) {
            indexedMeasurementSpecificCharacteristics.add(
                    new _MeasurementSiteIndexMeasurementSpecificCharacteristics(measurementSpecificCharacteristics.get(i), i+1));
        }
        return indexedMeasurementSpecificCharacteristics;
    }

    private static MeasurementSpecificCharacteristics createMeasurementSpecificCharacteristics(final RoadStationSensor sensor) {

        final MeasuredOrDerivedDataTypeEnum dataType =
                sensor.isSpeedSensor() ?
                    MeasuredOrDerivedDataTypeEnum.TRAFFIC_SPEED :
                    sensor.isFlowSensor() ? MeasuredOrDerivedDataTypeEnum.TRAFFIC_FLOW : null;

        final ComputationMethodEnum computationMethod =
                sensor.isMovingMeasurement() ? ComputationMethodEnum.MOVING_AVERAGE_OF_SAMPLES :
                ComputationMethodEnum.ARITHMETIC_AVERAGE_OF_SAMPLES_IN_A_TIME_PERIOD;

        final Integer periodSeconds = resolvePeriodSecondsFromSensorName(sensor.getNameFi());

        return new MeasurementSpecificCharacteristics()
                .withAccuracy(sensor.getAccuracy() != null ? sensor.getAccuracy().floatValue() : 0)
                .withComputationMethod(new _ComputationMethodEnum( computationMethod, null))
                .withSpecificMeasurementValueType(new _MeasuredOrDerivedDataTypeEnum(dataType, sensor.getNameFi()))
                .withPeriod(periodSeconds != null ? Float.valueOf(periodSeconds) : null)
                .withSpecificVehicleCharacteristics(new VehicleCharacteristics()
                        .withVehicleTypes(Collections.singletonList(new _VehicleTypeEnum().withValue(VehicleTypeEnum.ANY_VEHICLE))));
    }
}
