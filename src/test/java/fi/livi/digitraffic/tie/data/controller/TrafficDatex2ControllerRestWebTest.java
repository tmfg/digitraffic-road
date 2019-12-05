package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.Datex2UpdateService;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2SimpleMessageUpdater;

public class TrafficDatex2ControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    protected Datex2DataService datex2DataService;

    @Autowired
    protected Datex2UpdateService datex2UpdateService;

    @Autowired
    protected Datex2Repository datex2Repository;

    @Autowired
    private Datex2SimpleMessageUpdater datex2SimpleMessageUpdater;

    private final String incident1_past = "GUID5000526801";
    private final String incident2_active = "GUID50006936";
    private final String incident3_active = "GUID50013339";
    private final String roadwork1_active = "GUID50350441";
    private final String weightRestriction1_active = "GUID5035473201";

    @Before
    public void updateData() throws IOException {
        datex2Repository.deleteAll();
        // GUID5000526801 in past
        final String incident1 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-09-12-20-51-24-602.xml");
        // GUID50006936 active
        final String incident2 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-11-17-18-34-36-299.xml");
        // GUID50013339 active
        final String incident3 = readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-15-59-34-896.xml");
        // GUID50350441 active
        final String roadwork1 = readResourceContent("classpath:lotju/roadwork/roadwork1.xml");
        // GUID5035473201 active
        final String weightRestriction1 = readResourceContent("classpath:lotju/weight_restrictions/wr1.xml");

        updateDatex2(incident1, Datex2MessageType.TRAFFIC_DISORDER);
        updateDatex2(incident2, Datex2MessageType.TRAFFIC_DISORDER);
        updateDatex2(incident3, Datex2MessageType.TRAFFIC_DISORDER);
        updateDatex2(roadwork1, Datex2MessageType.ROADWORK);
        updateDatex2(weightRestriction1, Datex2MessageType.WEIGHT_RESTRICTION);
    }


    @Test
    public void datex2incident() throws Exception {
        final String url = API_BETA_BASE_PATH + TRAFFIC_DATEX2_PATH + "/" + Datex2MessageType.TRAFFIC_DISORDER;
        final String xml = getResponse(url);
        assertSituation(incident1_past, false, xml);
        assertSituation(incident2_active, true, xml);
        assertSituation(incident3_active, true, xml);
        assertSituation(roadwork1_active, false, xml);
        assertSituation(weightRestriction1_active, false, xml);
    }

    @Test
    public void datex2incidentInPast() throws Exception {
        final String url = API_BETA_BASE_PATH + TRAFFIC_DATEX2_PATH + "/" + Datex2MessageType.TRAFFIC_DISORDER + "?inactiveHours=200000";
        final String xml = getResponse(url);
        assertSituation(incident1_past, true, xml);
        assertSituation(incident2_active, true, xml);
        assertSituation(incident3_active, true, xml);
        assertSituation(roadwork1_active, false, xml);
        assertSituation(weightRestriction1_active, false, xml);
    }

    @Test
    public void datex2roadwork() throws Exception {
        final String url = API_BETA_BASE_PATH + TRAFFIC_DATEX2_PATH + "/" + Datex2MessageType.ROADWORK;
        final String xml = getResponse(url);
        assertSituation(incident1_past, false, xml);
        assertSituation(incident2_active, false, xml);
        assertSituation(incident3_active, false, xml);
        assertSituation(roadwork1_active, true, xml);
        assertSituation(weightRestriction1_active, false, xml);
    }

    @Test
    public void datex2weightRestriction() throws Exception {
        final String url = API_BETA_BASE_PATH + TRAFFIC_DATEX2_PATH + "/" + Datex2MessageType.WEIGHT_RESTRICTION;
        final String xml = getResponse(url);
        assertSituation(incident1_past, false, xml);
        assertSituation(incident2_active, false, xml);
        assertSituation(incident3_active, false, xml);
        assertSituation(roadwork1_active, false, xml);
        assertSituation(weightRestriction1_active, true, xml);
    }

    private void assertSituation(final String situationId, final boolean shouldExist, final String xml) {
        if (shouldExist) {
            assertTrue(situationId + " should exist in response", xml.contains(situationId));
        } else {
            assertFalse(situationId + " should not exist in response", xml.contains(situationId));
        }
    }

    private String getResponse(final String url) throws Exception {
        return mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString();
    }

    private void updateDatex2(final String datex2Xml, final Datex2MessageType type) {
        datex2UpdateService.updateDatex2Data(datex2SimpleMessageUpdater.convert(datex2Xml, type, ZonedDateTime.now()), type);
    }
}
