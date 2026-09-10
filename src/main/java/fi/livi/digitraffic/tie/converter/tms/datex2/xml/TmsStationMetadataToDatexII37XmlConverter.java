package fi.livi.digitraffic.tie.converter.tms.datex2.xml;

import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.filterSortAndFillInMissingSensors;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.resolvePeriodSecondsFromSensorName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.livi.digitraffic.tie.service.tms.TmsStationSensorConstantService;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.ComputationMethodEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.HeaderInformation;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.InformationStatusEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.InternationalIdentifier;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasuredOrDerivedDataTypeEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementSite;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementSiteTable;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementSpecificCharacteristics;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MultilingualString;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MultilingualStringValue;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.PointByCoordinates;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.PointCoordinates;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.PointLocation;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.VehicleCharacteristics;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.VehicleTypeEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._ComputationMethodEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._InformationStatusEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._MeasuredOrDerivedDataTypeEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._MeasurementSiteIndexMeasurementSpecificCharacteristics;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._VehicleTypeEnum;

@ConditionalOnWebApplication
@Component
public class TmsStationMetadataToDatexII37XmlConverter {
    private final TmsStationSensorConstantService tmsStationSensorConstantService;

    private final InformationStatusEnum informationStatus;

    public TmsStationMetadataToDatexII37XmlConverter(final TmsStationSensorConstantService tmsStationSensorConstantService,
                                                     @Value("${dt.domain.url}") final String appUrl) {
        this.tmsStationSensorConstantService = tmsStationSensorConstantService;
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

    private MeasurementSite getMeasurementSiteRecord(final TmsStation station, final List<RoadStationSensor> sensors) {
        final fi.livi.digitraffic.tie.metadata.geojson.Point point = TmsDatex2Common.resolveETRS89PointLocation(station.getRoadStation());

        final var bearing = tmsStationSensorConstantService.getCachedBearing(station.getRoadStationId());

        final String measurementEquipmentType =
                station.getCalculatorDeviceType() != null ? station.getCalculatorDeviceType().getValue() : null;
        final MeasurementSite measurementSite =
                new MeasurementSite()
                        .withId(station.getRoadStationNaturalId().toString())
                        .withMeasurementSiteRecordVersionTime(station.getMaxModified())
                        .withVersion(station.getMaxModified().toString()) // not required
                        .withMeasurementSiteIdentification(station.getRoadStation().getName())
                        .withMeasurementSiteName(getMeasurementSiteName(station))
                        .withMeasurementSiteLocation(
                                new PointLocation()
                                        .withPointByCoordinates(
                                                new PointByCoordinates()
                                                        .withBearing(bearing)
                                                        .withPointCoordinates(new PointCoordinates()
                                                                .withLongitude(point != null && point.getLongitude() != null ? point.getLongitude().floatValue() : 0)
                                                                .withLatitude(point != null && point.getLatitude() != null ? point.getLatitude().floatValue() : 0))))
                        .withMeasurementSpecificCharacteristics(
                                getMeasurementSpecificCharacteristics(filterSortAndFillInMissingSensors(sensors)));
                        //.withMeasurementSiteNumberOfLanes(); unknown
        if (measurementEquipmentType != null) {
            measurementSite.withMeasurementEquipmentTypeUsed(getMultilingualString(measurementEquipmentType));
        }

        return measurementSite;
    }

    private static List<_MeasurementSiteIndexMeasurementSpecificCharacteristics> getMeasurementSpecificCharacteristics(final List<RoadStationSensor> sensors) {

        final List<MeasurementSpecificCharacteristics> measurementSpecificCharacteristics =
                sensors.stream().map(TmsStationMetadataToDatexII37XmlConverter::createMeasurementSpecificCharacteristics).toList();

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
        final _MeasuredOrDerivedDataTypeEnum dataType = resolveMeasuredOrDerivedDataType(sensor);

        return new MeasurementSpecificCharacteristics()
                // accuracy is % value.
                .withAccuracy((float) TmsDatex2Common.getSensorValueAccuracyPercentage())
                .withComputationMethod(new _ComputationMethodEnum( computationMethod, null))
                .withSpecificMeasurementValueType(dataType)
                .withPeriod(periodSeconds != null ? Float.valueOf(periodSeconds) : null)
                .withSpecificVehicleCharacteristics(new VehicleCharacteristics()
                        .withVehicleTypes(Collections.singletonList(new _VehicleTypeEnum().withValue(VehicleTypeEnum.ANY_VEHICLE))));
    }

    private static _MeasuredOrDerivedDataTypeEnum resolveMeasuredOrDerivedDataType(final RoadStationSensor sensor) {
        if (sensor.isFlowSensor()) {
            return new _MeasuredOrDerivedDataTypeEnum(MeasuredOrDerivedDataTypeEnum.TRAFFIC_FLOW, null);
        }
        if (sensor.isSpeedSensor()) {
            return new _MeasuredOrDerivedDataTypeEnum(MeasuredOrDerivedDataTypeEnum.TRAFFIC_SPEED, null);
        }

        // Keep schema-required enum populated for sensors outside the predefined DATEX categories.
        return new _MeasuredOrDerivedDataTypeEnum(MeasuredOrDerivedDataTypeEnum.__EXTENDED, sensor.getNameFi());
    }

    private static InternationalIdentifier getInternationalIdentifier() {
        return new InternationalIdentifier()
                .withCountry("FI")
                .withNationalIdentifier(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER);
    }

    private static MultilingualString getMeasurementSiteName(final TmsStation station) {
        final String fi = ObjectUtils.firstNonNull(station.getRoadStation().getName(), station.getRoadStation().getNameFi());
        final String sv = station.getRoadStation().getNameSv();
        final String en = station.getRoadStation().getNameEn();
        final MultilingualString.Values values = new MultilingualString.Values();

        fiIfNotNull(fi, () -> values.getValues().add(new MultilingualStringValue(fi, "fi")));
        fiIfNotNull(sv, () -> values.getValues().add(new MultilingualStringValue(sv, "sv")));
        fiIfNotNull(en, () -> values.getValues().add(new MultilingualStringValue(en, "en")));

        return new MultilingualString().withValues(values);
    }

    private static HeaderInformation getHeaderInformation(final InformationStatusEnum informationStatus) {
        return new HeaderInformation()
                .withConfidentiality(new _ConfidentialityValueEnum().withValue(ConfidentialityValueEnum.NO_RESTRICTION))
                .withInformationStatus(new _InformationStatusEnum().withValue(informationStatus));
    }

    private static MultilingualString getMultilingualString(final String value) {
        return new MultilingualString(new MultilingualString.Values(
                Collections.singletonList(new MultilingualStringValue(value, "fi"))));
    }

    private static void fiIfNotNull(final String value, final Runnable action) {
        if (value != null) {
            action.run();
        }
    }
}
