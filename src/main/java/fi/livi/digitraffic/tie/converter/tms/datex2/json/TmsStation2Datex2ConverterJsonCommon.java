package fi.livi.digitraffic.tie.converter.tms.datex2.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.common.util.ObjectUtil;
import fi.livi.digitraffic.tie.converter.tms.datex2.TmsDatex2Common;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.ConfidentialityValueEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.HeaderInformation;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InformationStatusEnumG.InformationStatusEnum;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.InternationalIdentifier;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MultiLingualStringValue;
import fi.livi.digitraffic.tie.external.datex2.v3_5.json.MultilingualString;
import fi.livi.digitraffic.tie.model.tms.TmsStation;

@ConditionalOnWebApplication
@Component
public class TmsStation2Datex2ConverterJsonCommon {

    public static InternationalIdentifier getInternationalIdentifier() {
        return new InternationalIdentifier()
                .withCountry("FI")
                .withNationalIdentifier(TmsDatex2Common.MEASUREMENT_SITE_NATIONAL_IDENTIFIER);
    }

    public static MultilingualString getMeasurementSiteName(final TmsStation station) {
        final String fi = station.getRoadStation().getNameFi();
        final String sv = station.getRoadStation().getNameSv();
        final String en = station.getRoadStation().getNameEn();
        final MultilingualString values = new MultilingualString().withValues(new ArrayList<>());

        ObjectUtil.callIfNotNull(fi, () -> values.getValues().add(new MultiLingualStringValue("fi", fi)));
        ObjectUtil.callIfNotNull(sv, () -> values.getValues().add(new MultiLingualStringValue("sv", sv)));
        ObjectUtil.callIfNotNull(en, () -> values.getValues().add(new MultiLingualStringValue("en", en)));

        return values;
    }

    public static HeaderInformation getHeaderInformation(final InformationStatusEnum informationStatus) {
        return new HeaderInformation()
                .withConfidentiality(new ConfidentialityValueEnumG().withValue(ConfidentialityValueEnumG.ConfidentialityValueEnum.NO_RESTRICTION))
                .withInformationStatus(new InformationStatusEnumG().withValue(informationStatus));
    }

    @SafeVarargs
    public static MultilingualString getMultiLangualString(final Pair<String, String>...langValue) {
        final List<MultiLingualStringValue> values = Arrays.stream(langValue)
                .map(v -> new MultiLingualStringValue(v.getLeft(), v.getRight())).toList();
        return new MultilingualString().withValues(values);
    }

    public static MultilingualString getMultilingualString(final String lang, final String value) {
        return new MultilingualString(Collections.singletonList(new MultiLingualStringValue(lang, value)));
    }

}
