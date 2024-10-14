package fi.livi.digitraffic.tie.controller.maintenance;

import static fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingDao.STATE_ROADS_DOMAIN;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.ASFALTOINTI;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.PAALLYSTEIDEN_PAIKKAUS;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createMaintenanceTrackingWithLineString;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createWorkMachines;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.getTimeHoursInPast;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingWebDataServiceV1;

public class MaintenanceTrackingControllerV1CacheTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingControllerV1CacheTest.class);

    private final static Instant START_CACHED = getTimeHoursInPast(1);
    private final static Instant END_CACHED = START_CACHED.plus(1, ChronoUnit.MINUTES);

    private final static ArrayList<List<Double>> API_AREA_PARAMETERS = new ArrayList<>();
    static {
        API_AREA_PARAMETERS.add(Arrays.asList(19.0, 32.0, 59.5, 69.5)); // xMin, xMax, yMin, yMax
        API_AREA_PARAMETERS.add(Arrays.asList(19.9, 31.1, 59.9, 69.4));
        API_AREA_PARAMETERS.add(Arrays.asList(19.1, 31.9, 59.1, 69.1));
    }
    private final static double API_AREA_PARAMETERS_X_MIN_OUTSIDE_ROUTE = 30.0;

    // All area coordinates should be normalized to same area
    final Polygon SERVICE_EXPECTED_AREA_PARAMETER =
            PostgisGeometryUtils.createSquarePolygonFromMinMax(19, 32, 59.5, 69.5);
    final Polygon SERVICE_EXPECTED_NO_CACHED_AREA_PARAMETER =
            PostgisGeometryUtils.createSquarePolygonFromMinMax(API_AREA_PARAMETERS_X_MIN_OUTSIDE_ROUTE, 32, 59.5, 69.5);

    // Order of task parameters should not have affect to caching
    private final static List<List<MaintenanceTrackingTask>> API_TASK_PARAMETERS = Arrays.asList(
            Arrays.asList(MaintenanceTrackingTask.PAVING, MaintenanceTrackingTask.PATCHING),
            Arrays.asList(MaintenanceTrackingTask.PATCHING, MaintenanceTrackingTask.PAVING),
            Arrays.asList(MaintenanceTrackingTask.PATCHING, MaintenanceTrackingTask.PAVING));
    private final static Set<MaintenanceTrackingTask>
            SERVICE_EXPECTED_TASK_PARAMETER = Set.of(MaintenanceTrackingTask.PAVING, MaintenanceTrackingTask.PATCHING);

    private final static  List<List<String>> API_DOMAIN_PARAMETERS = Arrays.asList(
            List.of(),
            List.of(STATE_ROADS_DOMAIN),
            List.of(STATE_ROADS_DOMAIN));
    private final static  Set<String> SERVICE_EXPECTED_DOMAIN_PARAMETER = Set.of(STATE_ROADS_DOMAIN);

    @Autowired
    private MaintenanceTrackingServiceTestHelperV1 testHelper;

    @SpyBean
    private MaintenanceTrackingWebDataServiceV1 maintenanceTrackingWebDataServiceV1;

    @BeforeEach
    public void initData() throws JsonProcessingException {
        testHelper.clearDb();
        final List<Tyokone> workMachines = createWorkMachines(1);

        // Create observation with 1000 points
        testHelper.saveTrackingDataAsObservations( // end time will be same for LineString
                createMaintenanceTrackingWithLineString(START_CACHED, 1000, 1, workMachines, ASFALTOINTI, PAALLYSTEIDEN_PAIKKAUS));
        testHelper.handleUnhandledWorkMachineObservations(1000);
    }


    @Test
    public void findMaintenanceTracking() throws Exception {
        int index = 0;
        String prevResult = null;

        for (final List<Double> areaCoordinates : API_AREA_PARAMETERS) {
            final String result =
                expectOkFeatureCollectionWithSize(
                        getTrackingsJson(
                                API_DOMAIN_PARAMETERS.get(index),
                                API_TASK_PARAMETERS.get(index), // check that task order don't mess the cache
                                areaCoordinates.get(0), // xMin
                                areaCoordinates.get(1), // xMax
                                areaCoordinates.get(2), // yMin
                                areaCoordinates.get(3)  // yMax
                        ),
                        1
                ).andReturn().getResponse().getContentAsString();
            if (prevResult != null) {
                Assertions.assertEquals(prevResult, result);
            }
            prevResult = result;
            index++;
        }

        expectOkFeatureCollectionWithSize(
                getTrackingsJson(
                        API_DOMAIN_PARAMETERS.get(0),
                        API_TASK_PARAMETERS.get(0), // check that task order don't mess the cache
                        API_AREA_PARAMETERS_X_MIN_OUTSIDE_ROUTE, // xMin diff from cached one
                        API_AREA_PARAMETERS.get(0).get(1), // xMax
                        API_AREA_PARAMETERS.get(0).get(2), // yMin
                        API_AREA_PARAMETERS.get(0).get(3)  // yMax
                ),
                0
        );
        // All queries should match same cache key and only once it should be executed
        verify(maintenanceTrackingWebDataServiceV1, times(1)).findMaintenanceTrackingRoutes(
                START_CACHED, END_CACHED, null, null, SERVICE_EXPECTED_AREA_PARAMETER, SERVICE_EXPECTED_TASK_PARAMETER, SERVICE_EXPECTED_DOMAIN_PARAMETER
        );
        // All queries should match same cache key and only once it should be executed
        verify(maintenanceTrackingWebDataServiceV1, times(1)).findMaintenanceTrackingRoutes(
                START_CACHED, END_CACHED, null, null, SERVICE_EXPECTED_NO_CACHED_AREA_PARAMETER, SERVICE_EXPECTED_TASK_PARAMETER, SERVICE_EXPECTED_DOMAIN_PARAMETER
        );
    }

    @Test
    public void findLatestMaintenanceTrackings() throws Exception {
        int index = 0;
        String prevResult = null;

        for (final List<Double> areaCoordinates : API_AREA_PARAMETERS) {
            final String result =
                    expectOkFeatureCollectionWithSize(
                        getLatestTrackingsJson(
                                API_DOMAIN_PARAMETERS.get(index),
                                API_TASK_PARAMETERS.get(index), // check that task order don't mess the cache
                                areaCoordinates.get(0), // xMin
                                areaCoordinates.get(1), // xMax
                                areaCoordinates.get(2), // yMin
                                areaCoordinates.get(3)  // yMax
                        ),
                        1
                ).andReturn().getResponse().getContentAsString();
            if (prevResult != null) {
                Assertions.assertEquals(prevResult, result);
            }
            prevResult = result;
            index++;
        }

        expectOkFeatureCollectionWithSize(
                getLatestTrackingsJson(
                        API_DOMAIN_PARAMETERS.get(0),
                        API_TASK_PARAMETERS.get(0),
                        API_AREA_PARAMETERS_X_MIN_OUTSIDE_ROUTE, // xMin diff from cached one
                        API_AREA_PARAMETERS.get(0).get(1), // xMax
                        API_AREA_PARAMETERS.get(0).get(2), // yMin
                        API_AREA_PARAMETERS.get(0).get(3)  // yMax
                ),
                0
        );

        // All queries should match same cache key and only once it should be executed
        verify(maintenanceTrackingWebDataServiceV1, times(1)).findLatestMaintenanceTrackingRoutes(
                START_CACHED, null, SERVICE_EXPECTED_AREA_PARAMETER, SERVICE_EXPECTED_TASK_PARAMETER, SERVICE_EXPECTED_DOMAIN_PARAMETER
        );
        verify(maintenanceTrackingWebDataServiceV1, times(1)).findLatestMaintenanceTrackingRoutes(
                START_CACHED, null, SERVICE_EXPECTED_NO_CACHED_AREA_PARAMETER, SERVICE_EXPECTED_TASK_PARAMETER, SERVICE_EXPECTED_DOMAIN_PARAMETER
        );
    }

    private ResultActions getTrackingsJson(final List<String> domains, final List<MaintenanceTrackingTask> tasks,
                                           final double xMin, final double xMax, final double yMin, final double yMax) throws Exception {
        final String tasksParams = tasks.stream().map(t -> "&taskId=" + t.toString()).collect(Collectors.joining());
        final String domainParams = domains.stream().map(t -> "&domain=" + t).collect(Collectors.joining());
        final String url = MaintenanceTrackingControllerV1.API_MAINTENANCE_V1_TRACKING_ROUTES +
                String.format(Locale.US, "?endFrom=%s&endBefore=%s&xMin=%f&yMin=%f&xMax=%f&yMax=%f%s%s",
                        MaintenanceTrackingControllerV1CacheTest.START_CACHED, MaintenanceTrackingControllerV1CacheTest.END_CACHED, xMin, yMin, xMax, yMax, tasksParams, domainParams);
        log.info("Get URL: {}", url);
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);
        get.contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(get);
    }

    private ResultActions getLatestTrackingsJson(final List<String> domains, final List<MaintenanceTrackingTask> tasks,
                                                 final double xMin, final double xMax, final double yMin, final double yMax) throws Exception {
        final String tasksParams = tasks.stream().map(t -> "&taskId=" + t.toString()).collect(Collectors.joining());
        final String domainParams = domains.stream().map(t -> "&domain=" + t).collect(Collectors.joining());
        final String url = MaintenanceTrackingControllerV1.API_MAINTENANCE_V1_TRACKING_ROUTES_LATEST +
                String.format(Locale.US, "?endFrom=%s&xMin=%f&yMin=%f&xMax=%f&yMax=%f%s%s",
                        MaintenanceTrackingControllerV1CacheTest.START_CACHED.toString(), xMin, yMin, xMax, yMax, tasksParams, domainParams);
        log.info("Get URL: {}", url);
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);
        get.contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(get);
    }
}
