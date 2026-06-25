package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.test.util.AssertUtil;
import fi.livi.digitraffic.tie.AbstractServiceTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fi.livi.digitraffic.tie.TestUtils.loadResources;
import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Tests for {@link DatexII37XmlMarshaller}.
 * <p>
 * DatexII 3.7 uses the same XML namespaces as 3.5 (http://datex2.eu/schema/3/...),
 * so the same test XML resources from 3.5 can be used here.
 */
public class DatexII37XmlMarshallerTest extends AbstractServiceTest {
    private static final Logger log = getLogger(DatexII37XmlMarshallerTest.class);

    // match values in tags ending with "Time"; [^<]+ matches across newlines (character classes match \n by default)
    private static final Pattern TIMES_PATTERN = Pattern.compile("<[^>]*Time>([^<]+)</");
    // Match Zulu datetime (with optional milliseconds)
    private static final Pattern ISO_DATETIME_ZULU_PATTERN = Pattern.compile(
            "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])T([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(\\.\\d+)?Z");

    @Autowired
    private DatexII37XmlMarshaller datexII37XmlMarshaller;

    /**
     * Read DatexII 3.5/3.7 messages from test resources.
     * DatexII 3.7 uses the same XML format as 3.5, so 3.5 test resources are reused.
     *
     * @return list of pairs of filename and DatexII message content
     * @throws IOException if reading files fails
     */
    private List<Pair<String, String>> readDatexIIMessages() throws IOException {
        final List<Resource> resources = loadResources("classpath:/lotju/datex2/3.5/*.xml");
        log.info("Found {} DatexII 3.7-compatible messages", resources.size());
        AssertUtil.assertGe(resources.size(), 1);
        final List<Pair<String, String>> datexIIMessages = new ArrayList<>();
        for (final Resource resource : resources) {
            final String datexII = readFileToString(resource.getFile(), StandardCharsets.UTF_8);
            datexIIMessages.add(Pair.of(resource.getFilename(), datexII));
        }
        return datexIIMessages;
    }

    /**
     * All DatexII 3.7 messages can be unmarshalled without error.
     */
    @Test
    public void testMarshall() throws IOException {
        for (final Pair<String, String> d2 : readDatexIIMessages()) {
            assertNotNull(datexII37XmlMarshaller.convertToObject(null, d2.getRight()),
                    "Failed to unmarshall DatexII 3.7 message: " + d2.getLeft());
        }
    }

    /**
     * All datetime values in the DatexII 3.7 source XML are in UTC/Zulu format,
     * and the round-trip (unmarshal → marshal) preserves them as Zulu.
     * Uses GUID50456943.xml which has 6 time fields in the source XML.
     * The marshalled output has 5 time fields (situationRecordFirstSupplierVersionTime is optional and omitted on re-marshal).
     */
    @Test
    public void allDatesInUtc() throws IOException {
        final String fromXml = readResourceContent("classpath:lotju/datex2/3.5/GUID50456943.xml");
        final var object = datexII37XmlMarshaller.convertToObject(null, fromXml);
        final String toXml = datexII37XmlMarshaller.convertToString(object);

        log.info("Check source xml already has Zulu times");
        checkAllDatesAreZulu(fromXml, 6);
        log.info("Check marshalled xml still has Zulu times");
        checkAllDatesAreZulu(toXml, 5); // situationRecordFirstSupplierVersionTime (minOccurs=0) is not emitted on re-marshal
    }

    private void checkAllDatesAreZulu(final String xml, final int expectedCount) {
        final Matcher timesMatcher = TIMES_PATTERN.matcher(xml);
        int found = 0;
        while (timesMatcher.find()) {
            final String dateTime = timesMatcher.group(1).trim();
            log.info("Found dateTime: {}", dateTime);
            assertTrue(ISO_DATETIME_ZULU_PATTERN.matcher(dateTime).matches(),
                    "DateTime is not in UTC/Zulu format: " + dateTime);
            found++;
        }
        assertEquals(expectedCount, found,
                "Expected " + expectedCount + " time fields but found " + found);
    }
}

