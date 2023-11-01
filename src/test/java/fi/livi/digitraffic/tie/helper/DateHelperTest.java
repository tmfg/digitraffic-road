package fi.livi.digitraffic.tie.helper;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fi.livi.digitraffic.tie.AbstractTest;

public class DateHelperTest extends AbstractTest {

    private static final String DATE_STRING_OFFSET_2 = "2016-01-22T10:00:01+02:00";
    private static final String DATE_STRING_Z = "2016-01-22T08:00:01Z";
    private static final String DATE_STRING_MILLIS_Z = "2016-01-22T08:00:01.500Z";
    private static final String XML_DATE_STRING_Z = "2016-01-22T08:00:01.000Z";



    private final static String ISO_DATE_TIME_WITH_Z_REGEX_PATTERN =      "([0-9]{4})-(1[0-2]|0[1-9])-([0-3][0-9])T([0-2][0-9]):([0-5][0-9]):([0-5][0-9])(\\.(\\d{3}|\\d{6}))?Z";
    private final static String ISO_DATE_TIME_WITH_OFFSET_REGEX_PATTERN = "([0-9]{4})-(1[0-2]|0[1-9])-([0-3][0-9])T([0-2][0-9]):([0-6][0-9])(:([0-6][0-9])){0,1}(\\.[0-9]{0,6})?[+|-].*";

    public static final Matcher<String> ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER = Matchers.matchesRegex(ISO_DATE_TIME_WITH_Z_REGEX_PATTERN);
    public static final Matcher<String> ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER = Matchers.matchesRegex("[\\s\\S]*" +
        ISO_DATE_TIME_WITH_Z_REGEX_PATTERN + "[\\s\\S]*");
    public static final Matcher<String> NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER = Matchers.not(Matchers.matchesRegex(
        ISO_DATE_TIME_WITH_OFFSET_REGEX_PATTERN));
    public static final Matcher<String> NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER = Matchers.not(Matchers.matchesRegex("[\\s\\S]*" +
        ISO_DATE_TIME_WITH_OFFSET_REGEX_PATTERN + "[\\s\\S]*"));
    public static final Matcher<String> ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER =
        Matchers.allOf(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER, NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER);
    public static final Matcher<String> ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER =
        Matchers.allOf(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER, NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER);
    public static final ResultMatcher ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER =
        MockMvcResultMatchers.content().string(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER);

    @Test
    public void getInLastModifiedHeaderFormat() throws ParseException {
        final String srcString = "Tue, 03 Sep 2019 13:56:36 GMT";
        final java.util.Date srcDate = DateUtils.parseDate(srcString, DateHelper.LAST_MODIFIED_FORMAT);
        final Instant srcInstant = Instant.ofEpochMilli(srcDate.getTime());
        assertEquals(srcString, DateHelper.getInLastModifiedHeaderFormat(srcInstant));
    }

    /**
     * This is test of test. It checks that ISO_DATE_TIME regex patterns works.
     */
    @Test
    public void isoDateTimeRegexpMatcherMatches() {
        final String DATE_TIME = "2022-01-02T10:31:21";
        final String DATE_TIME_MILLIS = "2022-01-02T10:31:21.123";
        final String DATE_TIME_MIKROS = "2022-01-02T10:31:21.123456";
        final String ZONE_Z = "Z";
        final String ZONE_OFFSET = "+01:00";
        final String RANDOM = RandomStringUtils.random(3) + "\n" + RandomStringUtils.random(3);

        // No extra befor or after allowed
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(RANDOM + DATE_TIME + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ZONE_Z + RANDOM));

