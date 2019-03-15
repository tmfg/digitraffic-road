package fi.livi.digitraffic.tie.data.service;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineObservation;
import fi.livi.digitraffic.tie.data.model.maintenance.json.WorkMachineTrackingImmutable;

public class MaintenanceServiceServiceTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceServiceServiceTest.class);

    @Autowired
    private MaintenanceDataService maintenanceDataService;


    @Test
    public void findAllNotHandledWorkMachineTrackings() {
        List<WorkMachineTrackingImmutable> all =
            maintenanceDataService.findAllNotHandledWorkMachineTrackingsOldestFirst();
        log.info("all: {}", all);
    }

    @Test
    public void findWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId() {
        List<WorkMachineObservation> all =
            maintenanceDataService.findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(1L, 1L);
        log.info("all: {}", all);
    }

    @Test
    public void findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId() {
        WorkMachineObservation found =
            maintenanceDataService.findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(1L, 1L);
        log.info("found: {}", found);
    }


    @Test
    @Rollback(false)
    @Transactional(timeout = 1800)
    public void handleUnhandledWorkMachineTrakkings() throws JsonProcessingException {
        log.info("handleUnhandledWorkMachineTrakkings count {}",
                 maintenanceDataService.handleUnhandledWorkMachineTrakkings());
    }


}