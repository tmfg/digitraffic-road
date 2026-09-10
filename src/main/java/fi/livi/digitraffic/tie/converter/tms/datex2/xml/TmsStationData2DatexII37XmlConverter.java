package fi.livi.digitraffic.tie.converter.tms.datex2.xml;

import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.filterAllowedSensorValuesAndMapWithNaturalId;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.filterSortAndFillInMissingSensors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.HeaderInformation;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.InformationStatusEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.InternationalIdentifier;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasuredDataPublication;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.MeasurementOrCalculationTime;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.PhysicalQuantity;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.PhysicalQuantityFault;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.PhysicalQuantityFaultEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.SinglePhysicalQuantity;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.SiteMeasurements;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.SpeedValue;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.TimeMeaningEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.TrafficData;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.TrafficFlow;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.TrafficSpeed;
import fi.livi.digitraffic.tie.tms.datex2.v3_7.VehicleFlowValue;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._ConfidentialityValueEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._InformationStatusEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._MeasurementSiteTableVersionedReference;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._MeasurementSiteVersionedReference;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._PhysicalQuantityFaultEnum;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._SiteMeasurementsIndexPhysicalQuantity;
import fi.livi.digitraffic.tie.tms.datex2.v3_7._TimeMeaningEnum;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.tms.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationData2DatexII37XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(TmsStationData2DatexII37XmlConverter.class);

    private final InformationStatusEnum informationStatus;

    public TmsStationData2DatexII37XmlConverter(@Value("${dt.domain.url}") final String camUrl) {
        this.informationStatus = camUrl.toLowerCase().contains("test") ? InformationStatusEnum.TEST : InformationStatusEnum.REAL;
    }

    public MeasuredDataPublication convertToXml(final Map<TmsStation, List<SensorValueDtoV1>> stations, final Instant updated) {

        final MeasuredDataPublication publication =
                new MeasuredDataPublication()
                        .withPublicationTime(updated)
                        .withPublicationCreator(getInternationalIdentifier())
                        .withLang("fi")
                        .withHeaderInformation(getHeaderInformation(informationStatus))
                        .withMeasurementSiteTableReferences(
                                new _MeasurementSiteTableVersionedReference()
                                        .withId(TmsDatex2Common.MEASUREMENT_SITE_TABLE_IDENTIFIER)
                        );

        publication.withSiteMeasurements(
                stations.entrySet().stream()
                        .sorted(Comparator.comparing(o -> o.getKey().getRoadStationNaturalId()))
                        .map((station) -> getSiteMeasurements(station.getKey(), station.getValue(), updated)).toList());

        return publication;
    }

    private static SiteMeasurements getSiteMeasurements(
            final TmsStation station,
            final List<SensorValueDtoV1> sensorValues,
            final Instant updated) {

        final SiteMeasurements measurementSite =
                new SiteMeasurements()
                        .withMeasurementSiteReference(new _MeasurementSiteVersionedReference()
                                .withId(station.getRoadStationNaturalId().toString())
                                .withVersion(String.valueOf(station.getMaxModified()))
                        );

        // Filter allowed sensor values and group by natural id
        final Map<Long, SensorValueDtoV1> sensorsValuesByNaturalId =
                filterAllowedSensorValuesAndMapWithNaturalId(sensorValues);

        // Get sensor metadata for this station
        final List<RoadStationSensor> requiredSensors =
                filterSortAndFillInMissingSensors(station.getRoadStation().getRoadStationSensors());

        // Create measurement values
        final List<_SiteMeasurementsIndexPhysicalQuantity> measurementValues = new ArrayList<>();

        for (int i = 0; i < requiredSensors.size(); i++) {
            final RoadStationSensor sensor = requiredSensors.get(i);
            final SensorValueDtoV1 value = sensorsValuesByNaturalId.get(sensor.getNaturalId());
            final PhysicalQuantity quantity = getSinglePhysicalQuantity(sensor, value, updated);
            measurementValues.add(new _SiteMeasurementsIndexPhysicalQuantity(quantity, i + 1));
        }

        measurementSite.withPhysicalQuantities(measurementValues);

        final Instant minMeasuredTime = measurementSite.getPhysicalQuantities().stream()
                .map(a -> {
                    final MeasurementOrCalculationTime time = ((SinglePhysicalQuantity)
                            a.getPhysicalQuantity()).getBasicData().getMeasurementOrCalculationTime();
                    return time != null ? time.getTimeValue() : null;
                }).filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);

        measurementSite.withMeasurementTimeDefault(new MeasurementOrCalculationTime()
                .withTimeValue(minMeasuredTime)
                .withTimeMeaning(new _TimeMeaningEnum().withValue(TimeMeaningEnum.END_TIME)));

        return measurementSite;
    }

    private static PhysicalQuantity getSinglePhysicalQuantity(final RoadStationSensor sensor,
                                                              final SensorValueDtoV1 sensorValue,
                                                              final Instant updated) {
        final SinglePhysicalQuantity quantity =
                new SinglePhysicalQuantity()
                        .withBasicData(getBasicData(sensor, sensorValue));
        if (quantity.getBasicData() == null || sensorValue == null) {
            quantity.withPhysicalQuantityFaults(
                    new PhysicalQuantityFault()
                            .withPhysicalQuantityFaultType(
                                new _PhysicalQuantityFaultEnum()
                                        .withValue(
                                                PhysicalQuantityFaultEnum.NO_DATA_VALUES_AVAILABLE))
                            .withFaultLastUpdateTime(updated));
        }
        return quantity;
    }

    private static TrafficData getBasicData(final RoadStationSensor sensor,
                                            final SensorValueDtoV1 sensorValue) {

        if (sensor.isFlowSensor() || sensor.isSpeedSensor()) {
            final TrafficData trafficData;
            if (sensor.isFlowSensor()) {
                final TrafficFlow trafficFlow = new TrafficFlow();
                trafficData = trafficFlow;
                if (sensorValue != null) {
                    final BigInteger value = BigDecimal.valueOf(sensorValue.getValue()).round(MathContext.UNLIMITED).toBigInteger();
                    trafficFlow.withVehicleFlow(new VehicleFlowValue().withVehicleFlowRate(value));
                }
            } else { // == sensor.isSpeedSensor()
                final TrafficSpeed trafficSpeed = new TrafficSpeed();
                trafficData = trafficSpeed;
                if (sensorValue != null) {
                    trafficSpeed.withAverageVehicleSpeed(new SpeedValue().withSpeed((float) sensorValue.getValue()));
                }
            }
            withMeasurementOrCalculationTime(trafficData, sensorValue);
            return trafficData;
        }
        return null;
    }

    private static void withMeasurementOrCalculationTime(final TrafficData trafficData,
                                                        final SensorValueDtoV1 sensorValue) {
        if (sensorValue != null) {
            trafficData.withMeasurementOrCalculationTime(new MeasurementOrCalculationTime()
                    .withTimeValue(sensorValue.getMeasuredTime())
                    .withTimeMeaning(new _TimeMeaningEnum().withValue(TimeMeaningEnum.END_TIME)));
        }
    }

    private static InternationalIdentifier getInternationalIdentifier() {
        return new InternationalIdentifier()
                .withCountry("FI")
                .withNationalIdentifier(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER);
    }

    private static HeaderInformation getHeaderInformation(final InformationStatusEnum informationStatus) {
        return new HeaderInformation()
                .withConfidentiality(new _ConfidentialityValueEnum().withValue(ConfidentialityValueEnum.NO_RESTRICTION))
                .withInformationStatus(new _InformationStatusEnum().withValue(informationStatus));
    }
}




