package fi.livi.digitraffic.tie.data.controller.v3;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_SIMPLE_PATH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_1.ImsMessage;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2DetailedMessageType;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2DataService;


public class V3TrafficDatex2ControllerTest extends AbstractRestWebTest {

    @Autowired
    protected Datex2DataService datex2DataService;

    @Autowired
    protected V2Datex2UpdateService v2Datex2UpdateService;

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

    // Map active situations id:s with the type of the message
    private final List<Pair<Datex2DetailedMessageType, String>> activeMessageTypeSituationIds =
        List.of(
            Pair.of(Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT, "GUID00000001"),
            Pair.of(Datex2DetailedMessageType.PRELIMINARY_ANNOUNCEMENT, "GUID00000002"),
            Pair.of(Datex2DetailedMessageType.EXEMPTED_TRANSPORT, "GUID00000003"),
            Pair.of(Datex2DetailedMessageType.UNCONFIRMED_OBSERVATION, "GUID00000004"),
            Pair.of(Datex2DetailedMessageType.ROADWORK, "GUID00000005"),
            Pair.of(Datex2DetailedMessageType.WEIGHT_RESTRICTION, "GUID00000006"),
            Pair.of(Datex2DetailedMessageType.UNKNOWN, "GUID00000007")
        );

    // Map past situations id:s with the type of the message
    private final List<Pair<Datex2DetailedMessageType, String>> pastMessageTypeSituationIds =
        List.of(
            Pair.of(Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT, "GUID10000001"),
            Pair.of(Datex2DetailedMessageType.PRELIMINARY_ANNOUNCEMENT, "GUID10000002"),
            Pair.of(Datex2DetailedMessageType.EXEMPTED_TRANSPORT, "GUID10000003"),
            Pair.of(Datex2DetailedMessageType.UNCONFIRMED_OBSERVATION, "GUID10000004"),
            Pair.of(Datex2DetailedMessageType.ROADWORK, "GUID10000005"),
            Pair.of(Datex2DetailedMessageType.WEIGHT_RESTRICTION, "GUID10000006"),
            Pair.of(Datex2DetailedMessageType.UNKNOWN, "GUID10000007")
        );

    @Before
    public void initActiveAndPassiveDataFromImsMessages() throws IOException {
        datex2Repository.deleteAll();

        // One active situation of every type as mapped in activeMessageTypeSituationIds
        final String active = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessageV1_2_1JsonV0_2_6MultipleMessages.xml");

        // One past situation of every type as mapped in pastMessageTypeSituationIds
        final String ended = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessageV1_2_1JsonV0_2_6MultipleMessagesInactive.xml");

        updateFromImsMessage(active);
        updateFromImsMessage(ended);
    }

    /**
     * Tests that data return data is right type json vs xml.
     */
    @Test
    public void getJsonAndXmlCurrentlyActive() throws Exception {
        final String xml = getResponse(getUrlWithType(false, 0));
        final String json = getResponse(getUrlWithType(true, 0));
        assertIsValidDatex2Xml(xml);
        assertTextIsValidJson(json);
        assertTimesFormatMatches(xml);
        assertTimesFormatMatches(json);
    }

    /**
     * Tests that data return data is right type json vs xml.
     */
    @Test
    public void getJsonAndXmlBySituationId() throws Exception {
        final Pair<Datex2DetailedMessageType, String> situation = getRandomActiveSituation();
        final String xml = getResponse(getUrlWithSituationId(false, situation.getRight()));
        final String json = getResponse(getUrlWithSituationId(true, situation.getRight()));
        assertIsValidDatex2Xml(xml);
        assertTextIsValidJson(json);
    }

    /**
     * Tests that all active situations of all types are returned.
     */
    @Test
    public void getCurrentlyActive() throws Exception {
        final String xml = getResponse(getUrlWithType(false, 0));
        final String json = getResponse(getUrlWithType(true, 0));

        // Only now active incidents should exist
        final String[] activeIds = activeMessageTypeSituationIds.stream().map(Pair::getRight).toArray(String[]::new);
        final String[] pastIds = pastMessageTypeSituationIds.stream().map(Pair::getRight).toArray(String[]::new);
        assertTextExistInMessage(xml, activeIds);
        assertTextExistInMessage(json, activeIds);
        assertTextNotExistInMessage(xml, pastIds);
        assertTextNotExistInMessage(json, pastIds);
    }

    @Test
    public void getCurrentlyActiveAndActiveInPast() throws Exception {
        final String xml = getResponse(getUrlWithType(false, 200000));
        final String json = getResponse(getUrlWithType(true, 200000));

        // All active and past incidents should exist
        final String[] activeIds = activeMessageTypeSituationIds.stream().map(Pair::getRight).toArray(String[]::new);
        final String[] pastIds = pastMessageTypeSituationIds.stream().map(Pair::getRight).toArray(String[]::new);
        assertTextExistInMessage(xml, activeIds);
        assertTextExistInMessage(json, activeIds);
        assertTextExistInMessage(xml, pastIds);
        assertTextExistInMessage(json, pastIds);
    }

