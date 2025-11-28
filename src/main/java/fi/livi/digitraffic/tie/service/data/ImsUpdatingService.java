package fi.livi.digitraffic.tie.service.data;

import static fi.livi.digitraffic.tie.model.data.IncomingDataTypes.IMS_122;
import static fi.livi.digitraffic.tie.service.trafficmessage.ImsJsonConverter.getSituationType;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;

import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature;

import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncement;

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
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII223UpdateService;

@Service
public class ImsUpdatingService {
    private final DatexII223UpdateService datexII223UpdateService;
    private final DataDatex2SituationRepository dataDatex2SituationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ImsUpdatingService.class);

    public ImsUpdatingService(final DatexII223UpdateService datexII223UpdateService, final DataDatex2SituationRepository dataDatex2SituationRepository) {
        this.datexII223UpdateService = datexII223UpdateService;
        this.dataDatex2SituationRepository = dataDatex2SituationRepository;

        // this is needed to handle Instant
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @NotTransactionalServiceMethod
    public void handleIms(final DataIncoming data) throws JsonProcessingException {
        if(!data.getVersion().equals(IMS_122)) {
            throw new IllegalArgumentException("Unsupported version: " + data.getVersion());
        }

        final ImsMessage message = objectMapper.readerFor(ImsMessage.class).readValue(data.getData());

        final var simpleOptional = message.getMessageContent().getMessages().stream().filter(m -> m.getType() == MessageTypeEnum.SIMPPELI).findFirst();
        if(simpleOptional.isEmpty()) {
            throw new IllegalArgumentException("No Simple-json found id :" + data.getDataId());
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
        final var models = datexII223UpdateService.createModels(d223Message, simpleMessage, Instant.now());
        datexII223UpdateService.updateTrafficDatex2Messages(models);
    }

    private DataDatex2Situation createSituationFromSimple(final ExternalMessage message)
            throws JsonProcessingException {
        final ImsGeoJsonFeature feature = objectMapper.readerFor(ImsGeoJsonFeature.class).readValue(message.getContent());

        final var situationId = feature.getProperties().getSituationId();
        final var situationVersion = feature.getProperties().getVersion();
        final var situationType = feature.getProperties().getSituationType();
            //getSituationType(simpleRoot);

        final var geometry = convertGeometry(objectMapper.valueToTree(feature.getGeometry()).toString());
//        final Geometry geometry = convertGeometry(simpleRoot.get("geometry").toPrettyString());
        final var publicationTime = feature.getProperties().getReleaseTime();
        final var times = getStartAndEndTimes(feature.getProperties().getAnnouncements());

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

    private Pair<Instant, Instant> getStartAndEndTimes(final List<TrafficAnnouncement> announcements) {
        Instant startTime = null;
        Instant endTime = null;

        for(final TrafficAnnouncement announcement : announcements) {
            final var timeAndDuration = announcement.getTimeAndDuration();
            final var announcementStartTime = timeAndDuration.getStartTime();
            final var announcementEndTime = timeAndDuration.getEndTime();

            if(announcementStartTime != null) {
                if (startTime == null || startTime.isAfter(announcementStartTime)) {
                    startTime = announcementStartTime;
                }
            }

            if(announcementEndTime != null) {
                if (endTime == null || endTime.isBefore(announcementEndTime)) {
                    endTime = announcementEndTime;
                }
            }
        };

        return Pair.of(startTime, endTime);
    }

    private Instant safeParseInstant(final JsonNode node) {
        return node == null ? null : Instant.parse(node.asText());
    }
}
