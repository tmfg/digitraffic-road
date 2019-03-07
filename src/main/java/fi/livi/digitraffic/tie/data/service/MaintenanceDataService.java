package fi.livi.digitraffic.tie.data.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.data.dao.WorkMachineTrackingRepository;
import fi.livi.digitraffic.tie.data.model.maintenance.json.WorkMachineTracking;
import fi.livi.digitraffic.tie.data.model.maintenance.json.WorkMachineTrackingRecord;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;

@ConditionalOnWebApplication
@Service
public class MaintenanceDataService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataService.class);
    private final WorkMachineTrackingRepository workMachineTrackingRepository;
    private final ConversionService conversionService;

    @Autowired
    public MaintenanceDataService(final WorkMachineTrackingRepository workMachineTrackingRepository,
                                  @Qualifier("mvcConversionService")
                                  final ConversionService conversionService) {
        this.workMachineTrackingRepository = workMachineTrackingRepository;
        this.conversionService = conversionService;
    }

    @Transactional
    public WorkMachineTracking saveWorkMachineTrackingData(final TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus) throws JsonProcessingException {

        final WorkMachineTrackingRecord record = conversionService.convert(tyokoneenseurannanKirjaus, WorkMachineTrackingRecord.class);
        final WorkMachineTracking tracking = new WorkMachineTracking(record);
        workMachineTrackingRepository.save(tracking);
        log.info("method=saveWorkMachineTrackingData Saved={}", tracking);
        return tracking;
    }

    @Transactional
    public int updateWorkMachineTrackingType() throws JsonProcessingException {
        return workMachineTrackingRepository.updateWorkMachineTrackingType();
    }

    @Transactional(readOnly = true)
    public List<WorkMachineTracking> findAll() {
        return workMachineTrackingRepository.findAll();
    }
}
