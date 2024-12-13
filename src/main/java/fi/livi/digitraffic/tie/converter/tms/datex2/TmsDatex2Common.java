package fi.livi.digitraffic.tie.converter.tms.datex2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;

@Component
public class TmsDatex2Common {
    private static final Logger log = LoggerFactory.getLogger(TmsDatex2Common.class);

    public static final String MEASUREMENT_SITE_NATIONAL_IDENTIFIER = "Fintraffic";
    public static final String MEASUREMENT_SITE_TABLE_IDENTIFIER = "TMS_STATIONS";
    public static final Collection<String> ALLOWED_SENSOR_NAMES = Arrays.asList(
            "KESKINOPEUS_5MIN_LIUKUVA_SUUNTA1",
            "KESKINOPEUS_5MIN_LIUKUVA_SUUNTA2",
            "KESKINOPEUS_60MIN_KIINTEA_SUUNTA1",
            "KESKINOPEUS_60MIN_KIINTEA_SUUNTA2",
            "OHITUKSET_5MIN_LIUKUVA_SUUNTA1",
            "OHITUKSET_5MIN_LIUKUVA_SUUNTA2",
            "OHITUKSET_60MIN_KIINTEA_SUUNTA1",
            "OHITUKSET_60MIN_KIINTEA_SUUNTA2"
    );

    public static final String GENERIC_NAME_LANG_CODE = "fi-gen";

    private static double SENSOR_VALUE_ACCURACY_PERCENTAGE;

    @Value("${tms.sensorValueAccuracyPercentage.totalAmounts}")
    protected void setSensorValueAccuracyPercentage(final String sensorValueAccuracyPercentage) {
        TmsDatex2Common.SENSOR_VALUE_ACCURACY_PERCENTAGE = Double.parseDouble(sensorValueAccuracyPercentage);
    }

    public static double getSensorValueAccuracyPercentage() {
        return SENSOR_VALUE_ACCURACY_PERCENTAGE;
    }

    public static Integer resolvePeriodSecondsFromSensorName(final String nameFi) {
        // e.g. OHITUKSET_5MIN_LIUKUVA_YHTEENSÄ
        try {
            final String value = StringUtils.substringAfterLast(StringUtils.substringBefore(nameFi, "MIN_"), "_");
            if (StringUtils.isNotBlank(value) && StringUtils.isNumeric(value)) {
                return Integer.parseInt(value)*60;
            }
        } catch (final Exception e) {
            log.error("method=resolvePeriodSecondsFromName failed for {}", nameFi, e);
        }
        return null;
    }

    public static List<RoadStationSensor> filterSortAndFillInMissingSensors(final List<RoadStationSensor> sensors) {
        final List<RoadStationSensor> filteredSensors = new ArrayList<>(filterAllowedSensors(sensors));
        final List<String> missingSensorsNames = ALLOWED_SENSOR_NAMES.stream()
                .filter(name -> filteredSensors.stream().noneMatch(sensor -> sensor.getNameFi().equals(name)))
                .toList();

        missingSensorsNames.forEach(missingSensorName -> {
            final RoadStationSensor s = new RoadStationSensor();
            s.setNameFi(missingSensorName);
            s.setNaturalId(getDummyNaturalIdForSensorName(missingSensorName));
            filteredSensors.add(s);
        });
        return sortSensorsByNaturalId(filteredSensors);
    }

    private static List<RoadStationSensor> filterAllowedSensors(final List<RoadStationSensor> sensors) {
        return sensors.stream()
                .filter(s -> ALLOWED_SENSOR_NAMES.contains(s.getNameFi()))
                .toList();
    }

    private static List<RoadStationSensor> sortSensorsByNaturalId(final List<RoadStationSensor> sensors) {
        return sensors.stream()
                .sorted(Comparator.comparingLong(RoadStationSensor::getNaturalId))
                .toList();
    }

    public static Map<Long, SensorValueDtoV1> filterAllowedSensorValuesAndMapWithNaturalId(final List<SensorValueDtoV1> values) {
        return values.stream()
                .filter(v -> ALLOWED_SENSOR_NAMES.contains(v.getSensorNameFi()))
                .collect(Collectors.toMap(SensorValueDtoV1::getSensorNaturalId, Function.identity()));
    }

    private static int getDummyNaturalIdForSensorName(final String missingSensor) {
        return -1 * missingSensor.hashCode();
    }

    public static fi.livi.digitraffic.tie.metadata.geojson.Point resolveETRS89PointLocation(final RoadStation rs) {
        if (rs.getLatitude() != null && rs.getLongitude() != null) {
            if (rs.getAltitude() != null) {
                return new fi.livi.digitraffic.tie.metadata.geojson.Point(
                        rs.getLongitude().doubleValue(),
                        rs.getLatitude().doubleValue(),
                        rs.getAltitude().doubleValue());
            } else {
                return new fi.livi.digitraffic.tie.metadata.geojson.Point(
                        rs.getLongitude().doubleValue(),
                        rs.getLatitude().doubleValue());
            }
        }
        return null;
    }

}
