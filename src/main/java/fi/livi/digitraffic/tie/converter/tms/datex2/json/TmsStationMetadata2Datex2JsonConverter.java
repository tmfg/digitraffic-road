package fi.livi.digitraffic.tie.converter.tms.datex2.json;

import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common.filterSortAndFillInMissingSensors;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common.resolvePeriodSecondsFromSensorName;
import static fi.livi.digitraffic.tie.converter.tms.datex2.json.TmsStation2Datex2ConverterJsonCommon.getHeaderInformation;
import static fi.livi.digitraffic.tie.converter.tms.datex2.json.TmsStation2Datex2ConverterJsonCommon.getInternationalIdentifier;
import static fi.livi.digitraffic.tie.converter.tms.datex2.json.TmsStation2Datex2ConverterJsonCommon.getMeasurementSiteName;
import static fi.livi.digitraffic.tie.converter.tms.datex2.json.TmsStation2Datex2ConverterJsonCommon.getMultilingualString;
import static fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG.InformationStatusEnum.REAL;
import static fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG.InformationStatusEnum.TEST;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.ComputationMethodEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.LocationReferenceG;
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
                .withIdG(TmsDatex2Common.MEASUREMENT_SITE_TABLE_IDENTIFIER)
                .withVersionG(String.valueOf(metadataLastUpdated))
                .withMeasurementSiteTableIdentification(TmsDatex2Common.MEASUREMENT_SITE_TABLE_IDENTIFIER)
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
        final fi.livi.digitraffic.tie.metadata.geojson.Point point = TmsDatex2Common.resolveETRS89PointLocation(station.getRoadStation());

        final String measurementEquipmentType =
                station.getCalculatorDeviceType() != null ? station.getCalculatorDeviceType().getValue() : null;
        final MeasurementSite measurementSite =
                new MeasurementSite()
                        .withIdG(station.getRoadStationNaturalId().toString())
                        .withMeasurementSiteRecordVersionTime(station.getMaxModified())
                        .withVersionG(station.getMaxModified().toString()) // not required
                        .withMeasurementSiteIdentification(station.getRoadStation().getName())
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

        final ComputationMethodEnumG.ComputationMethodEnum computationMethod =
                sensor.isMovingMeasurement() ? ComputationMethodEnumG.ComputationMethodEnum.MOVING_AVERAGE_OF_SAMPLES :
                ComputationMethodEnumG.ComputationMethodEnum.ARITHMETIC_AVERAGE_OF_SAMPLES_IN_A_TIME_PERIOD;

        final Integer periodSeconds = resolvePeriodSecondsFromSensorName(sensor.getNameFi());

        return new MeasurementSpecificCharacteristics()
                // accuracy is % value
                .withAccuracy(TmsDatex2Common.getSensorValueAccuracyPercentage())
                .withComputationMethod(new ComputationMethodEnumG( computationMethod, null))
                // Not recommended to use like this
                //.withSpecificMeasurementValueType(new MeasuredOrDerivedDataTypeEnumG(dataType, sensor.getNameFi()))
                .withPeriod(periodSeconds != null ? Double.valueOf(periodSeconds) : null)
                .withSpecificVehicleCharacteristics(new VehicleCharacteristics()
                        .withVehicleType(Collections.singletonList(new VehicleTypeEnumG().withValue(
                                VehicleTypeEnumG.VehicleTypeEnum.ANY_VEHICLE))));
    }
}
