package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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

import fi.livi.digitraffic.tie.dao.v2.V2WorkMachineRealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2WorkMachineRealizationRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.MaaraSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TehtavaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TehtavatSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TyokoneSchema;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.model.v2.maintenance.WorkMachineRealizationData;

@Service
public class V2MaintenanceUpdateService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceUpdateService.class);
    private final V2WorkMachineRealizationRepository v2WorkMachineRealizationRepository;
    private final V2WorkMachineRealizationDataRepository v2WorkMachineRealizationDataRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;

    @Autowired
    public V2MaintenanceUpdateService(final V2WorkMachineRealizationRepository v2WorkMachineRealizationRepository,
                                      final V2WorkMachineRealizationDataRepository v2WorkMachineRealizationDataRepository,
                                      final ObjectMapper objectMapper) {
        this.v2WorkMachineRealizationRepository = v2WorkMachineRealizationRepository;
        this.v2WorkMachineRealizationDataRepository = v2WorkMachineRealizationDataRepository;
        this.jsonWriter = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
    }

    @Transactional
    public void saveNewWorkMachineRealization(final Long jobId, final ReittitoteumanKirjausRequestSchema reittitoteumanKirjaus) throws JsonProcessingException {
        final String json = jsonWriter.writeValueAsString(reittitoteumanKirjaus);
        WorkMachineRealizationData realization = new WorkMachineRealizationData(jobId, json);
        v2WorkMachineRealizationDataRepository.save(realization);
        log.info("method=saveWorkMachineRealizationData jsonData={}", json);
    }

    @Transactional
    public long handleUnhandledWorkMachineRealizations(int maxToHandle) {
        final Stream<WorkMachineRealizationData> data = v2WorkMachineRealizationDataRepository.findUnhandled(maxToHandle);
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

    private int handleWorkMachineRealization(final WorkMachineRealizationData wmr) throws JsonProcessingException {
        log.info("method=handleWorkMachineRealization {}", wmr);
        final ReittitoteumanKirjausRequestSchema kirjaus = jsonReader.readValue(wmr.getJson());

        // Message info
        final String sendingSystem = kirjaus.getOtsikko().getLahettaja().getJarjestelma();
        final Integer messageId = kirjaus.getOtsikko().getViestintunniste().getId();
        final ZonedDateTime sendingTime = kirjaus.getOtsikko().getLahetysaika();

        // Data is either in reittitoteuma or in reittitoteumat depending of system
        final List<ReittitoteumaSchema> toteumat =
            kirjaus.getReittitoteuma() != null ?
                Collections.singletonList(kirjaus.getReittitoteuma()) :
                kirjaus.getReittitoteumat().stream().map(rt -> rt.getReittitoteuma()).collect(Collectors.toList());

        toteumat.stream().forEach(reittitoteuma -> {

            // Tehtava
            final ZonedDateTime started = reittitoteuma.getToteuma().getAlkanut();
            final ZonedDateTime ended = reittitoteuma.getToteuma().getPaattynyt();
            final Integer sopimusId = reittitoteuma.getToteuma().getSopimusId();
            // This is not implemented by contractors
//            final String workMachineId = reittitoteuma.getTyokone().getTunniste();
//            final TyokoneSchema.Tyyppi workMachineType = reittitoteuma.getTyokone().getTyyppi();

            // Reitti
            final List<ReittiSchema> reitti = reittitoteuma.getReitti();
            reitti.stream().forEach(r -> {
                final KoordinaattisijaintiSchema koordinaatit = r.getReittipiste().getKoordinaatit();
                final Point point = new Point(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ());
                List<TehtavatSchema> tehtavat = r.getReittipiste().getTehtavat();
                tehtavat.stream().forEach(t -> {
                    final TehtavaSchema tehtava = t.getTehtava();
                    final Integer tehtavaId = tehtava.getId();
                    final MaaraSchema maara = tehtava.getMaara();
                    final String selite = tehtava.getSelite();
                });
            });
        });

        wmr.updateStatusToHandled();
        return 1;
    }

}
