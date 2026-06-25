package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.API_TRAFFIC_MESSAGE_V2;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.DATEX2_3_5;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.HISTORY;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.TRAFFIC_DATA;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.ResponseAsserter;
import fi.livi.digitraffic.XmlAsserter;
import fi.livi.digitraffic.tie.AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock;

@TestPropertySource(properties = "dt.trafficMessage.rtti.enabled=false")
public class TrafficMessageControllerV2RttiDisabledTest extends AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock {


    private void insertTrafficData() {
        entityManager.createNativeQuery("""
                insert into datex2_rtti(situation_id,type,publication_time,geometry,start_time,is_srti,message)
                values('id1', 'PLACEHOLDER', now(), 'POINT(10 10)', now(), true, 'MSG')
                """).executeUpdate();
    }

    /**
     * When RTTI is disabled the list endpoint must return HTTP 200 with an empty SituationPublication
     * (no {@code <sit:situation>} elements) — even when data exists in the database.
     */
    @Test
    public void trafficData35_rttiDisabled_returnsEmptyPublication() throws Exception {
        insertTrafficData();

        final var response = mockMvc
                .perform(MockMvcRequestBuilders
                        .get(API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + DATEX2_3_5)
                        .contentType(MediaType.APPLICATION_XML))
                .andReturn().getResponse();

        XmlAsserter.ok(response).expectContent(xmlNode -> {
            Assertions.assertEquals("sit:SituationPublication", xmlNode.get("type").asString());
            // No situations must be present
            Assertions.assertNull(xmlNode.get("situation"),
                    "Expected no situations when RTTI is disabled");
        });
    }

    /**
     * When RTTI is disabled the per-situationId endpoint must return HTTP 404
     * — even when data exists in the database.
     */
    @Test
    public void trafficDataById35_rttiDisabled_returns404() throws Exception {
        insertTrafficData();

        final var response = mockMvc
                .perform(MockMvcRequestBuilders
                        .get(API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/id1" + DATEX2_3_5)
                        .contentType(MediaType.APPLICATION_XML))
                .andReturn().getResponse();

        ResponseAsserter.notFound(response).run();
    }

    /**
     * When RTTI is disabled the history endpoint must also return HTTP 404.
     */
    @Test
    public void trafficDataHistory35_rttiDisabled_returns404() throws Exception {
        insertTrafficData();

        final var response = mockMvc
                .perform(MockMvcRequestBuilders
                        .get(API_TRAFFIC_MESSAGE_V2 + TRAFFIC_DATA + "/id1" + HISTORY + DATEX2_3_5)
                        .contentType(MediaType.APPLICATION_XML))
                .andReturn().getResponse();

        ResponseAsserter.notFound(response).run();
    }
}

