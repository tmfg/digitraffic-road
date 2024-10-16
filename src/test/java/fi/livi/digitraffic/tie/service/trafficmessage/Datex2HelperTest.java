package fi.livi.digitraffic.tie.service.trafficmessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.Accident;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.Comment;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.GenericPublication;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.MultilingualString;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.MultilingualStringValue;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.Situation;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationRecord;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.TrafficAnnouncementType;

public class Datex2HelperTest extends AbstractServiceTest {

    @Autowired
    protected ObjectMapper objectMapper;

    public static final Instant TIME_NOW = Instant.now().with(ChronoField.MILLI_OF_SECOND, 123);
    public static final ZonedDateTime TIME_NOW_ZONED = TIME_NOW.atZone(ZoneOffset.UTC);
    public static final Instant TIME_MILLIS_IN_FUTURE = TIME_NOW.with(ChronoField.MILLI_OF_SECOND, 321);
    public static final Instant TIME_SECONDS_IN_FUTURE = Instant.ofEpochMilli(TIME_NOW.toEpochMilli()+1000); // +1s
    public static final Instant TIME_SECONDS_IN_PAST = Instant.ofEpochMilli(TIME_NOW.toEpochMilli()-1000); // +1s

    @Test
    public void isUpdatedRecord() {

        // Millis don't matter
        final SituationRecord millisDiff = new Accident().withSituationRecordVersionTime(TIME_MILLIS_IN_FUTURE);
        assertFalse(Datex2Helper.isUpdatedRecord(TIME_NOW, millisDiff));

        // Second in future -> is updated
        final SituationRecord secondsDiff = new Accident().withSituationRecordVersionTime(TIME_SECONDS_IN_FUTURE);
        assertTrue(Datex2Helper.isUpdatedRecord(TIME_NOW, secondsDiff));

        // Second in past -> not updated
        final SituationRecord secondsPast = new Accident().withSituationRecordVersionTime(TIME_SECONDS_IN_PAST);
        assertFalse(Datex2Helper.isUpdatedRecord(TIME_NOW, secondsPast));
    }

    @Test
    public void isNewOrUpdatedSituation() {
        final Situation sNow = createSituationWithRecordsVersionTimes(TIME_MILLIS_IN_FUTURE);
        assertFalse(Datex2Helper.isNewOrUpdatedSituation(TIME_NOW, sNow));
        assertFalse(Datex2Helper.isNewOrUpdatedSituation(TIME_NOW_ZONED, sNow));

        final Situation sFuture = createSituationWithRecordsVersionTimes(TIME_SECONDS_IN_FUTURE);
        assertTrue(Datex2Helper.isNewOrUpdatedSituation(TIME_NOW, sFuture));
        assertTrue(Datex2Helper.isNewOrUpdatedSituation(TIME_NOW_ZONED, sFuture));

        final Situation sPast = createSituationWithRecordsVersionTimes(TIME_SECONDS_IN_PAST);
        assertFalse(Datex2Helper.isNewOrUpdatedSituation(TIME_NOW, sPast));
        assertFalse(Datex2Helper.isNewOrUpdatedSituation(TIME_NOW_ZONED, sPast));
    }

    @Test
    public void getSituationPublication() {
        final SituationPublication sp = new SituationPublication();
        final D2LogicalModel d2 = new D2LogicalModel().withPayloadPublication(sp);
        final SituationPublication spResult = Datex2Helper.getSituationPublication(d2);
        assertSame(sp, spResult);
    }

    @Test
    public void getSituationPublicationUnsuportedExeption() {
        final D2LogicalModel d2 = new D2LogicalModel().withPayloadPublication(new GenericPublication());

        assertThrows(IllegalArgumentException.class, () -> Datex2Helper.getSituationPublication(d2));
    }

    @Test
    public void checkD2HasOnlyOneSituation() {
        final D2LogicalModel d2 = createD2LogicalModelWithSituationPublications(new Situation());
        Datex2Helper.checkD2HasOnlyOneSituation(d2); // no exception
    }

    @Test
    public void checkD2HasOnlyOneSituationFails() {
        final D2LogicalModel d2 = createD2LogicalModelWithSituationPublications(new Situation(), new Situation());

        assertThrows(IllegalArgumentException.class, () -> {
            Datex2Helper.checkD2HasOnlyOneSituation(d2); // no exception
        });
    }

