package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.test.util.AssertUtil;
import fi.livi.digitraffic.tie.AbstractServiceTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static fi.livi.digitraffic.tie.TestUtils.loadResources;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

@Disabled
public class DatexII35XmlMarshallerTest extends AbstractServiceTest {
    private static final Logger log = getLogger(DatexII35XmlMarshallerTest.class);
    @Autowired
    private DatexII35XmlMarshaller datexII35XmlMarshaller;

    /**
     * Read DatexII messages from test resources
     *
     * @return list of pairs of filename and DatexII message content
     * @throws IOException if reading files fails
     */
    private List<Pair<String, String>> readDatexIIMessages() throws IOException {
        final List<Resource> resources = loadResources("classpath:/lotju/datex2/3.5/*.xml");
        log.info("Found {} DatexII messages", resources.size());
        AssertUtil.assertGe(resources.size(), 1);
        final List<Pair<String, String>> datexIIMessages = new ArrayList<>();
        for (final Resource resource : resources) {
            final String datexII = readFileToString(resource.getFile(), StandardCharsets.UTF_8);
            datexIIMessages.add(Pair.of(resource.getFilename(), datexII));
        }
        return datexIIMessages;
    }

    @Test
    public void testMarshall() throws IOException {
        for (final Pair<String, String> d2 : readDatexIIMessages()) {
            assertNotNull(datexII35XmlMarshaller.convertToObject(d2.getRight()),
                    "Failed to unmarshall DatexII message: " + d2.getLeft());
        }
    }
}

