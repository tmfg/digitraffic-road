package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineObservation;
import fi.livi.digitraffic.tie.data.model.maintenance.harja.WorkMachineTrackingDto;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;

public class MaintenanceDataServiceTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataServiceTest.class);

    @Autowired
    private MaintenanceDataService maintenanceDataService;

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectReader reader;

    @Before
    public void initObjectReader() {
        reader = objectMapper.readerFor(TyokoneenseurannanKirjausRequestSchema.class);
    }

    @Test
    public void findAllNotHandledWorkMachineTrackings() throws IOException {
        readTrackingJsonAndSave("timegap", 3);
        List<WorkMachineTrackingDto> all =
            maintenanceDataService.findAllNotHandledWorkMachineTrackingsOldestFirst();
        log.info("all: {}", all);
    }

    @Test
    public void findWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId() throws IOException {
        readTrackingJsonAndSave("timegap", 3 );
        List<WorkMachineObservation> all =
            maintenanceDataService.findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(1L, 1L);
        log.info("all: {}", all);
    }

    @Test
    public void findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId() throws IOException {
        readTrackingJsonAndSave("timegap", 3);
        WorkMachineObservation found =
            maintenanceDataService.findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(1L, 1L);
        log.info("found: {}", found);
    }


    @Test
    public void handleUnhandledWorkMachineTrakkings() throws JsonProcessingException {
        log.info("handleUnhandledWorkMachineTrackings count {}",
                 maintenanceDataService.handleUnhandledWorkMachineTrackings(null));
    }

    /**
     * Tests that trackings with gap between observation time is over limit will be divided to distinct observations
     */
    @Test
    public void convertUnhandledWorkMachineTrackingsToObservationsWithTimeGap() throws IOException {

        readTrackingJsonAndSave("timegap", 3);

        Map<Pair<Integer, Integer>, List<MaintenanceDataService.ObservationFeatureWrapper>> unhandledMap =
            maintenanceDataService.findUnhandledTrakkingsOldestFirstMappedByHarjaWorkMachineAndContract(100);

        convertWorkMachineTrackingsToObservationsAndSave(unhandledMap);

        // Must be flushed to get coordinates read
        entityManager.flush();
        entityManager.clear();

        List<WorkMachineObservation> obs =
            maintenanceDataService.findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(1234, 999999);

        // 3. observation has over 30 min gap from 2. observation -> should be divided to two distinct observations
        Assert.assertEquals(2, obs.size());

        final WorkMachineObservation first = obs.get(0);
        final WorkMachineObservation second = obs.get(1);

        Assert.assertEquals(10, first.getCoordinates().size());
        Assert.assertEquals(5, second.getCoordinates().size());
    }

    /**
     * Tests for trackings with and without task will be divided to distinct observations
     */
    @Test
    public void convertUnhandledWorkMachineTrackingsToObservationsWithMissingTask() throws IOException {

        readTrackingJsonAndSave("missingtask", 5);

        Map<Pair<Integer, Integer>, List<MaintenanceDataService.ObservationFeatureWrapper>> unhandledMap =
            maintenanceDataService.findUnhandledTrakkingsOldestFirstMappedByHarjaWorkMachineAndContract(100);

        convertWorkMachineTrackingsToObservationsAndSave(unhandledMap);

        // Must be flushed to get coordinates read
        entityManager.flush();
        entityManager.clear();

        List<WorkMachineObservation> obs =
            maintenanceDataService.findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(12345, 999999);

        // Observation 3/5 has empty task -> 1+2, 3, 3+4 should be result observations, 3 as transition
        Assert.assertEquals(3, obs.size());

        final WorkMachineObservation first = obs.get(0);
        final WorkMachineObservation transition = obs.get(1);
        final WorkMachineObservation second = obs.get(2);

        Assert.assertEquals(10, first.getCoordinates().size());
        Assert.assertEquals(5, transition.getCoordinates().size());
        Assert.assertEquals(9, second.getCoordinates().size());

        Assert.assertEquals(false, first.isTransition());
        Assert.assertEquals(true, transition.isTransition());
        Assert.assertEquals(false, second.isTransition());

    }

    /**
     * For development
     */
    @Rollback(false)
//    @Test
    public void devTestConvertAllUnhandledWorkMachineTrackingsInDbToObservations() throws JsonProcessingException {
        int count = 0;
        do {
            count = maintenanceDataService.handleUnhandledWorkMachineTrackings(100);
            log.info("handleUnhandledWorkMachineTrackings handledCount={} trackings", count);
        } while (count > 0);
    }

    /**
     * For development
     */
    @Rollback(false)
//    @Test
    public void devTestConvertNext100UnhandledWorkMachineTrackingsInDbToObservations() throws JsonProcessingException {
        final int count = maintenanceDataService.handleUnhandledWorkMachineTrackings(100);
        log.info("handleUnhandledWorkMachineTrackings handledCount={} trackings", count);
    }

    private int convertWorkMachineTrackingsToObservationsAndSave(final Map<Pair<Integer, Integer>, List<MaintenanceDataService.ObservationFeatureWrapper>> unhandledMap) {
        return unhandledMap.entrySet().stream().mapToInt(value -> maintenanceDataService.convertUnhandledWorkMachineTrackingsToObservations(value)).sum();
    }

    private void readTrackingJsonAndSave(final String dir, final int filesCount) throws IOException {
        int fileNo = 0;
        while ( fileNo < filesCount) {
            TyokoneenseurannanKirjausRequestSchema kirjaus = readTrackingJsonFile(String.format(dir +"/linestring_tracking_%d.json", fileNo ));
            maintenanceDataService.saveWorkMachineTrackingData(kirjaus);
            fileNo++;
        }
        maintenanceDataService.updateWorkMachineTrackingTypes();
    }

    private TyokoneenseurannanKirjausRequestSchema readTrackingJsonFile(final String fileName) throws IOException {
        final String jsonContent = readResourceContent("classpath:harja/service/" + fileName);
        final TyokoneenseurannanKirjausRequestSchema result = reader.readValue(jsonContent);
        return result;
    }

}