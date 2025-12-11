package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.tie.AbstractServiceTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Disabled
public class ManualDatexII35XmlMarshallerTest extends AbstractServiceTest {
    @Autowired
    private DatexII35XmlMarshaller datexII35XmlMarshaller;

    private static final String TEST_XML = """
            INSERT XML HERE
            """;

    @Test
    public void testMarshall() {
        datexII35XmlMarshaller.convertToObject(TEST_XML);
    }
}

