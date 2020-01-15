package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.WorkMachineRealizationData.Status.HANDLED;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2WorkMachineRealizationDataRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.model.v2.maintenance.WorkMachineRealizationData;

@Import({ V2MaintenanceUpdateService.class, JacksonAutoConfiguration.class })
public class V2MaintenanceUpdateServiceTest extends AbstractServiceTest {

    @Autowired
    private V2MaintenanceUpdateService v2MaintenanceUpdateService;

    @Autowired
    private V2WorkMachineRealizationDataRepository v2WorkMachineRealizationDataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private String jsonSingleRealisation;
    private String jsonMultipleRealisations;
    private ObjectReader reader;
    private ObjectWriter writer;

    @Before
    public void init() throws IOException {
        reader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        writer = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        v2WorkMachineRealizationDataRepository.deleteAllInBatch();
        flushAndClear();
        jsonSingleRealisation = readResourceContent("classpath:harja/controller/toteumakirjaus-yksi-reittitoteuma.json");
        jsonMultipleRealisations = readResourceContent("classpath:harja/controller/toteumakirjaus-monta-reittitoteumaa.json");
    }

    @Rollback(false)
    @Test
    public void saveNewWorkMachineRealization_single() throws IOException {
        saveRealization(jsonSingleRealisation);
        final String formattedRealisationJSon = writer.writeValueAsString(reader.readValue(jsonSingleRealisation));
        flushAndClear();
        final List<WorkMachineRealizationData> data = v2WorkMachineRealizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Rollback(false)
    @Test
    public void saveNewWorkMachineRealization_multiple() throws IOException {
        saveRealization(jsonMultipleRealisations);
        final String formattedRealisationJSon = writer.writeValueAsString(reader.readValue(jsonMultipleRealisations));
        flushAndClear();
        final List<WorkMachineRealizationData> data = v2WorkMachineRealizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void handleUnhandledWorkMachineRealizations() throws JsonProcessingException {
        saveRealization(jsonSingleRealisation);
        saveRealization(jsonMultipleRealisations);
        final long count = v2MaintenanceUpdateService.handleUnhandledWorkMachineRealizations(100);
        Assert.assertEquals(2, count);
        flushAndClear();

        final List<WorkMachineRealizationData> data = v2WorkMachineRealizationDataRepository.findAll();
        Assert.assertEquals(2, data.size());
        final WorkMachineRealizationData realisation = data.get(0);
        Assert.assertEquals(HANDLED, data.get(0).getStatus());
        Assert.assertEquals(HANDLED, data.get(1).getStatus());
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsWithError() {
        final String invalidJson =  "invalid json: " + jsonSingleRealisation;
        final WorkMachineRealizationData realization = new WorkMachineRealizationData(123L, invalidJson);
        v2WorkMachineRealizationDataRepository.save(realization);
        flushAndClear();

        // Double check we have right data in db
        Assert.assertEquals(1, v2WorkMachineRealizationDataRepository.findUnhandled(100).count());

        final long count = v2MaintenanceUpdateService.handleUnhandledWorkMachineRealizations(100);
        Assert.assertEquals(0, count);
    }


    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void saveRealization(String realisationJSon) throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema realization = reader.readValue(realisationJSon);
        v2MaintenanceUpdateService.saveNewWorkMachineRealization(123L, realization);
    }



}