    @Test
    public void getCurrentlyActiveWithType() throws Exception {
        final Pair<Datex2DetailedMessageType, String> active = getRandomActiveSituation();
        final String xml = getResponse(getUrlWithType(false, 0, active.getLeft()));
        final String json = getResponse(getUrlWithType(true, 0, active.getLeft()));

        assertTextExistInMessage(xml, active.getRight());
        assertTextExistInMessage(json, active.getRight());
        assertTextNotExistInMessage(xml, json, getSituationsWithout(active));
    }

    @Test
    public void getCurrentlyActiveWithMultipleTypes() throws Exception {
        // Get all types in pairs of two types
        for(int i = 0; i < activeMessageTypeSituationIds.size()-1; i++) {
            final Pair<Datex2DetailedMessageType, String> first = activeMessageTypeSituationIds.get(i);
            final Pair<Datex2DetailedMessageType, String> second = activeMessageTypeSituationIds.get(i+1);
            final String xml = getResponse(getUrlWithType(false, 0, first.getLeft(), second.getLeft()));
            final String json = getResponse(getUrlWithType(true, 0, first.getLeft(), second.getLeft()));

            // Both active should exist
            assertTextExistInMessage(xml, first.getRight());
            assertTextExistInMessage(json, first.getRight());
            assertTextExistInMessage(xml, second.getRight());
            assertTextExistInMessage(json, second.getRight());

            //  Other active or past situation should not exist
            assertTextNotExistInMessage(xml, json, getSituationsWithout(first, second));
        }
    }

    @Test
    public void getBySituationId() throws Exception {
        final Pair<Datex2DetailedMessageType, String> situation = getRandomActiveSituation();
        final String xml = getResponse(getUrlWithSituationId(false, situation.getRight()));
        final String json = getResponse(getUrlWithSituationId(true, situation.getRight()));

        assertTextExistInMessage(xml, situation.getRight());
        assertTextExistInMessage(json, situation.getRight());
        assertTextNotExistInMessage(xml, json, getSituationsWithout(situation));
    }

    private Pair<Datex2DetailedMessageType, String> getRandomActiveSituation() {
        return activeMessageTypeSituationIds.get(getRandSituationIndex());
    }

    private List<Pair<Datex2DetailedMessageType, String>> getSituationsWithout(final Pair<Datex2DetailedMessageType, String>...notThese) {
        List<Pair<Datex2DetailedMessageType, String>> tmp = new ArrayList<>();
        tmp.addAll(activeMessageTypeSituationIds);
        tmp.addAll(pastMessageTypeSituationIds);
        for (final Pair<Datex2DetailedMessageType, String> not : notThese) {
            tmp.remove(not);
        }
        return tmp;
    }

    public int getRandSituationIndex() {
        return getRandom(0, 7);
    }

    private void assertIsValidDatex2Xml(final String xml) {
        try {
            datex2Jaxb2Marshaller.unmarshal(new StringSource(xml));
        } catch (XmlMappingException e) {
            throw new IllegalArgumentException("Not XML: " + xml, e);
        }
    }

    private void assertTextIsValidJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Not JSON: " + json, e);
        }
    }

    private static String getUrlWithType(final boolean json, final int inactiveHours, final Datex2DetailedMessageType...messageType) {
        final String[] types = V3Datex2DataService.typesAsStrings(messageType);
        final String params = String.join(",", types);
        return API_BETA_BASE_PATH + (json ? TRAFFIC_MESSAGES_SIMPLE_PATH : TRAFFIC_MESSAGES_DATEX2_PATH) + "?inactiveHours=" + inactiveHours + "&messageType=" + params;
    }

    private static String getUrlWithSituationId(final boolean json, final String situationId) {
        return API_BETA_BASE_PATH + (json ? TRAFFIC_MESSAGES_SIMPLE_PATH : TRAFFIC_MESSAGES_DATEX2_PATH) + "/" + situationId;
    }

    private void assertTextNotExistInMessage(final String xml, final String json, final List<Pair<Datex2DetailedMessageType, String>> messageTypeSituationIds) {
        for (final Pair<Datex2DetailedMessageType, String> not : messageTypeSituationIds) {
            assertTextNotExistInMessage(xml, not.getRight());
            assertTextNotExistInMessage(json, not.getRight());
        }

    }

    private void assertTextExistInMessage(final String xml, final String...text) {
        for (String t : text) {
            assertTextExistenceInMessage(t, true, xml);
        }
    }

    private void assertTextNotExistInMessage(final String xml, final String...text) {
        for (String t : text) {
            assertTextExistenceInMessage(t, false, xml);
        }
    }

    private void assertTextExistenceInMessage(final String text, final boolean shouldExist, final String message) {
        if (shouldExist) {
            assertTrue(text + " should exist in response", message.contains(text));
        } else {
            assertFalse(text + " should not exist in response", message.contains(text));
        }
    }

    private String getResponse(final String url) throws Exception {
        return mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString();
    }

    private void updateFromImsMessage(final String imsXml) {
        final ImsMessage ims = (fi.livi.digitraffic.tie.external.tloik.ims.v1_2_1.ImsMessage) imsJaxb2Marshaller.unmarshal(new StringSource(imsXml));
        v2Datex2UpdateService.updateTrafficDatex2ImsMessages(Collections.singletonList(ims));
    }
}
