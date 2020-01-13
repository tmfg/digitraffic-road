package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.WorkMachineRealization.Status.HANDLED;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2WorkMachineRealizationRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.model.v2.maintenance.WorkMachineRealization;

@Import({ V2MaintenanceUpdateService.class, JacksonAutoConfiguration.class })
public class V2MaintenanceUpdateServiceTest extends AbstractServiceTest {

    @Autowired
    private V2MaintenanceUpdateService v2MaintenanceUpdateService;

    @Autowired
    private V2WorkMachineRealizationRepository v2WorkMachineRealizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private String realisationJSon;
    private ObjectReader reader;
    private ObjectWriter writer;

    @Before
    public void init() throws IOException {
        reader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        writer = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        v2WorkMachineRealizationRepository.deleteAllInBatch();
        flushAndClear();
        realisationJSon = readResourceContent("classpath:harja/controller/toteuma.json");
    }

    @Test
    public void saveNewWorkMachineRealization() throws IOException {
        saveRealization(realisationJSon);
        final String formattedRealisationJSon = writer.writeValueAsString(reader.readValue(realisationJSon));
        flushAndClear();
        final List<WorkMachineRealization> data = v2WorkMachineRealizationRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void handleUnhandledWorkMachineRealizations() throws JsonProcessingException {
        saveRealization(realisationJSon);
        final long count = v2MaintenanceUpdateService.handleUnhandledWorkMachineRealizations(100);
        Assert.assertEquals(1, count);
        flushAndClear();

        final List<WorkMachineRealization> data = v2WorkMachineRealizationRepository.findAll();
        Assert.assertEquals(1, data.size());
        final WorkMachineRealization realisation = data.get(0);
        Assert.assertEquals(HANDLED, realisation.getStatus());
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsWithError() throws JsonProcessingException {
        final String invalidJson =  "invalid json: " + realisationJSon;
        final WorkMachineRealization realization = new WorkMachineRealization(invalidJson);
        v2WorkMachineRealizationRepository.save(realization);
        flushAndClear();

        // Double check we have right data in db
        Assert.assertEquals(1, v2WorkMachineRealizationRepository.findUnhandled(100).count());

        final long count = v2MaintenanceUpdateService.handleUnhandledWorkMachineRealizations(100);
        Assert.assertEquals(0, count);
    }


    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void saveRealization(String realisationJSon) throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema realization = reader.readValue(realisationJSon);
        v2MaintenanceUpdateService.saveNewWorkMachineRealization(realization);
    }



}
