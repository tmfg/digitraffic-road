package fi.livi.digitraffic.tie.data.controller.v2;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.WEIGHT_RESTRICTION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.datex2.Datex2Helper;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2DataService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

public class V2TrafficDatex2ControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    protected Datex2DataService datex2DataService;
    @Autowired
    protected Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;
    @Autowired
    protected Datex2Repository datex2Repository;
    @Autowired
    private GenericApplicationContext applicationContext;

    private final String incident1_past_id = "GUID50005166";
    private final String incident2_active_id = "GUID50006936";
    private final String incident3_active_id = "GUID50013339";
    private final String roadwork1_active_id = "GUID50350441";
    private final String weightRestriction1_active_id = "GUID5035473201";

    @Before
    public void updateData() throws IOException {
        final V2Datex2UpdateService datex2UpdateService =
            applicationContext.getAutowireCapableBeanFactory().createBean(V2Datex2UpdateService.class);

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

        datex2UpdateService.updateDatex2Data(Collections.singletonList(createDatex2MessageDto(
            incident1, "", SituationType.TRAFFIC_ANNOUNCEMENT, TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT)));
        datex2UpdateService.updateDatex2Data(Collections.singletonList(createDatex2MessageDto(
            incident2, "", SituationType.TRAFFIC_ANNOUNCEMENT, TrafficAnnouncementType.ACCIDENT_REPORT)));
        datex2UpdateService.updateDatex2Data(Collections.singletonList(createDatex2MessageDto(
            incident3, "", SituationType.TRAFFIC_ANNOUNCEMENT, TrafficAnnouncementType.ACCIDENT_REPORT)));
        datex2UpdateService.updateDatex2Data(Collections.singletonList(createDatex2MessageDto(
            roadwork1, "", SituationType.ROAD_WORK, null)));
        datex2UpdateService.updateDatex2Data(Collections.singletonList(createDatex2MessageDto(
            weightRestriction1, "", SituationType.WEIGHT_RESTRICTION, null)));
    }

    private Datex2MessageDto createDatex2MessageDto(final String datexMessage, final String jsonMessage, final SituationType situationType, final TrafficAnnouncementType trafficAnnouncementType) {
        final D2LogicalModel d2LogicalModel =
            datex2XmlStringToObjectMarshaller.convertToObject(datexMessage);
        final SituationPublication s = Datex2Helper.getSituationPublication(d2LogicalModel);
        return new Datex2MessageDto(d2LogicalModel,
                             situationType,
                             trafficAnnouncementType,
                             datexMessage,
                             jsonMessage,
                             ZonedDateTime.now(),
                             s.getSituations().get(0).getId());
    }

    @Test
    public void datex2incident() throws Exception {
        final String url = getUrl(TRAFFIC_INCIDENT, false, 0);
        final String xml = getResponse(url);
        assertSituationNotExistInXml(incident1_past_id, xml);
        assertSituationExistInXml(incident2_active_id, xml);
        assertSituationExistInXml(incident3_active_id, xml);
        assertSituationNotExistInXml(roadwork1_active_id, xml);
        assertSituationNotExistInXml(weightRestriction1_active_id, xml);
        assertTimesFormatMatches(xml);
    }

    @Test
    public void datex2incidentInPast() throws Exception {
        final String url = getUrl(TRAFFIC_INCIDENT, false, 200000);
        final String xml = getResponse(url);
        assertSituationExistInXml(incident1_past_id, xml);
        assertSituationExistInXml(incident2_active_id, xml);
        assertSituationExistInXml(incident3_active_id, xml);
        assertSituationNotExistInXml(roadwork1_active_id, xml);
        assertSituationNotExistInXml(weightRestriction1_active_id, xml);
    }

    @Test
    public void datex2roadwork() throws Exception {
        final String url = getUrl(ROADWORK, false, 0);
        final String xml = getResponse(url);
        assertSituationNotExistInXml(incident1_past_id, xml);
        assertSituationNotExistInXml(incident2_active_id, xml);
        assertSituationNotExistInXml(incident3_active_id, xml);
        assertSituationExistInXml(roadwork1_active_id, xml);
        assertSituationNotExistInXml(weightRestriction1_active_id, xml);
        assertTimesFormatMatches(xml);
    }

    @Test
    public void datex2weightRestriction() throws Exception {
        final String url = getUrl(WEIGHT_RESTRICTION, false, 0);
        final String xml = getResponse(url);
        assertSituationNotExistInXml(incident1_past_id, xml);
        assertSituationNotExistInXml(incident2_active_id, xml);
        assertSituationNotExistInXml(incident3_active_id, xml);
        assertSituationNotExistInXml(roadwork1_active_id, xml);
        assertSituationExistInXml(weightRestriction1_active_id, xml);
        assertTimesFormatMatches(xml);
    }

    private static String getUrl(final Datex2MessageType messageType, final boolean json, final int inactiveHours) {
        return API_V2_BASE_PATH + API_DATA_PART_PATH + TRAFFIC_DATEX2_PATH + "/" + messageType.toParameter() + (json ? ".json" : ".xml") + "?inactiveHours=" + inactiveHours;
    }

    private void assertSituationExistInXml(final String situationId,  final String xml) {
        assertSituationExistenceInXml(situationId, true, xml);
    }

    private void assertSituationNotExistInXml(final String situationId,  final String xml) {
        assertSituationExistenceInXml(situationId, false, xml);
    }

    private void assertSituationExistenceInXml(final String situationId, final boolean shouldExist, final String xml) {
        if (shouldExist) {
            assertTrue(situationId + " should exist in response", xml.contains(situationId));
        } else {
            assertFalse(situationId + " should not exist in response", xml.contains(situationId));
        }
    }

    private String getResponse(final String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }
}
