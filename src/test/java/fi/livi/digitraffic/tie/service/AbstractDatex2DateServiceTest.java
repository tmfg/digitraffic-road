package fi.livi.digitraffic.tie.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.conf.jms.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.service.datex2.Datex2JsonConverterService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

@Import({ V2Datex2DataService.class, V2Datex2UpdateService.class, Datex2JsonConverterService.class, XmlMarshallerConfiguration.class, JacksonAutoConfiguration.class})
public abstract class AbstractDatex2DateServiceTest extends AbstractServiceTest {

    public final static String GUID_WITH_JSON = "GUID50001238";
    protected final static String GUID_NO_JSON = "GUID50001234";
    protected static final String FEATURE_1 = "Nopeusrajoitus";
    protected static final String FEATURE_2 = "Huono ajokeli";

    public enum ImsXmlVersion {
        V1_2_0,
        V1_2_1
    }

    public enum ImsJsonVersion {
        V0_2_4,
        V0_2_6,
        V0_2_8,
        V0_2_9,
    }

    protected static final String JSON_MESSAGE_PLACEHOLDER = "JSON_MESSAGE";
    protected static final String D2_MESSAGE_PLACEHOLDER = "D2_MESSAGE";

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller imsJaxb2Marshaller;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @Autowired
    private Datex2Repository datex2Repository;

    @Before
    public void cleanDb() {
        datex2Repository.deleteAll();
    }

    public void initDataFromFile(final ImsXmlVersion xmlVersion, final ImsJsonVersion jsonVersion) throws IOException {
        final String imsMessage = readImsMessageResourceContent(xmlVersion, jsonVersion);
        final ExternalIMSMessage ims = (ExternalIMSMessage) imsJaxb2Marshaller.unmarshal(new StringSource(imsMessage));
        v2Datex2UpdateService.updateTrafficDatex2ImsMessages(Collections.singletonList(ims));
    }

    protected void initDataFromFile(final String file) throws IOException {
        final ArrayList<String> xmlImsMessages = readResourceContents("classpath:tloik/ims/" + file);
        final ExternalIMSMessage ims = (ExternalIMSMessage) imsJaxb2Marshaller.unmarshal(new StringSource(xmlImsMessages.get(0)));
        v2Datex2UpdateService.updateTrafficDatex2ImsMessages(Collections.singletonList(ims));
    }

    public static String readImsMessageResourceContent(final ImsXmlVersion xmlVersion, final ImsJsonVersion jsonVersion) throws IOException {
        final String xmlImsMessage = readResourceContent("classpath:tloik/ims/ImsMessage" + xmlVersion + ".xml");
        final String jsonImsMessage = readResourceContent("classpath:tloik/ims/Json" + jsonVersion + ".json");
        final String datex2ImsMessage = readResourceContent("classpath:tloik/ims/d2Message.xml");
        // Insert datex2 and message contents
        return xmlImsMessage.replace(D2_MESSAGE_PLACEHOLDER, datex2ImsMessage).replace(JSON_MESSAGE_PLACEHOLDER, jsonImsMessage);
    }
}