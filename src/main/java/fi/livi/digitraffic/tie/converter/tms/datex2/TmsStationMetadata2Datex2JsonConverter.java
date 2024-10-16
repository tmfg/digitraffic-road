package fi.livi.digitraffic.tie.converter.tms.datex2;

import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.MEASUREMENT_SITE_TABLE_IDENTIFIER;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.resolveETRS89PointLocation;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.filterSortAndFillInMissingSensors;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.getHeaderInformation;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.getInternationalIdentifier;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.getMeasurementSiteName;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.getMultilingualString;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.resolvePeriodSecondsFromSensorName;
import static fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG.InformationStatusEnum.REAL;
import static fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG.InformationStatusEnum.TEST;

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

import fi.livi.digitraffic.tie.external.datex2.v3_5.json.ComputationMethodEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.LocationReferenceG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredOrDerivedDataTypeEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSite;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteIndexMeasurementSpecificCharacteristicsG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTable;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSpecificCharacteristics;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.PointByCoordinates;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.PointCoordinates;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.PointLocation;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.VehicleCharacteristics;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.VehicleTypeEnumG;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.tms.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationMetadata2Datex2JsonConverter {
    private static final Logger log = LoggerFactory.getLogger(TmsStationMetadata2Datex2JsonConverter.class);

    private final InformationStatusEnumG.InformationStatusEnum informationStatus;

    public TmsStationMetadata2Datex2JsonConverter(@Value("${dt.domain.url}") final String appUrl) {
        this.informationStatus = appUrl.toLowerCase().contains("test") ? TEST : REAL;
    }

    public MeasurementSiteTablePublication convertToJson(final List<TmsStation> stations, final Instant metadataLastUpdated) {



        final MeasurementSiteTablePublication measurementSiteTablePublication =
            new MeasurementSiteTablePublication()
                .withPublicationTime(metadataLastUpdated)
                .withPublicationCreator(getInternationalIdentifier())
                .withLang("fi")
                .withHeaderInformation(getHeaderInformation(informationStatus));

        // https://docs.datex2.eu/levels/mastering/roadtrafficdata/
        final MeasurementSiteTable siteTable =
            new MeasurementSiteTable()
                .withIdG(MEASUREMENT_SITE_TABLE_IDENTIFIER)
                .withVersionG(String.valueOf(metadataLastUpdated))
                .withMeasurementSiteTableIdentification(MEASUREMENT_SITE_TABLE_IDENTIFIER)
                .withMeasurementSite(
                    stations.stream().map(station -> getMeasurementSiteRecord(
                            station,
                            station.getRoadStation().getRoadStationSensors().stream()
                                    .sorted(Comparator.comparingLong(RoadStationSensor::getNaturalId))
                                    .toList()
                    )).toList());
        measurementSiteTablePublication.withMeasurementSiteTable(Collections.singletonList(siteTable));

        return measurementSiteTablePublication;
    }



    private static MeasurementSite getMeasurementSiteRecord(final TmsStation station, final List<RoadStationSensor> sensors) {
        final fi.livi.digitraffic.tie.metadata.geojson.Point point = resolveETRS89PointLocation(station.getRoadStation());

        final String measurementEquipmentType =
                station.getCalculatorDeviceType() != null ? station.getCalculatorDeviceType().getValue() : null;
        final MeasurementSite measurementSite =
                new MeasurementSite()
                        .withIdG(station.getRoadStationNaturalId().toString())
                        .withMeasurementSiteRecordVersionTime(station.getMaxModified())
                        .withVersionG(station.getMaxModified().toString()) // not required
                        .withMeasurementSiteIdentification(String.valueOf(station.getNaturalId()))
                        .withMeasurementSiteName(getMeasurementSiteName(station))
                        .withMeasurementSiteLocation(
                                new LocationReferenceG().withLocPointLocation(
                                new PointLocation()
                                        .withPointByCoordinates(
                                                new PointByCoordinates().withPointCoordinates(
                                                        new PointCoordinates()
                                                                .withLongitude(point != null ? point.getLongitude() : 0)
                                                                .withLatitude(point != null ? point.getLatitude() : 0)))))
                        .withMeasurementSpecificCharacteristics(
                                getMeasurementSpecificCharacteristics(filterSortAndFillInMissingSensors(sensors)));
                        //.withMeasurementSiteNumberOfLanes(); unknown
        if (measurementEquipmentType != null) {
            measurementSite.withMeasurementEquipmentTypeUsed(getMultilingualString("fi", measurementEquipmentType));
        }

        return measurementSite;
    }

    private static List<MeasurementSiteIndexMeasurementSpecificCharacteristicsG> getMeasurementSpecificCharacteristics(final List<RoadStationSensor> sensors) {

        final List<MeasurementSpecificCharacteristics> measurementSpecificCharacteristics =
                sensors.stream().map(TmsStationMetadata2Datex2JsonConverter::createMeasurementSpecificCharacteristics).toList();

        final List<MeasurementSiteIndexMeasurementSpecificCharacteristicsG> indexedMeasurementSpecificCharacteristics = new ArrayList<>();
        for (int i = 0; i < measurementSpecificCharacteristics.size(); i++) {
            indexedMeasurementSpecificCharacteristics.add(
                    new MeasurementSiteIndexMeasurementSpecificCharacteristicsG(measurementSpecificCharacteristics.get(i), i+1));
        }
        return indexedMeasurementSpecificCharacteristics;
    }

    private static MeasurementSpecificCharacteristics createMeasurementSpecificCharacteristics(final RoadStationSensor sensor) {

        final MeasuredOrDerivedDataTypeEnumG.MeasuredOrDerivedDataTypeEnum dataType =
                sensor.isSpeedSensor() ?
                MeasuredOrDerivedDataTypeEnumG.MeasuredOrDerivedDataTypeEnum.TRAFFIC_SPEED :
                sensor.isFlowSensor() ? MeasuredOrDerivedDataTypeEnumG.MeasuredOrDerivedDataTypeEnum.TRAFFIC_FLOW : null;

        final ComputationMethodEnumG.ComputationMethodEnum computationMethod =
                sensor.isMovingMeasurement() ? ComputationMethodEnumG.ComputationMethodEnum.MOVING_AVERAGE_OF_SAMPLES :
                ComputationMethodEnumG.ComputationMethodEnum.ARITHMETIC_AVERAGE_OF_SAMPLES_IN_A_TIME_PERIOD;

        final Integer periodSeconds = resolvePeriodSecondsFromSensorName(sensor.getNameFi());

        return new MeasurementSpecificCharacteristics()
                .withAccuracy(sensor.getAccuracy() != null ? Double.valueOf(sensor.getAccuracy()) : null)
                .withComputationMethod(new ComputationMethodEnumG( computationMethod, null))
                .withSpecificMeasurementValueType(new MeasuredOrDerivedDataTypeEnumG(dataType, sensor.getNameFi()))
                .withPeriod(periodSeconds != null ? Double.valueOf(periodSeconds) : null)
                .withSpecificVehicleCharacteristics(new VehicleCharacteristics()
                        .withVehicleType(Collections.singletonList(new VehicleTypeEnumG().withValue(
                                VehicleTypeEnumG.VehicleTypeEnum.ANY_VEHICLE))));
    }
}
