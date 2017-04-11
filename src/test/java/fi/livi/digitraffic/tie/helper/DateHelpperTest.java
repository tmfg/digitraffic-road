package fi.livi.digitraffic.tie.helper;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.time.LocalDateTime;
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
    public void testZonedDateTimeToDate() throws DatatypeConfigurationException {
        GregorianCalendar c = GregorianCalendar.from((ZonedDateTime.parse("2016-01-22T10:00:00Z")));
        LocalDateTime winterLocalDateTime = DateHelper.toLocalDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
        Assert.assertEquals("2016-01-22T12:00:00", ISO_LOCAL_DATE_TIME.format(winterLocalDateTime));

        c = GregorianCalendar.from((ZonedDateTime.parse("2016-06-22T10:10:01.102Z")));
        LocalDateTime summerLocalDateTime = DateHelper.toLocalDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
        Assert.assertEquals("2016-06-22T13:10:01.102", ISO_LOCAL_DATE_TIME.format(summerLocalDateTime));
    }

}
