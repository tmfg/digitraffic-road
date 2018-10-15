package fi.livi.digitraffic.tie.data.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.data.dao.WorkMachineTrackingRepository;
import fi.livi.digitraffic.tie.data.model.WorkMachineTracking;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Service
public class MaintenanceDataService {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataService.class);
    private ObjectMapper objectMapper;
    private WorkMachineTrackingRepository workMachineTrackingRepository;

    @Autowired
    public MaintenanceDataService(final ObjectMapper objectMapper,
                                  final WorkMachineTrackingRepository workMachineTrackingRepository) {
        this.objectMapper = objectMapper;
        this.workMachineTrackingRepository = workMachineTrackingRepository;
    }

    @Transactional
    public void saveWorkMachineTrackingData(final TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus) throws JsonProcessingException {

        final WorkMachineTracking tracking = new WorkMachineTracking(tyokoneenseurannanKirjaus);
        workMachineTrackingRepository.save(tracking);
    }
}
