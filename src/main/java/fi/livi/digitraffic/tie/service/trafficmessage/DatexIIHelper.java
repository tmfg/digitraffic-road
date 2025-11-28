package fi.livi.digitraffic.tie.service.trafficmessage;

import java.time.Instant;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.Situation;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationRecord;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.TrafficAnnouncementType;

public class DatexIIHelper {
    private static final Logger log = LoggerFactory.getLogger(DatexIIHelper.class);

    public static boolean isNewOrUpdatedSituation(final Instant latestVersionTime, final Situation situation) {
        // does any record have new version time?
        return latestVersionTime == null || situation.getSituationRecords().stream().anyMatch(r -> isUpdatedRecord(latestVersionTime, r));
    }

    public static boolean isUpdatedRecord(final Instant latestVersionTime, final SituationRecord record) {
        // different resolution, so remove fractions of second
        final Instant vTime = TimeUtil.withoutMillis(record.getSituationRecordVersionTime());
        return vTime.isAfter(TimeUtil.withoutMillis(latestVersionTime) );
    }

    public static SituationPublication getSituationPublication(final D2LogicalModel model) {
        if (model.getPayloadPublication() instanceof SituationPublication) {
            return (SituationPublication) model.getPayloadPublication();
        } else {
            final String err = "method=getSituationPublication Not SituationPublication available for " + ToStringHelper.toStringFull(model.getPayloadPublication());
            log.error(err);
            throw new IllegalArgumentException(err);
        }
    }

    public static void checkD2HasOnlyOneSituation(final D2LogicalModel d2) {
        final int situations = getSituationPublication(d2).getSituations().size();
        if ( situations > 1 ) {
            log.error("method=checkOnyOneSituation D2LogicalModel had {) situations. Only 1 is allowed in this service.");
            throw new IllegalArgumentException("D2LogicalModel passed to Datex2UpdateService can only have one situation per message, " +
                                               "there was " + situations);
        }
    }

    public static SituationType resolveSituationTypeFromText(final String...texts) {
        if (contains(texts, "Liikennetiedote.", "Liikennetiedote ", "Tilanne ohi.", "Ensitiedote ", "Vahvistamaton havainto.")) {
            return SituationType.TRAFFIC_ANNOUNCEMENT;
        } else if (contains(texts,"Erikoiskuljetus.")) {
            return SituationType.EXEMPTED_TRANSPORT;
        } else if (contains(texts,"Painorajoitus.")) {
            return SituationType.WEIGHT_RESTRICTION;
        } else if (contains(texts,"Tietyö.", "Tietyövaihe.")) {
            return SituationType.ROAD_WORK;
        }
        return SituationType.TRAFFIC_ANNOUNCEMENT;
    }

    private static boolean contains(final String[] values, final String...matchTo) {
        return Arrays.stream(values)
            .anyMatch(text -> Arrays.stream(matchTo)
                                .anyMatch(match -> Strings.CS.contains(text, match)));
    }

    public static TrafficAnnouncementType resolveTrafficAnnouncementTypeFromText(final String text) {
        final SituationType st = resolveSituationTypeFromText(text);
        if (st != SituationType.TRAFFIC_ANNOUNCEMENT) {
            return null;
        }
        if (Strings.CS.contains(text, "Tilanne ohi.")) {
            return TrafficAnnouncementType.ENDED;
        } else if (Strings.CS.contains(text, "Ensitiedote ")) {
            return TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT;
        } else if (Strings.CS.contains(text,"peruttu.")) {
            return TrafficAnnouncementType.RETRACTED;
        } else if (Strings.CS.contains(text, "Liikennetiedote onnettomuudesta")) {
            return TrafficAnnouncementType.ACCIDENT_REPORT;
        } else if (Strings.CS.contains(text, "Vahvistamaton havainto.")) {
            return TrafficAnnouncementType.UNCONFIRMED_OBSERVATION;
        } else // else if (Strings.CS.contains(text, "Liikennetiedote.", "Liikennetiedote ")) {
        return TrafficAnnouncementType.GENERAL;
    }

}
