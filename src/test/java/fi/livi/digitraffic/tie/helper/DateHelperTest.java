package fi.livi.digitraffic.tie.helper;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.tie.AbstractSpringJUnitTest;

public class DateHelperTest extends AbstractSpringJUnitTest {

    private static final String DATE_STRING_OFFSET_2 = "2016-01-22T10:00:01+02:00";
    private static final String DATE_STRING_Z = "2016-01-22T08:00:01Z";
    private static final String DATE_STRING_MILLIS_OFFSET_2 = "2016-01-22T10:00:01.500+02:00";
    private static final String DATE_STRING_MILLIS_Z = "2016-01-22T08:00:01.500Z";
    private static final String XML_DATE_STRING_Z = "2016-01-22T08:00:01.000Z";

    @Test
    public void getNewest() {
        final ZonedDateTime now = DateHelper.getZonedDateTimeNowAtUtc();
        final ZonedDateTime older = now.minusNanos(1);
        final ZonedDateTime newest = DateHelper.getNewestAtUtc(now, older);
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
        ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(xmlUtc);
        assertEquals(timeAtOffset2.toEpochSecond() , utc.toEpochSecond());
    }

    @Test
    public void instantToXMLGregorianCalendarAtUtc() {
        final Instant instant = Instant.parse(DATE_STRING_Z);
        final XMLGregorianCalendar xmlUtc = DateHelper.toXMLGregorianCalendarAtUtc(instant);
        assertEquals(XML_DATE_STRING_Z, xmlUtc.toString());
        ZonedDateTime utc = DateHelper.toZonedDateTimeAtUtc(xmlUtc);
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
