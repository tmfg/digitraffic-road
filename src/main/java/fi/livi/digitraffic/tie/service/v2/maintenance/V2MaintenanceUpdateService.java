package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v2.V2WorkMachineRealizationRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.model.v2.maintenance.WorkMachineRealization;

@Service
public class V2MaintenanceUpdateService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceUpdateService.class);
    private final V2WorkMachineRealizationRepository v2WorkMachineRealizationRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;

    @Autowired
    public V2MaintenanceUpdateService(final V2WorkMachineRealizationRepository v2WorkMachineRealizationRepository,
                                      final ObjectMapper objectMapper) {
        this.v2WorkMachineRealizationRepository = v2WorkMachineRealizationRepository;
        this.jsonWriter = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
    }

    @Transactional
    public void saveNewWorkMachineRealization(final ReittitoteumanKirjausRequestSchema reittitoteumanKirjaus) throws JsonProcessingException {
        final String json = jsonWriter.writeValueAsString(reittitoteumanKirjaus);
        WorkMachineRealization realization = new WorkMachineRealization(json);
        v2WorkMachineRealizationRepository.save(realization);
        log.info("method=saveWorkMachineRealizationData jsonData={}", json);
    }

    @Transactional
    public long handleUnhandledWorkMachineRealizations(int maxToHandle) {
        final Stream<WorkMachineRealization> data = v2WorkMachineRealizationRepository.findUnhandled(maxToHandle);
        final long handled = data.mapToInt(wmr -> {
            try {
                return handleWorkMachineRealization(wmr);
            } catch (JsonProcessingException ex) {
                log.error(String.format("HandleWorkMachineRealization failed for id %d", wmr.getId()), ex);
                wmr.updateStatusToError();
                return 0;
            }
        }).sum();
        return handled;
    }

    private int handleWorkMachineRealization(final WorkMachineRealization wmr) throws JsonProcessingException {
        log.info("method=handleWorkMachineRealization {}", wmr);
        final ReittitoteumanKirjausRequestSchema toteuma = jsonReader.readValue("plaa " + wmr.getJson());
        wmr.updateStatusToHandled();
        return 1;
    }

}
