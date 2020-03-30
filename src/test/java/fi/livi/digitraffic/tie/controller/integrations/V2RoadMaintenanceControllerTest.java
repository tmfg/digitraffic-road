package fi.livi.digitraffic.tie.controller.integrations;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_INTEGRATIONS_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_WORK_MACHINE_PART_PATH;
import static fi.livi.digitraffic.tie.controller.integrations.V2RoadMaintenanceController.REALIZATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.integrations.V2RoadMaintenanceController.TRACKINGS_PATH;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingData;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class V2RoadMaintenanceControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(V2RoadMaintenanceControllerTest.class);

    @Autowired
    private V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository;
    @Autowired
    private V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    @Autowired
    private V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;

    @Before
    public void cleanDb() {
        v2MaintenanceTrackingRepository.deleteAll();
        if (TestTransaction.isActive()) {
            TestTransaction.flagForCommit();
            TestTransaction.end();
        }
        TestTransaction.start();
    }


    @Test
    public void postWorkMachineTrackingDataOk() throws Exception {

        final int recordsBefore = v2MaintenanceTrackingDataRepository.findAll().size();

        postTrackingJson("pisteseuranta.json");

        final int recordsAfter = v2MaintenanceTrackingDataRepository.findAll().size();
        Assert.assertEquals(recordsBefore+1, recordsAfter);
    }

    @Test
    public void postWorkMachineTrackingLineStringDataOk() throws Exception {

        final int recordsBefore = v2MaintenanceTrackingDataRepository.findAll().size();

        postTrackingJson("viivageometriaseuranta.json");

        final List<MaintenanceTrackingData> all = v2MaintenanceTrackingDataRepository.findAll();
        Assert.assertEquals(recordsBefore+1, all.size());
    }


    @Test
    public void postWorkMachineTrackingDataWithNoContentType() throws Exception {

        final int recordsBefore = v2MaintenanceTrackingRepository.findAll().size();

        postTracking("pisteseuranta.json", null, status().is5xxServerError());

        final int recordsAfter = v2MaintenanceTrackingRepository.findAll().size();
        Assert.assertEquals(recordsBefore, recordsAfter);
    }

    @Ignore("DPO-631 Temporally disabled the check of time gap between points to see what is real data quality")
    @Test
    public void postWorkMachineTrackingDataAndHandleAsDistinctObservations() throws Exception {
        final long harjaUrakkaId = 999999;
        final long harjaTyokoneId = 1111111111;

        postTrackingJson("linestring_tracking_1.json");
        postTrackingJson("linestring_tracking_2.json");
        postTrackingJson("linestring_tracking_3.json");


        v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
        entityManager.flush();
        entityManager.clear();

        final List<MaintenanceTracking> observations = v2MaintenanceTrackingRepository
            .findAllByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByModifiedAscIdAsc(harjaTyokoneId, harjaUrakkaId);

        Assert.assertEquals("Observations should be divided in two as there is over 30 min gap in observations",
                   2, observations.size());

        final MaintenanceTracking first = observations.get(0);
        final MaintenanceTracking second = observations.get(1);

        Assert.assertEquals("First observation should have coordinates from 2 first messages = 10",
            10, first.getLineString().getNumPoints());

        Assert.assertEquals("Second observation should have coordinates from 3 message = 5",
            5, second.getLineString().getNumPoints());

        Assert.assertEquals(2, first.getTasks().size());
        Assert.assertTrue(first.getTasks().contains(PLOUGHING_AND_SLUSH_REMOVAL));
        Assert.assertTrue(first.getTasks().contains(SALTING));

        Assert.assertEquals(1, second.getTasks().size());
        Assert.assertTrue(second.getTasks().contains(SALTING));


    }

    @Test
    public void postSingleRealization() throws Exception {
        postRealization("toteumakirjaus-yksi-reittitoteuma.json", status().isOk());
    }

    @Test
    public void postMultipleRealization() throws Exception {
        postRealization("toteumakirjaus-monta-reittitoteumaa.json", status().isOk());
    }

    private void postTrackingJson(final String fileName) throws Exception {
        postTracking(fileName, MediaType.APPLICATION_JSON, status().isOk());
    }

    private void postTracking(final String fileName, final MediaType mediaType, final ResultMatcher expectResult) throws Exception {
        postData(fileName, mediaType, TRACKINGS_PATH, expectResult);
    }

    private void postRealization(final String fileName, final ResultMatcher expectResult) throws Exception {
        postData(fileName, MediaType.APPLICATION_JSON, REALIZATIONS_PATH + "/123", expectResult);
    }

    private void postData(final String fileName, final MediaType mediaType, final String path, final ResultMatcher expectResult) throws Exception {
        final String jsonContent = readResourceContent("classpath:harja/controller/" + fileName);

        log.info("POST: {}", API_INTEGRATIONS_BASE_PATH + API_WORK_MACHINE_PART_PATH + "/v2" + path);
        final MockHttpServletRequestBuilder post = post(API_INTEGRATIONS_BASE_PATH + API_WORK_MACHINE_PART_PATH + "/v2" + path)
            .content(jsonContent);
        if (mediaType != null) {
            post.contentType(mediaType);
        }

        mockMvc.perform(post).andExpect(expectResult);
    }

    //    @Test
    public void reconvertKoordinatesToETRS89() {
        List<List<Double>> list1 = Arrays.asList(
            Arrays.asList(23.043001740682634, 62.077040093972144),
            Arrays.asList(23.04298031157012, 62.077057456787955),
            Arrays.asList(23.042977977405062, 62.07707536693498),
            Arrays.asList(23.04300057360766, 62.077049049045925),
            Arrays.asList(23.042936286174413, 62.077101137481954));

        List<List<Double>> list2 = Arrays.asList(
            Arrays.asList(23.04283614273957, 62.07713422104009),
            Arrays.asList(23.042776523433194, 62.07715048911074),
            Arrays.asList(23.0424745049959, 62.07711486578356),
            Arrays.asList(23.04269897620993, 62.07715725471351),
            Arrays.asList(23.04262376333742, 62.07714611013338));

        List<List<Double>> list3 = Arrays.asList(
            Arrays.asList(23.04241955454114, 62.077095313427435),
            Arrays.asList(23.042364604157456, 62.07707576104993),
            Arrays.asList(23.042331083335966, 62.07703884594243),
            Arrays.asList(23.04225545265861, 62.07688387267941),
            Arrays.asList(23.04226087078776, 62.07669526886046)
        );

        System.out.println("1:");
        System.out.println(CoordinateConverter.convertLineStringFromWGS84ToETRS89(list1));
        System.out.println("2:");
        System.out.println(CoordinateConverter.convertLineStringFromWGS84ToETRS89(list2));
        System.out.println("3:");
        System.out.println(CoordinateConverter.convertLineStringFromWGS84ToETRS89(list3));
    }

}
