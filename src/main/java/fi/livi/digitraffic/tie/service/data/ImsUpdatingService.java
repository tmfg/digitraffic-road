package fi.livi.digitraffic.tie.service.data;

import static fi.livi.digitraffic.tie.service.trafficmessage.ImsJsonConverter.getSituationType;

import java.time.Instant;
import java.time.ZonedDateTime;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;

import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalMessage;
import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.ImsMessage;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;
import fi.livi.digitraffic.tie.model.data.DataDatex2SituationMessage;
import fi.livi.digitraffic.tie.model.data.DataIncoming;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex223UpdateService;

@Service
public class ImsUpdatingService {
    private final Datex223UpdateService datex223UpdateService;
    private final DataDatex2SituationRepository dataDatex2SituationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ImsUpdatingService.class);

    public ImsUpdatingService(final Datex223UpdateService datex223UpdateService, final DataDatex2SituationRepository dataDatex2SituationRepository) {
        this.datex223UpdateService = datex223UpdateService;
        this.dataDatex2SituationRepository = dataDatex2SituationRepository;

        // this is needed to handle Instant
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @NotTransactionalServiceMethod
    public void handleIms(final DataIncoming data) throws JsonProcessingException {
        if(!data.getVersion().equals("1.2.2")) {
            throw new IllegalArgumentException("Unsupported version: " + data.getVersion());
        }

        final ImsMessage message = objectMapper.readerFor(ImsMessage.class).readValue(data.getData());

        final var simpleOptional = message.getMessageContent().getMessages().stream().filter(m -> m.getType() == MessageTypeEnum.SIMPPELI).findFirst();
        if(simpleOptional.isEmpty()) {
            throw new IllegalArgumentException("No Simple-json found");
        }
        final var simpleJson = simpleOptional.get();

        final var situation = createSituationFromSimple(simpleJson);
        handleDatexMessages(situation, message.getMessageContent());

        dataDatex2SituationRepository.save(situation);
    }

    private void handleDatexMessages(final DataDatex2Situation situation, final ImsMessage.MessageContent messageContent) {
        ImsMessage.MessageContent.Message d223Message = null;
        ImsMessage.MessageContent.Message simpleMessage = null;

        for(final ImsMessage.MessageContent.Message message : messageContent.getMessages()) {
            if(message.getType() == MessageTypeEnum.SIMPPELI) {
                simpleMessage = message;
            } else if(message.getType() == MessageTypeEnum.DATEX_2) {
                if(message.getVersion().equals(Datex2Version.V_2_2_3.version)) {
                    d223Message = message;
                }
            }
        }

        // add all messages
        messageContent.getMessages().forEach(m -> {
            final DataDatex2SituationMessage message = new DataDatex2SituationMessage(
                    m.getVersion(), m.getType().value(), m.getContent());

            situation.addMessage(message);
        });

        if(d223Message != null && simpleMessage != null) {
            handle223(d223Message.getContent(), simpleMessage.getContent());
        }
    }

    ///  insert DatexII 2.2.3 to old tables
    private void handle223(final String d223Message, final String simpleMessage) {
        final var models = datex223UpdateService.createModels(d223Message, simpleMessage, Instant.now());
        datex223UpdateService.updateTrafficDatex2Messages(models);
    }

    private DataDatex2Situation createSituationFromSimple(final ExternalMessage message)
            throws JsonProcessingException {
        final var simpleRoot = objectMapper.readTree(message.getContent());
        final var properties = simpleRoot.get("properties");
        final var situationId = properties.get("situationId").asText();
        final var situationVersion = properties.get("version").asInt();
        final var situationType = getSituationType(simpleRoot);

        final Geometry geometry = convertGeometry(simpleRoot.get("geometry").toPrettyString());
        final var publicationTime = ZonedDateTime.parse(properties.get("releaseTime").asText());
        final var times = getStartAndEndTimes(properties);

        return new DataDatex2Situation(situationId, situationVersion, situationType,
                geometry, publicationTime, times.getLeft(), times.getRight());
    }

    private Geometry convertGeometry(final String geometryString) {
        try {
            final Geometry geometry = PostgisGeometryUtils.convertGeoJsonGeometryToGeometry(geometryString);

            if(geometry.isValid()) {
                return geometry;
            }

            return PostgisGeometryUtils.fixGeometry(geometry);
        } catch(final Exception e) {
            log.error(String.format("method=convertGeometry Failed to fix feature json: %s",
                    geometryString), e);

            return null;
        }
    }

    private Pair<ZonedDateTime, ZonedDateTime> getStartAndEndTimes(final JsonNode properties) {
        final var announcements = properties.get("announcements");
        ZonedDateTime startTime = null;
        final ZonedDateTime endTime = null;

        for(final JsonNode announcement : announcements) {
            final var timeAndDuration = announcement.get("timeAndDuration");
            final var announcementStartTime = ZonedDateTime.parse(timeAndDuration.get("startTime").asText());
            if(startTime == null || startTime.isAfter(announcementStartTime)) {
                startTime = announcementStartTime;
            }
        };

        return Pair.of(startTime, endTime);
    }
}
