package fi.livi.digitraffic.tie.service.datex2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.datex2.Accident;
import fi.livi.digitraffic.tie.datex2.Comment;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.GenericPublication;
import fi.livi.digitraffic.tie.datex2.MultilingualString;
import fi.livi.digitraffic.tie.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2DetailedMessageType;

@Import({ JacksonAutoConfiguration.class })
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

    @Test(expected = IllegalArgumentException.class)
    public void getSituationPublicationUnsuportedExeption() {
        final D2LogicalModel d2 = new D2LogicalModel().withPayloadPublication(new GenericPublication());
        Datex2Helper.getSituationPublication(d2);
        fail("Should not go here");
    }

    @Test
    public void checkD2HasOnlyOneSituation() {
        final D2LogicalModel d2 = createD2LogicalModelWithSituationPublications(new Situation());
        Datex2Helper.checkD2HasOnlyOneSituation(d2); // no exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkD2HasOnlyOneSituationFails() {
        final D2LogicalModel d2 = createD2LogicalModelWithSituationPublications(new Situation(), new Situation());
        Datex2Helper.checkD2HasOnlyOneSituation(d2); // no exception
    }

    @Test
    public void resolveMessageType() {
        for( final Datex2DetailedMessageType type : Datex2DetailedMessageType.values()) {
            Situation s = createSituationWithComment(type.getToken());
            assertEquals(type, Datex2Helper.resolveMessageType(s));
        }
    }

    private Situation createSituationWithComment(String token) {
        final Instant now = Instant.now();
        final Situation s = createSituationWithRecordsVersionTimes(now.minusSeconds(60 * 2), now.minusSeconds(60), now);
        s.getSituationRecords().get(0).withGeneralPublicComments(createGeneralPublicComments("Diipadaapaa", token, "Hello World!"));
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

    private static Situation createSituationWithRecordsVersionTimes(Instant...versionTimes) {
        final List<SituationRecord> records = Arrays.stream(versionTimes).map(Datex2HelperTest::createSituationRecord).collect(Collectors.toList());
        return new Situation().withSituationRecords(records);
    }

    private static SituationRecord createSituationRecord(final Instant versionTime) {
        return new Accident().withSituationRecordVersionTime(versionTime);
    }

    private static D2LogicalModel createD2LogicalModelWithSituationPublications(Situation...situations) {
        final SituationPublication sp = new SituationPublication();
        sp.withSituations(situations);
        return new D2LogicalModel().withPayloadPublication(sp);
    }
}