    // TODO: Datex2Helper.resolveTrafficAnnouncementTypeFromText()
    @Test
    public void resolveMessageType() {
        assertEquals(SituationType.TRAFFIC_ANNOUNCEMENT, Datex2Helper.resolveSituationTypeFromText("Foo", "Tie 14866, eli Kyläniementie, Ruokolahti. Liikennetiedote.", "Bar"));
        assertEquals(SituationType.TRAFFIC_ANNOUNCEMENT, Datex2Helper.resolveSituationTypeFromText("Foo", "Tie 4, eli Sodankyläntie, Rovaniemi. Liikennetiedote onnettomuudesta. Tilanne ohi.", "Bar"));
        assertEquals(SituationType.TRAFFIC_ANNOUNCEMENT, Datex2Helper.resolveSituationTypeFromText("Foo", "Tie 4, Inari. Ensitiedote liikenneonnettomuudesta.", "Bar"));
        assertEquals(SituationType.TRAFFIC_ANNOUNCEMENT, Datex2Helper.resolveSituationTypeFromText("Foo", "Tie 20, Oulu, Pudasjärvi. Vahvistamaton havainto.", "Bar"));
        assertEquals(SituationType.EXEMPTED_TRANSPORT, Datex2Helper.resolveSituationTypeFromText("Foo", "Erikoiskuljetus. Pirkanmaa", "Bar"));
        assertEquals(SituationType.WEIGHT_RESTRICTION, Datex2Helper.resolveSituationTypeFromText("Foo", "Tie 130, Lempäälä. Painorajoitus. ", "Bar"));
        assertEquals(SituationType.ROAD_WORK, Datex2Helper.resolveSituationTypeFromText("Foo", "Tie 3, eli Tampereen Läntinen Kehätie, Pirkkala. Tietyö. Tie 3, eli Tampereen Läntinen Kehätie, Pirkkala.", "Bar"));
        assertEquals(SituationType.ROAD_WORK, Datex2Helper.resolveSituationTypeFromText("Foo", "Tie 3172, Hollola. Tietyövaihe.", "Bar"));
    }

    @Test
    public void resolveTrafficAnnouncementType() {
        assertEquals(TrafficAnnouncementType.GENERAL, Datex2Helper.resolveTrafficAnnouncementTypeFromText("Tie 14866, eli Kyläniementie, Ruokolahti. Liikennetiedote."));
        assertEquals(TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT, Datex2Helper.resolveTrafficAnnouncementTypeFromText("Tie 4, eli Sodankyläntie, Rovaniemi. Ensitiedote liikenneonnettomuudesta."));
        assertEquals(TrafficAnnouncementType.ACCIDENT_REPORT, Datex2Helper.resolveTrafficAnnouncementTypeFromText("Tie 4, eli Sodankyläntie, Rovaniemi. Liikennetiedote onnettomuudesta."));
        assertEquals(TrafficAnnouncementType.ENDED, Datex2Helper.resolveTrafficAnnouncementTypeFromText("Tie 4, eli Sodankyläntie, Rovaniemi. Liikennetiedote onnettomuudesta. Tilanne ohi."));
        assertEquals(TrafficAnnouncementType.UNCONFIRMED_OBSERVATION, Datex2Helper.resolveTrafficAnnouncementTypeFromText("Tie 20, Oulu, Pudasjärvi. Vahvistamaton havainto."));
        assertEquals(TrafficAnnouncementType.RETRACTED, Datex2Helper.resolveTrafficAnnouncementTypeFromText("Tie 14866, eli Kyläniementie, Ruokolahti. Liikennetiedote peruttu."));
    }

    private Situation createSituationWithComment(final String[] token) {
        final String tokenToUse =
            token == null ? "" :
                            token[TestUtils.getRandom(0, token.length)];
        final Instant now = Instant.now();
        final Situation s = createSituationWithRecordsVersionTimes(now.minusSeconds(60 * 2), now.minusSeconds(60), now);
        s.getSituationRecords().get(0).withGeneralPublicComments(createGeneralPublicComments("Diipadaapaa", tokenToUse, "Hello World!"));
        return s;
    }

    private Collection<Comment> createGeneralPublicComments(final String...comments) {
        return Arrays.stream(comments)
            .map(c -> new Comment()
                .withComment(new MultilingualString()
                    .withValues(new MultilingualString.Values()
                        .withValues(new MultilingualStringValue()
                            .withLang("fi")
                            .withValue("Testing\nmultiline " +  c + " comment."))))
        ).collect(Collectors.toList());
    }

    private static Situation createSituationWithRecordsVersionTimes(final Instant...versionTimes) {
        final List<SituationRecord> records = Arrays.stream(versionTimes).map(Datex2HelperTest::createSituationRecord).collect(Collectors.toList());
        return new Situation().withSituationRecords(records);
    }

    private static SituationRecord createSituationRecord(final Instant versionTime) {
        return new Accident().withSituationRecordVersionTime(versionTime);
    }

    private static D2LogicalModel createD2LogicalModelWithSituationPublications(final Situation...situations) {
        final SituationPublication sp = new SituationPublication();
        sp.withSituations(situations);
        return new D2LogicalModel().withPayloadPublication(sp);
    }
}