        // Digits
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ".1" + ZONE_Z)); // 1-digit millis not allowed
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ".12" + ZONE_Z)); // 2-digit millis not allowed
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ".123" + ZONE_Z)); // 3-digit millis ok
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + "123" + ZONE_Z)); // 3-digit millis without dot not allowed
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "4" + ZONE_Z)); // 4-digit micros not allowed
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "45" + ZONE_Z)); // 5-digit micros not allowed
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "456" + ZONE_Z)); // 6-digit micros ok
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "4567" + ZONE_Z)); // 7-digit micros not allowed
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_Z));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MIKROS + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME + ZONE_OFFSET));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_OFFSET));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MIKROS + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(DATE_TIME_MIKROS + ZONE_OFFSET));

        // All digits should "match" iso with offset -> make no-matcher to not match as ok (offset checker can be looser to match)
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ".1" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ".12" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ".123" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "4" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "45" + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + "456" + ZONE_OFFSET));

        Assertions.assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ZONE_Z));
        Assertions.assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_Z));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + "+"));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME + ZONE_OFFSET));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_OFFSET));

        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER.matches(DATE_TIME + ZONE_Z));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_Z));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER.matches(DATE_TIME + ZONE_OFFSET));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER.matches(DATE_TIME_MILLIS + ZONE_OFFSET));


        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_Z + RANDOM));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + "\n" + DATE_TIME + ZONE_Z + "\n" + RANDOM));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_Z + RANDOM));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_OFFSET + RANDOM));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_OFFSET + RANDOM));

        Assertions.assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_Z + RANDOM));
        Assertions.assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_Z + RANDOM));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + "+" + RANDOM));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_OFFSET + RANDOM));
        Assertions.assertFalse(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_OFFSET + RANDOM));

        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_Z + RANDOM));
        Assertions.assertTrue(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_Z + RANDOM));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME + ZONE_OFFSET + RANDOM));
        Assertions.assertFalse(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_MATCHER.matches(RANDOM + DATE_TIME_MILLIS + ZONE_OFFSET + RANDOM));
    }


    @Test
    public void getNewest() {
        final Instant now = Instant.now();
        final Instant older = now.minusNanos(1);
        final Instant newest = DateHelper.getGreatest(now, older);
        assertEquals(now, newest);
    }

    @Test
    public void xmlGregorianCalendarToZonedDateTimetoAtUtc() throws DatatypeConfigurationException {
        final String DATE_STRING_WINTER = "2016-01-22T10:00:00+02:00";
        final String DATE_STRING_WINTER_Z = "2016-01-22T08:00:00Z";
        final String DATE_STRING_SUMMER = "2016-06-22T10:10:01.102+03:00";
        final String DATE_STRING_SUMMER_Z = "2016-06-22T07:10:01.102Z";

        final GregorianCalendar wc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_WINTER)));
        final ZonedDateTime winterTime = DateHelper.toZonedDateTimeAtUtc(DatatypeFactory.newInstance().newXMLGregorianCalendar(wc));
        assertEquals(DATE_STRING_WINTER_Z, ISO_OFFSET_DATE_TIME.format(winterTime));

        final GregorianCalendar sc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_SUMMER)));
        final ZonedDateTime summerTime = DateHelper.toZonedDateTimeAtUtc(DatatypeFactory.newInstance().newXMLGregorianCalendar(sc));
        assertEquals(DATE_STRING_SUMMER_Z, ISO_OFFSET_DATE_TIME.format(summerTime));
    }

    @Test
    public void xmlGregorianCalendarToInstant() throws DatatypeConfigurationException {
        final GregorianCalendar wc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_OFFSET_2)));
        final Instant instant = DateHelper.toInstant(DatatypeFactory.newInstance().newXMLGregorianCalendar(wc));
        assertEquals(DATE_STRING_Z, instant.toString());
    }

    @Test
    public void zonedDateTimeToInstant() {
        final ZonedDateTime zdt = ZonedDateTime.of(2019, 12, 1, 10, 15, 20, 500, ZoneOffset.UTC);
        final String DATE_STRING_NANOS = "2019-12-01T10:15:20.000000500Z";
        final Instant instant = DateHelper.toInstant(zdt);
        assertEquals(DATE_STRING_NANOS, instant.toString());
    }

    @Test
    public void epocMillisToInstant() {
        final ZonedDateTime zdt = ZonedDateTime.of(2019, 12, 1, 10, 15, 20, 500000000, ZoneOffset.UTC);
        final String DATE_STRING_NANOS = "2019-12-01T10:15:20.500Z";
        final Instant instant = DateHelper.toInstant(zdt.toInstant().toEpochMilli());
        assertEquals(DATE_STRING_NANOS, instant.toString());
    }

    @Test
    public void xmlGregorianCalendarToZonedDateTimeWithoutMillis() throws DatatypeConfigurationException {
        final GregorianCalendar gc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_OFFSET_2).plusNanos(500000000)));
        final XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        final Instant instant = DateHelper.toInstant(xmlDate);
        assertEquals(DATE_STRING_MILLIS_Z, instant.toString());

        final ZonedDateTime zdtWithOutMillis = DateHelper.toZonedDateTimeWithoutMillisAtUtc(xmlDate);
        assertEquals(DATE_STRING_Z, zdtWithOutMillis.toString());
    }

    @Test
    public void zdtToZonedDateTimeAtUtc() {
        final ZonedDateTime timeAtOffset2 = ZonedDateTime.parse(DATE_STRING_OFFSET_2);
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(timeAtOffset2);

        assertEquals(0, utc.getOffset().getTotalSeconds());
        assertEquals(timeAtOffset2.toEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void instantToZonedDateTimeAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(instant);
        assertEquals(0, utc.getOffset().getTotalSeconds());
        assertEquals(instant.getEpochSecond() , utc.toEpochSecond());
    }


    @Test
    public void epochMillisToZonedDateTimeAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(instant.toEpochMilli());
        assertEquals(0, utc.getOffset().getTotalSeconds());
        assertEquals(instant.getEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void dateToZonedDateTimeAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final java.util.Date date = Date.from(instant);
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(date);
        assertEquals(0, utc.getOffset().getTotalSeconds());
        assertEquals(instant.getEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void zdtToXMLGregorianCalendarAtUtc() {
        final ZonedDateTime timeAtOffset2 = ZonedDateTime.parse(DATE_STRING_OFFSET_2);
        final XMLGregorianCalendar xmlUtc = DateHelper.toXMLGregorianCalendarAtUtc(timeAtOffset2);
        assertEquals(XML_DATE_STRING_Z, xmlUtc.toString());
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(xmlUtc);
        assertEquals(timeAtOffset2.toEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void instantToXMLGregorianCalendarAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final XMLGregorianCalendar xmlUtc = DateHelper.toXMLGregorianCalendarAtUtc(instant);
        assertEquals(XML_DATE_STRING_Z, xmlUtc.toString());
        final ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(xmlUtc);
        assertEquals(instant.getEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void zonedDateTimeNowAtUtc() {
        final long now = ZonedDateTime.now().toInstant().getEpochSecond();
        final ZonedDateTime utc = DateHelper.getZonedDateTimeNowAtUtc();

        assertEquals(0, utc.getOffset().getTotalSeconds());
        assertEquals(now , utc.toEpochSecond(), 1.0);
    }

    @Test
    public void zonedDateTimeToSqlTimestamp() {
        final ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
        final Timestamp sqlTimestamp = DateHelper.toSqlTimestamp(now);

        assertEquals(now.toInstant().toEpochMilli(), sqlTimestamp.getTime());
    }

    @Test
    public void sqlTimestampToZonedDateTime() {
        final Timestamp sqlTimestamp = new Timestamp(Instant.now().toEpochMilli());
        final ZonedDateTime zonedDateTime = DateHelper.toZonedDateTimeAtUtc(sqlTimestamp);

        assertEquals(sqlTimestamp.getTime(), zonedDateTime.toInstant().toEpochMilli());
    }
}
