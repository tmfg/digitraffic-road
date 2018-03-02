package fi.livi.digitraffic.tie.helper;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import fi.livi.digitraffic.tie.AbstractTest;

@RunWith(JUnit4.class)
public class DateHelpperTest extends AbstractTest {

    @Test
    public void XMLGregorianCalendarToZonedDateTime() throws DatatypeConfigurationException {
        final String DATE_STRING_1 = "2016-01-22T10:00:00+02:00";
        final String DATE_STRING_2 = "2016-06-22T10:10:01.102+03:00";

        final GregorianCalendar wc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_1)));
        final ZonedDateTime winterTime = DateHelper.toZonedDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(wc));
        Assert.assertEquals(DATE_STRING_1, ISO_OFFSET_DATE_TIME.format(winterTime));

        final GregorianCalendar sc = GregorianCalendar.from((ZonedDateTime.parse(DATE_STRING_2)));
        final ZonedDateTime summerTime = DateHelper.toZonedDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(sc));
        Assert.assertEquals(DATE_STRING_2, ISO_OFFSET_DATE_TIME.format(summerTime));
    }

}
