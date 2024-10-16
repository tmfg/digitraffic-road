package fi.livi.digitraffic.tie.converter.tms.datex2;

import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.filterAllowedSensorValuesAndMapWithNaturalId;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.filterSortAndFillInMissingSensors;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.getMultiLangualString;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterJsonCommon.resolvePeriodSecondsFromSensorName;
import static fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG.InformationStatusEnum.REAL;
import static fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG.InformationStatusEnum.TEST;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.common.util.ObjectUtil;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.BasicDataG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasuredDataPublication;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementOrCalculationTime;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteTableVersionedReferenceG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MeasurementSiteVersionedReferenceG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.Period;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.PhysicalQuantityFault;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.PhysicalQuantityFaultEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.PhysicalQuantityG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.SinglePhysicalQuantity;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.SiteMeasurements;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.SiteMeasurementsIndexPhysicalQuantityG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.SpeedValue;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.TimeMeaningEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.TrafficFlow;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.TrafficSpeed;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.VehicleFlowValue;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.tms.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStationData2Datex2JsonConverter {

    private static final Logger log = LoggerFactory.getLogger(TmsStationData2Datex2JsonConverter.class);

    private final InformationStatusEnumG.InformationStatusEnum informationStatus;

    public TmsStationData2Datex2JsonConverter(@Value("${dt.domain.url}") final String camUrl) {
        this.informationStatus = camUrl.toLowerCase().contains("test") ? TEST : REAL;
    }

    public MeasuredDataPublication convertToJson(final Map<TmsStation, List<SensorValueDto>> stations, final Instant updated) {

        final MeasuredDataPublication publication =
                new MeasuredDataPublication()
                        .withPublicationTime(updated)
                        .withPublicationCreator(TmsStation2Datex2ConverterJsonCommon.getInternationalIdentifier())
                        .withLang("fi")
                        .withHeaderInformation(TmsStation2Datex2ConverterJsonCommon.getHeaderInformation(informationStatus))
                        .withMeasurementSiteTableReference(Collections.singletonList(
                                new MeasurementSiteTableVersionedReferenceG()
                                        .withIdG(TmsStation2Datex2ConverterCommon.MEASUREMENT_SITE_TABLE_IDENTIFIER)
                                // Optional, no need to ref specific version of TMS station table
                                //.withVersion(metadata update time))
                                )
                        );

        publication.setSiteMeasurements(
                stations.entrySet().stream().map((station) -> getSiteMeasurements(station.getKey(), station.getValue())).toList());

        return publication;
    }

    private static SiteMeasurements getSiteMeasurements(final TmsStation station, final List<SensorValueDto> sensorValues) {

        final SiteMeasurements measurementSite =
                new SiteMeasurements()
                        .withMeasurementSiteReference(new MeasurementSiteVersionedReferenceG()
                                        .withIdG(station.getRoadStationNaturalId().toString())
                                        .withVersionG(String.valueOf(station.getMaxModified()))
                        );

        final List<RoadStationSensor> requiredSensors =
                filterSortAndFillInMissingSensors(station.getRoadStation().getRoadStationSensors());

        final List<SiteMeasurementsIndexPhysicalQuantityG>
                indexedSiteMeasurementsPhysicalQuantities =
                getSiteMeasurementsPhysicalQuantities(requiredSensors, sensorValues);

        measurementSite.withPhysicalQuantity(indexedSiteMeasurementsPhysicalQuantities);

        final Instant minMeasuredTime = measurementSite.getPhysicalQuantity().stream()
                .map(a -> {
                    final MeasurementOrCalculationTime timeS = ObjectUtil.callAndIgnoreExeption(() ->
                            a.getPhysicalQuantity().getRoaSinglePhysicalQuantity().getBasicData().getRoaTrafficSpeed().getMeasurementOrCalculationTime());
                    final MeasurementOrCalculationTime timeF = ObjectUtil.callAndIgnoreExeption(() -> a.getPhysicalQuantity().getRoaSinglePhysicalQuantity().getBasicData().getRoaTrafficFlow()
                            .getMeasurementOrCalculationTime());
                    return TimeUtil.getGreatest(timeS != null ? timeS.getTimeValue() : null, timeF != null ? timeF.getTimeValue() : null);
                }).filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);

        measurementSite.withMeasurementTimeDefault(new MeasurementOrCalculationTime()
                .withTimeValue(minMeasuredTime)
                .withTimeMeaning(new TimeMeaningEnumG().withValue(TimeMeaningEnumG.TimeMeaningEnum.END_TIME)));

        return measurementSite;
    }

    private static List<SiteMeasurementsIndexPhysicalQuantityG> getSiteMeasurementsPhysicalQuantities(
            final List<RoadStationSensor> requiredSensors, final List<SensorValueDto> values) {
        final Map<Long, SensorValueDto> valuesMap = filterAllowedSensorValuesAndMapWithNaturalId(values);

        final List<SiteMeasurementsIndexPhysicalQuantityG> measurements = new ArrayList<>();
        for (int i = 0; i < requiredSensors.size(); i++) {
            final RoadStationSensor sensor = requiredSensors.get(i);
            final SensorValueDto value = valuesMap.get(sensor.getNaturalId());
            final PhysicalQuantityG quantity = getSinglePhysicalQuantity(sensor, value);
            measurements.add(new SiteMeasurementsIndexPhysicalQuantityG(quantity, i+1));
        }
        return measurements;
    }

    private static PhysicalQuantityG getSinglePhysicalQuantity(final RoadStationSensor sensor,
                                                              final SensorValueDto sensorValue) {
        final SinglePhysicalQuantity quantity =
                new SinglePhysicalQuantity()
                        .withBasicData(getBasicData(sensor, sensorValue));
        if (quantity.getBasicData() == null || sensorValue == null) {
            quantity.withPhysicalQuantityFault(Collections.singletonList(
                    new PhysicalQuantityFault().withPhysicalQuantityFaultType(
                            new PhysicalQuantityFaultEnumG().withValue(
                                    PhysicalQuantityFaultEnumG.PhysicalQuantityFaultEnum.NO_DATA_VALUES_AVAILABLE))
                    ));
        }
        return new PhysicalQuantityG().withRoaSinglePhysicalQuantity(quantity);
    }

    private static BasicDataG getBasicData(final RoadStationSensor sensor,
                                                                                         final SensorValueDto sensorValue) {
        final BasicDataG trafficData = new BasicDataG();

        if (sensor.isFlowSensor() || sensor.isSpeedSensor()) {

            if (sensor.isFlowSensor()) {
                final TrafficFlow trafficFlow = new TrafficFlow();
                trafficData.withRoaTrafficFlow(trafficFlow);
                if (sensorValue != null) {
                    final int value = BigDecimal.valueOf(sensorValue.getSensorValue()).round(MathContext.UNLIMITED).intValue();
                    trafficFlow.withVehicleFlow(new VehicleFlowValue().withVehicleFlowRate(value));
                }
            } else  { // == sensor.isSpeedSensor()
                final TrafficSpeed trafficSpeed = new TrafficSpeed();
                trafficData.withRoaTrafficSpeed(trafficSpeed);
                if (sensorValue != null) {
                    trafficSpeed.withAverageVehicleSpeed(new SpeedValue().withSpeed(sensorValue.getSensorValue()));
                }
            }
            withMeasurementOrCalculationTime(trafficData, sensorValue);
            return trafficData;
        } else {
            log.error("method=getBasicData called with sensor that is not flow or speed sensor {}", sensor);
            return null;
        }
    }

    private static void withMeasurementOrCalculationTime(final BasicDataG basicData,
                                                         final SensorValueDto sensorValue) {

        if (sensorValue != null) {
            final MeasurementOrCalculationTime time = new MeasurementOrCalculationTime()
                    .withTimeValue(sensorValue.getMeasuredTime().toInstant())
                    .withTimeMeaning(new TimeMeaningEnumG().withValue(TimeMeaningEnumG.TimeMeaningEnum.END_TIME));

            time.withPeriod(new Period()
                    .withPeriodName(getMultiLangualString(Pair.of(sensorValue.getSensorNameFi(), "fi")))
            );
            //  Fixed period
            if (sensorValue.getTimeWindowStart() != null && sensorValue.getTimeWindowEnd() != null) {
                time.getPeriod()
                        .withStartOfPeriod(sensorValue.getTimeWindowStart().toInstant())
                        .withEndOfPeriod(sensorValue.getTimeWindowEnd().toInstant());
            } else { // running period
                try {
                    final Integer periodTimeS = resolvePeriodSecondsFromSensorName(sensorValue.getSensorNameFi());
                    final Instant end = sensorValue.getMeasuredTime().toInstant();
                    final Instant start = end.minus(periodTimeS, ChronoUnit.SECONDS);
                    time.getPeriod()
                            .withStartOfPeriod(start)
                            .withEndOfPeriod(end);
                } catch (final Exception e) {
                    log.error("method=withMeasurementOrCalculationTime Failed to calculate time period for sensor {}",
                            sensorValue.getSensorNameFi(), e);
                }
            }
            if (basicData.getRoaTrafficSpeed() != null) {
                basicData.getRoaTrafficSpeed().withMeasurementOrCalculationTime(time);
            } if (basicData.getRoaTrafficFlow() != null) {
                basicData.getRoaTrafficFlow().withMeasurementOrCalculationTime(time);
            }
        }
    }
}
