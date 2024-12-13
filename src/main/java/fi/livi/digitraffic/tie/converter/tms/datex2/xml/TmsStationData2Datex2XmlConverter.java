package fi.livi.digitraffic.tie.converter.tms.datex2.xml;

import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.filterAllowedSensorValuesAndMapWithNaturalId;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.filterSortAndFillInMissingSensors;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.getHeaderInformation;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.getInternationalIdentifier;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.getMultiLangualString;
import static fi.livi.digitraffic.tie.converter.tms.datex2.xml.TmsStation2Datex2XmlConverterCommon.resolvePeriodSecondsFromSensorName;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.external.datex2.v3_5.BasicData;
import fi.livi.digitraffic.tie.external.datex2.v3_5.InformationStatusEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasuredDataPublication;
import fi.livi.digitraffic.tie.external.datex2.v3_5.MeasurementOrCalculationTime;
import fi.livi.digitraffic.tie.external.datex2.v3_5.Period;
import fi.livi.digitraffic.tie.external.datex2.v3_5.PhysicalQuantity;
import fi.livi.digitraffic.tie.external.datex2.v3_5.PhysicalQuantityFault;
import fi.livi.digitraffic.tie.external.datex2.v3_5.PhysicalQuantityFaultEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.SinglePhysicalQuantity;
import fi.livi.digitraffic.tie.external.datex2.v3_5.SiteMeasurements;
import fi.livi.digitraffic.tie.external.datex2.v3_5.SpeedValue;
import fi.livi.digitraffic.tie.external.datex2.v3_5.TimeMeaningEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.TrafficData;
import fi.livi.digitraffic.tie.external.datex2.v3_5.TrafficFlow;
import fi.livi.digitraffic.tie.external.datex2.v3_5.TrafficSpeed;
import fi.livi.digitraffic.tie.external.datex2.v3_5.VehicleFlowValue;
import fi.livi.digitraffic.tie.external.datex2.v3_5._MeasurementSiteTableVersionedReference;
import fi.livi.digitraffic.tie.external.datex2.v3_5._MeasurementSiteVersionedReference;
import fi.livi.digitraffic.tie.external.datex2.v3_5._PhysicalQuantityFaultEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5._SiteMeasurementsIndexPhysicalQuantity;
import fi.livi.digitraffic.tie.external.datex2.v3_5._TimeMeaningEnum;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.tms.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationData2Datex2XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(TmsStationData2Datex2XmlConverter.class);

    private final fi.livi.digitraffic.tie.external.datex2.v3_5.InformationStatusEnum informationStatus;

    public TmsStationData2Datex2XmlConverter(@Value("${dt.domain.url}") final String camUrl) {
        this.informationStatus = camUrl.toLowerCase().contains("test") ? fi.livi.digitraffic.tie.external.datex2.v3_5.InformationStatusEnum.TEST : InformationStatusEnum.REAL;
    }

    public fi.livi.digitraffic.tie.external.datex2.v3_5.MeasuredDataPublication convertToXml(final Map<TmsStation, List<SensorValueDtoV1>> stations, final Instant updated) {

        final fi.livi.digitraffic.tie.external.datex2.v3_5.MeasuredDataPublication publication =
                new MeasuredDataPublication()
                        .withPublicationTime(updated)
                        .withPublicationCreator(getInternationalIdentifier())
                        .withLang("fi")
                        .withHeaderInformation(getHeaderInformation(informationStatus))
                        .withMeasurementSiteTableReferences(
                                new _MeasurementSiteTableVersionedReference()
                                        .withId(TmsDatex2Common.MEASUREMENT_SITE_TABLE_IDENTIFIER)
                                // Optional, no need to ref specific version of TMS station table
                                //.withVersion(metadata update time))
                        );

        publication.withSiteMeasurements(
                stations.entrySet().stream()
                        .sorted(Comparator.comparing(o -> o.getKey().getRoadStationNaturalId()))
                        .map((station) -> getSiteMeasurements(station.getKey(), station.getValue(), updated)).toList());

        return publication;
    }

    private static fi.livi.digitraffic.tie.external.datex2.v3_5.SiteMeasurements getSiteMeasurements(
            final TmsStation station,
            final List<SensorValueDtoV1> sensorValues,
            final Instant updated) {

        final fi.livi.digitraffic.tie.external.datex2.v3_5.SiteMeasurements measurementSite =
                new SiteMeasurements()
                        .withMeasurementSiteReference(new _MeasurementSiteVersionedReference()
                                        .withId(station.getRoadStationNaturalId().toString())
                                        .withVersion(String.valueOf(station.getMaxModified()))
                        );

        final List<RoadStationSensor> requiredSensors =
                filterSortAndFillInMissingSensors(station.getRoadStation().getRoadStationSensors());

        final List<_SiteMeasurementsIndexPhysicalQuantity>
                indexedSiteMeasurementsPhysicalQuantities =
                getSiteMeasurementsPhysicalQuantities(requiredSensors, sensorValues, updated);

        measurementSite.withPhysicalQuantities(indexedSiteMeasurementsPhysicalQuantities);

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

    private static List<_SiteMeasurementsIndexPhysicalQuantity> getSiteMeasurementsPhysicalQuantities(
            final List<RoadStationSensor> requiredSensors,
            final List<SensorValueDtoV1> values,
            final Instant updated) {
        final Map<Long, SensorValueDtoV1> valuesMap = filterAllowedSensorValuesAndMapWithNaturalId(values);

        final List<_SiteMeasurementsIndexPhysicalQuantity> measurements = new ArrayList<>();
        for (int i = 0; i < requiredSensors.size(); i++) {
            final RoadStationSensor sensor = requiredSensors.get(i);
            final SensorValueDtoV1 value = valuesMap.get(sensor.getNaturalId());
            final PhysicalQuantity quantity = getSinglePhysicalQuantity(sensor, value, updated);
            measurements.add(new _SiteMeasurementsIndexPhysicalQuantity(quantity, i+1));
        }
        return measurements;
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

    private static fi.livi.digitraffic.tie.external.datex2.v3_5.TrafficData getBasicData(final RoadStationSensor sensor,
                                                                                         final SensorValueDtoV1 sensorValue) {


        if (sensor.isFlowSensor() || sensor.isSpeedSensor()) {
            final TrafficData trafficData;
            if (sensor.isFlowSensor()) {
                final fi.livi.digitraffic.tie.external.datex2.v3_5.TrafficFlow trafficFlow = new TrafficFlow();
                trafficData = trafficFlow;
                if (sensorValue != null) {
                    final BigInteger value = BigDecimal.valueOf(sensorValue.getValue()).round(MathContext.UNLIMITED).toBigInteger();
                    trafficFlow.withVehicleFlow(new VehicleFlowValue().withVehicleFlowRate(value));
                }
            } else  { // == sensor.isSpeedSensor()
                final fi.livi.digitraffic.tie.external.datex2.v3_5.TrafficSpeed trafficSpeed = new TrafficSpeed();
                trafficData = trafficSpeed;
                if (sensorValue != null) {
                    trafficSpeed.withAverageVehicleSpeed(new SpeedValue().withSpeed((float) sensorValue.getValue()));
                }
            }
            withMeasurementOrCalculationTime(trafficData, sensorValue);
            return trafficData;
        } else {
            log.error("method=getBasicData called with sensor that is not flow or speed sensor {}", sensor);
            return null;
        }
    }

    private static <T extends BasicData> void withMeasurementOrCalculationTime(final T basicData,
                                                                               final SensorValueDtoV1 sensorValue) {

        if (sensorValue != null) {
            final MeasurementOrCalculationTime time = new MeasurementOrCalculationTime()
                    .withTimeValue(sensorValue.getMeasuredTime())
                    .withTimeMeaning(new _TimeMeaningEnum().withValue(TimeMeaningEnum.END_TIME));

            time.withPeriod(new Period()
                    .withPeriodName(getMultiLangualString(Pair.of("fi", sensorValue.getSensorNameFi())))
            );
            //  Fixed period
            if (sensorValue.getTimeWindowStart() != null && sensorValue.getTimeWindowEnd() != null) {
                time.getPeriod()
                        .withStartOfPeriod(sensorValue.getTimeWindowStart())
                        .withEndOfPeriod(sensorValue.getTimeWindowEnd());
            } else { // running period
                try {
                    final Instant end = sensorValue.getMeasuredTime();
                    final Integer periodTimeS = resolvePeriodSecondsFromSensorName(sensorValue.getSensorNameFi());
                    if (periodTimeS != null) {
                        final Instant start = end.minus(periodTimeS, ChronoUnit.SECONDS);
                        time.getPeriod().withStartOfPeriod(start);
                    }
                    time.getPeriod().withEndOfPeriod(end);
                } catch (final Exception e) {
                    log.error("method=withMeasurementOrCalculationTime Failed to calculate time period for sensor {}",
                            sensorValue.getSensorNameFi(), e);
                }
            }
            basicData.withMeasurementOrCalculationTime(time);
        }
    }
}
