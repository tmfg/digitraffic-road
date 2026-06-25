package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.tie.AbstractServiceTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Disabled
public class ManualDatexII37XmlMarshallerTest extends AbstractServiceTest {
    @Autowired
    private DatexII37XmlMarshaller datexII37XmlMarshaller;

    private static final String TEST_XML = """
            INSERT XML HERE
            """;

    @Test
    public void testMarshall() {
        datexII37XmlMarshaller.convertToObject(null, TEST_XML);
    }
}

