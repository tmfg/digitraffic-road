package fi.livi.digitraffic.tie.service.v1.datex2;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Datex2XmlStringToObjectMarshallerTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(Datex2XmlStringToObjectMarshallerTest.class);

    @Autowired
    private Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;

    // match values tags ending with name Time
    private final static Pattern timesPattern = Pattern.compile("<[^>]*Time>(.+?)</");
    // Match Zulu datetime
    private final static Pattern isoDateTimeZPattern = Pattern.compile("\\d{4}-([0]\\d|1[0-2])-([0-2]\\d|3[01])T([0-2]\\d):([0-6]\\d):([0-6]\\d)(.\\d{3})?Z");

    @Test
    public void allDatesInUtc() throws IOException {
        final String fromXml = readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-15-59-34-896.xml");
        final D2LogicalModel object = datex2XmlStringToObjectMarshaller.convertToObject(fromXml);
        final String toXml = datex2XmlStringToObjectMarshaller.convertToString(object);
        log.info("Check source xml not having Zulu times");
        checkIsoDateFormats(fromXml, false, 13);
        log.info("Check created xml having Zulu times");
        checkIsoDateFormats(toXml, true, 13);

    }

    private void checkIsoDateFormats(final String xml, final boolean shouldMatchIsoFormat, final int timesCount) {
        // Now create matcher object to find times from the xml.
        Matcher timesMatcher = timesPattern.matcher(xml);

        int found = 0;
        while (timesMatcher.find()) {
            final String dateTime = timesMatcher.group(1); // group(0) = whole match
            log.info("Found dateTime: {}", dateTime);
            if (shouldMatchIsoFormat) {
                assertTrue(isoDateTimeZPattern.matcher(dateTime).matches(), "Illegal iso datetime at Zulu " + dateTime);
            } else {
                assertFalse(isoDateTimeZPattern.matcher(dateTime).matches(), "Should not be iso datetime Zulu: " + dateTime);
            }
            found++;
        }
        assertEquals(13, found);

    }
}