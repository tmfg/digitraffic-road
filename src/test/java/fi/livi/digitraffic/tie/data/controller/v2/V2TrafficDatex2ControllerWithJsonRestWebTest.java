package fi.livi.digitraffic.tie.data.controller.v2;

import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_0.ImsMessage;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;

public class V2TrafficDatex2ControllerWithJsonRestWebTest extends AbstractRestWebTest {

    @Autowired
    protected Datex2Repository datex2Repository;

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller imsJaxb2Marshaller;

    @Autowired
    @Qualifier("datex2Jaxb2Marshaller")
    private Jaxb2Marshaller datex2Jaxb2Marshaller;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    private final String incident1_past_id = "GUID50005166";
    private final String incident2_active_id = "GUID50006936";
    private final String incident3_active_id = "GUID50013339";
    private final String incident4_active_id_invalid_json_duration = "GUID60013339";
    private final String incident1_past_estimated_minimum = "PT6H";
    private final String incident2_active_estimated_minimum = "PT7H";
    private final String incident3_active_estimated_minimum = "PT8H";
    private final String incident3_active_estimated_maximum = "PT12H";

    @BeforeEach
    public void updateData() throws IOException {
        datex2Repository.deleteAll();
        // GUID5000526801 in past
        final String incident1 = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessage-2016-09-12-20-51-24-602.xml");
        // GUID50006936 active
        final String incident2 = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessage-2016-11-17-18-34-36-299.xml");
        // GUID50013339 active
        final String incident3 = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessage-2017-08-10-15-59-34-896.xml");
        // GUID60013339 active
        final String incident4 = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessage-invalid-duration.xml");

        updateFromImsMessage(incident1);
        updateFromImsMessage(incident2);
        updateFromImsMessage(incident3);
        updateFromImsMessage(incident4);
    }

    @Test
    public void datex2incident() throws Exception {
        final String xml = getResponse(getUrl(TRAFFIC_INCIDENT, false, 0));
        final String json = getResponse(getUrl(TRAFFIC_INCIDENT, true, 0));
        assertDatex2Xml(xml);
        assertJson(json);
        assertTextNotExistInMessage(incident1_past_id, xml);
        assertTextNotExistInMessage(incident1_past_id, json);
        assertTextExistInMessage(incident2_active_id, xml);
        assertTextExistInMessage(incident2_active_id, json);
        assertTextExistInMessage(incident3_active_id, xml);
        assertTextExistInMessage(incident3_active_id, json);

        assertTextExistInMessage(incident4_active_id_invalid_json_duration, xml);
        assertTextNotExistInMessage(incident4_active_id_invalid_json_duration, json);

        assertTextNotExistInMessage(incident1_past_estimated_minimum, json);
        assertTextExistInMessage(incident2_active_estimated_minimum, json);
        assertTextExistInMessage(incident3_active_estimated_minimum, json);
        assertTextExistInMessage(incident3_active_estimated_maximum, json);
        assertTimesFormatMatches(xml);
        assertTimesFormatMatches(json);
    }

    @Test
    public void datex2incidentInPast() throws Exception {
        final String xml = getResponse(getUrl(TRAFFIC_INCIDENT, false, 200000));
        final String json = getResponse(getUrl(TRAFFIC_INCIDENT, true, 200000));
        assertDatex2Xml(xml);
        assertJson(json);
        assertTextExistInMessage(incident1_past_id, xml);
        assertTextExistInMessage(incident1_past_id, json);
        assertTextExistInMessage(incident2_active_id, xml);
        assertTextExistInMessage(incident2_active_id, json);
        assertTextExistInMessage(incident3_active_id, xml);
        assertTextExistInMessage(incident3_active_id, json);
    }

    private void assertDatex2Xml(final String xml) {
        try {
            datex2Jaxb2Marshaller.unmarshal(new StringSource(xml));
        } catch (XmlMappingException e) {
            throw new IllegalArgumentException("Not XML: " + xml, e);
        }
    }

    private void assertJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Not JSON: " + json, e);
        }
    }

    private static String getUrl(final Datex2MessageType messageType, final boolean json, final int inactiveHours) {
        return API_V2_BASE_PATH + API_DATA_PART_PATH + TRAFFIC_DATEX2_PATH + "/" + messageType.toParameter() + (json ? ".json" : ".xml") + "?inactiveHours=" + inactiveHours;
    }

    private void assertTextExistInMessage(final String text, final String xml) {
        assertTextExistenceInMessage(text, true, xml);
    }

    private void assertTextNotExistInMessage(final String text, final String xml) {
        assertTextExistenceInMessage(text, false, xml);
    }

    private void assertTextExistenceInMessage(final String text, final boolean shouldExist, final String message) {
        if (shouldExist) {
            assertTrue(message.contains(text), text + " should exist in response");
        } else {
            assertFalse(message.contains(text), text + " should not exist in response");
        }
    }

    private String getResponse(final String url) throws Exception {
        return mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString();
    }

    private void updateFromImsMessage(final String imsXml) {
        final ImsMessage ims = (ImsMessage) imsJaxb2Marshaller.unmarshal(new StringSource(imsXml));
        trafficMessageTestHelper.getV2Datex2UpdateService().updateTrafficDatex2ImsMessages(Collections.singletonList(ims));
    }
}
