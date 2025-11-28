package fi.livi.digitraffic.tie.service.trafficmessage;

import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.readImsMessageResourceContent;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;

public class Datex2DataInternalTest extends AbstractServiceTest {

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller imsJaxb2Marshaller;

    @Autowired
    private ImsUpdateService v2Datex2UpdateService;

    @Disabled("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void initTrafficMessagesEveryVersionOfImsAndJson() {
        IntStream.range(0,100).forEach(i -> {
            for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
                for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                    for (final SituationType situationType : SituationType.values()) {
                        final Instant start = TimeUtil.nowWithoutMillis().minus(1, ChronoUnit.HOURS);
                        final Instant end = start.plus(2, ChronoUnit.HOURS);
                        try {
                            trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }
/*
    @Disabled("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void importSimpleJsonWithMultipleRegionGeometries() throws IOException {
        final String jsonGuid = "GUID50379279";
        final String xmlImsMessage = readImsMessageResourceContent(ImsXmlVersion.V1_2_1);
        final String jsonImsMessage = readResourceContent("classpath:tloik/ims/internal/datex2_json_multiple_regions_geometries.json");
        final String datex2ImsMessage = readResourceContent("classpath:tloik/ims/d2Message.xml").replace("GUID50001238", jsonGuid);

        // Insert datex2 and message contents
        final String msg = xmlImsMessage.replace(D2_MESSAGE_PLACEHOLDER, datex2ImsMessage).replace(JSON_MESSAGE_PLACEHOLDER, jsonImsMessage);
        final ExternalIMSMessage ims = (ExternalIMSMessage) imsJaxb2Marshaller.unmarshal(new StringSource(msg));
        v2Datex2UpdateService.handleTrafficDatex2ImsMessages(Collections.singletonList(ims));
    }

    @Disabled("Just for internal testing to import given simple json and xml traffic message to db")
    @Rollback(value = false)
    @Test
    public void manualImportOfImsMessage() throws IOException {
        final String xmlImsMessage = readImsMessageResourceContent(ImsXmlVersion.V1_2_1);
        final String jsonImsMessage = readResourceContent("classpath:tloik/ims/internal/manualImportOfImsMessage-simple.json");
        final String datex2ImsMessage = readResourceContent("classpath:tloik/ims/internal/manualImportOfImsMessage-datex2.xml");

        // Insert datex2 and message contents
        final String msg = xmlImsMessage.replace(D2_MESSAGE_PLACEHOLDER, datex2ImsMessage).replace(JSON_MESSAGE_PLACEHOLDER, jsonImsMessage);
        final ExternalIMSMessage ims = (ExternalIMSMessage) imsJaxb2Marshaller.unmarshal(new StringSource(msg));
        v2Datex2UpdateService.handleTrafficDatex2ImsMessages(Collections.singletonList(ims));
    }*/
}
