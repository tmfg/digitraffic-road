package fi.livi.digitraffic.tie.converter.tms.datex2;

import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.ALLOWED_SENSOR_NAMES;
import static fi.livi.digitraffic.tie.converter.tms.datex2.TmsStation2Datex2ConverterCommon.MEASUREMENT_SITE_NATIONAL_IDENTIFIER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.common.util.ObjectUtil;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.ConfidentialityValueEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.HeaderInformation;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG.InformationStatusEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InternationalIdentifier;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MultiLingualStringValue;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MultilingualString;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.tms.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStation2Datex2ConverterJsonCommon {
    private static final Logger log = LoggerFactory.getLogger(TmsStation2Datex2ConverterJsonCommon.class);


    public static InternationalIdentifier getInternationalIdentifier() {
        return new InternationalIdentifier()
                .withCountry("FI")
                .withNationalIdentifier(MEASUREMENT_SITE_NATIONAL_IDENTIFIER);
    }

    public static MultilingualString getMeasurementSiteName(final TmsStation station) {
        final String fi = station.getRoadStation().getNameFi();
        final String sv = station.getRoadStation().getNameSv();
        final String en = station.getRoadStation().getNameEn();
        final MultilingualString values = new MultilingualString().withValues(new ArrayList<>());

        ObjectUtil.callIfNotNull(fi, () -> values.getValues().add(new MultiLingualStringValue(fi, "fi")));
        ObjectUtil.callIfNotNull(sv, () -> values.getValues().add(new MultiLingualStringValue(sv, "sv")));
        ObjectUtil.callIfNotNull(en, () -> values.getValues().add(new MultiLingualStringValue(en, "en")));

        return values;
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

    public static Map<Long, SensorValueDto> filterAllowedSensorValuesAndMapWithNaturalId(final List<SensorValueDto> values) {
        return values.stream()
                .filter(v -> ALLOWED_SENSOR_NAMES.contains(v.getSensorNameFi()))
                .collect(Collectors.toMap(SensorValueDto::getSensorNaturalId, Function.identity()));
    }

    public static HeaderInformation getHeaderInformation(final InformationStatusEnum informationStatus) {
        return new HeaderInformation()
                .withConfidentiality(new ConfidentialityValueEnumG().withValue(ConfidentialityValueEnumG.ConfidentialityValueEnum.NO_RESTRICTION))
                .withInformationStatus(new InformationStatusEnumG().withValue(informationStatus));
    }

    @SafeVarargs
    public static MultilingualString getMultiLangualString(final Pair<String, String>...valueLang) {
        final List<MultiLingualStringValue> values = Arrays.stream(valueLang)
                .map(v -> new MultiLingualStringValue(v.getLeft(), v.getRight())).toList();
        return new MultilingualString().withValues(values);
    }

    public static MultilingualString getMultilingualString(final String lang, final String value) {
        return new MultilingualString(Collections.singletonList(new MultiLingualStringValue(value, lang)));
    }

    private static int getDummyNaturalIdForSensorName(final String missingSensor) {
        return -1 * missingSensor.hashCode();
    }
}
