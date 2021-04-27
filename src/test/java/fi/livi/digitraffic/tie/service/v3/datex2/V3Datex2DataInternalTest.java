package fi.livi.digitraffic.tie.service.v3.datex2;

import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.D2_MESSAGE_PLACEHOLDER;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.JSON_MESSAGE_PLACEHOLDER;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.readImsMessageResourceContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.jms.ExternalIMSMessage;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

@Import({V3RegionGeometryDataService.class})
public class V3Datex2DataInternalTest extends AbstractRestWebTest {
    private static final Logger log = getLogger(V3Datex2DataInternalTest.class);

    @Autowired
    private V3Datex2DataService v3Datex2DataService;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller imsJaxb2Marshaller;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @Ignore("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void initTrafficMessagesEveryVersionOfImsAndJson() {
        IntStream.range(0,100).forEach(i -> {
            for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
                for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                    for (final SituationType situationType : SituationType.values()) {
                        final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
                        final ZonedDateTime end = start.plusHours(2);
                        try {
                            trafficMessageTestHelper.initDataFromStaticImsResourceConent(imsXmlVersion, situationType, imsJsonVersion, start, end);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Ignore("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void importSimpleJsonWithMultipeRegionGeometries() throws IOException {
        final String jsonGuid = "GUID50379279";
        final String xmlImsMessage = readImsMessageResourceContent(ImsXmlVersion.V1_2_1);
        final String jsonImsMessage = readResourceContent("classpath:tloik/ims/internal/datex2_json_multiple_regions_geometries.json");
        final String datex2ImsMessage = readResourceContent("classpath:tloik/ims/d2Message.xml").replace("GUID50001238", jsonGuid);

        // Insert datex2 and message contents
        final String msg = xmlImsMessage.replace(D2_MESSAGE_PLACEHOLDER, datex2ImsMessage).replace(JSON_MESSAGE_PLACEHOLDER, jsonImsMessage);
        final ExternalIMSMessage ims = (ExternalIMSMessage) imsJaxb2Marshaller.unmarshal(new StringSource(msg));
        v2Datex2UpdateService.updateTrafficDatex2ImsMessages(Collections.singletonList(ims));
    }
}
