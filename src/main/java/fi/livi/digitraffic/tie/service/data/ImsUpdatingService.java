package fi.livi.digitraffic.tie.service.data;

import static fi.livi.digitraffic.tie.model.data.IncomingDataTypes.IMS_122;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalMessage;
import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncement;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.ImsMessage;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;
import fi.livi.digitraffic.tie.model.data.DataDatex2SituationMessage;
import fi.livi.digitraffic.tie.model.data.DataIncoming;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII223UpdateService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Second stage of the IMS message processing pipeline.
 *
 * <p>Reads a JSON-serialised IMS v1.2.2 envelope from {@link DataIncoming}
 * (written by {@link fi.livi.digitraffic.tie.service.trafficmessage.ImsUpdateService})
 * and persists the traffic situation and its individual sub-messages to the database.</p>
 *
 * <h2>Sub-message types and their {@code content} format</h2>
 * <table border="1">
 *   <tr><th>type</th><th>content format</th><th>example version</th></tr>
 *   <tr><td>SIMPPELI</td><td>JSON string – {@link ImsGeoJsonFeature} (GeoJSON Feature with traffic announcement properties)</td><td>0.2.x</td></tr>
 *   <tr><td>DATEX_2</td><td>XML string – Datex II d2LogicalModel / d2:payload</td><td>2.2.3, 3.5, 3.7</td></tr>
 * </table>
 *
 * <h2>Processing steps</h2>
 * <ol>
 *   <li>Deserialize JSON envelope → {@link ImsMessage}.</li>
 *   <li>Find the mandatory {@code SIMPPELI} sub-message; parse its JSON content as
 *       {@link ImsGeoJsonFeature} to extract situation id, version, type, geometry
 *       and time window → create {@link DataDatex2Situation}.</li>
 *   <li>Iterate all sub-messages; persist each as {@link DataDatex2SituationMessage}
 *       (stores the raw content string together with type and version).</li>
 *   <li>If a {@code DATEX_2 v2.2.3} sub-message is present, also forward it together
 *       with the SIMPPELI content to {@link DatexII223UpdateService} to maintain
 *       backwards-compatible legacy Datex II 2.2.3 tables.</li>
 * </ol>
 *
 * <p>Each call runs in its own {@link org.springframework.transaction.annotation.Propagation#REQUIRES_NEW}
 * transaction so that a single invalid message does not roll back the rest of the batch.</p>
 *
 * @see fi.livi.digitraffic.tie.service.trafficmessage.ImsUpdateService  first stage (JMS → DB)
 */
@Service
public class ImsUpdatingService {
    private final DatexII223UpdateService datexII223UpdateService;
    private final DataDatex2SituationRepository dataDatex2SituationRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private static final Logger log = LoggerFactory.getLogger(ImsUpdatingService.class);

    public ImsUpdatingService(final DatexII223UpdateService datexII223UpdateService,
                              final DataDatex2SituationRepository dataDatex2SituationRepository) {
        this.datexII223UpdateService = datexII223UpdateService;
        this.dataDatex2SituationRepository = dataDatex2SituationRepository;
    }

    /**
     * Open a new transaction for the handling of each message.
     * This is to prevent rolling back successfully processed
     * messages if the batch also contains invalid messages causing
     * for example a duplicate key violation (happens if we receive
     * the same message twice for some reason).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleIms(final DataIncoming data) throws JacksonException {
        if (!data.getVersion().equals(IMS_122)) {
            throw new IllegalArgumentException("Unsupported version: " + data.getVersion());
        }

        final ImsMessage message = objectMapper.readerFor(ImsMessage.class).readValue(data.getData());

        final var simpleOptional =
                message.getMessageContent().getMessages().stream().filter(m -> m.getType() == MessageTypeEnum.SIMPPELI)
                        .findFirst();
        if (simpleOptional.isEmpty()) {
            throw new IllegalArgumentException("No Simple-json found id :" + data.getDataId());
        }
        final var simpleJson = simpleOptional.get();

        final var situation = createSituationFromSimple(simpleJson);
        handleDatexMessages(situation, message.getMessageContent());

        dataDatex2SituationRepository.save(situation);
    }

    /**
     * Persists all sub-messages of the IMS envelope as {@link DataDatex2SituationMessage} rows
     * and, if a Datex II 2.2.3 XML sub-message is present, also writes it to the legacy tables.
     *
     * <p>Sub-messages are stored with their raw {@code content} string (XML or JSON),
     * {@code type} (DATEX_2 / SIMPPELI) and {@code version}.  The content is never
     * re-parsed here – only the routing logic (which legacy table to update) requires
     * inspecting type and version.</p>
     */
    private void handleDatexMessages(final DataDatex2Situation situation,
                                     final ImsMessage.MessageContent messageContent) {
        ImsMessage.MessageContent.Message d223Message = null;
        ImsMessage.MessageContent.Message simpleMessage = null;

        for (final ImsMessage.MessageContent.Message message : messageContent.getMessages()) {
            if (message.getType() == MessageTypeEnum.SIMPPELI) {
                simpleMessage = message;
            } else if (message.getType() == MessageTypeEnum.DATEX_2 &&
                    message.getVersion().equals(Datex2Version.V_2_2_3.version)) {
                d223Message = message;
            }
        }

        // add all messages
        messageContent.getMessages().forEach(m -> {
            final DataDatex2SituationMessage message = new DataDatex2SituationMessage(
                    m.getVersion(), m.getType().value(), m.getContent());

            situation.addMessage(message);
        });

        if (d223Message != null && simpleMessage != null) {
            handle223(d223Message.getContent(), simpleMessage.getContent());
        }
    }

    /**
     * Forwards a Datex II 2.2.3 XML string and the corresponding SIMPPELI GeoJSON string
     * to {@link DatexII223UpdateService} to keep the legacy Datex II 2.2.3 tables in sync.
     *
     * <p>This is only called when the IMS envelope contains a {@code DATEX_2 v2.2.3}
     * sub-message in addition to the mandatory {@code SIMPPELI} sub-message.</p>
     */
    private void handle223(final String d223Message, final String simpleMessage) {
        final var models = datexII223UpdateService.createModels(d223Message, simpleMessage, Instant.now());
        datexII223UpdateService.updateTrafficDatex2Messages(models);
    }

    private DataDatex2Situation createSituationFromSimple(final ExternalMessage message)
            throws JacksonException {
        final ImsGeoJsonFeature feature =
                objectMapper.readerFor(ImsGeoJsonFeature.class).readValue(message.getContent());

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

            if (geometry.isValid()) {
                return geometry;
            }

            return PostgisGeometryUtils.fixGeometry(geometry);
        } catch (final Exception e) {
            log.error(String.format("method=convertGeometry Failed to fix feature json: %s",
                    geometryString), e);

            return null;
        }
    }

    /**
     * Derives the overall situation time window from its announcements.
     *
     * <ul>
     *   <li><b>startTime</b> – the earliest {@code startTime} across all announcements
     *       (i.e. when the situation first became active).</li>
     *   <li><b>endTime</b> – the latest {@code endTime} across all announcements,
     *       i.e. the time window that covers all of them.
     *       Returns {@code null} if <em>any</em> announcement is open-ended
     *       ({@code endTime == null} or {@code timeAndDuration} is absent altogether),
     *       because the situation as a whole cannot be considered ended until every
     *       announcement has ended.</li>
     * </ul>
     */
    Pair<Instant, Instant> getStartAndEndTimes(final List<TrafficAnnouncement> announcements) {
        Instant startTime = null;
        Instant endTime = null;
        boolean anyEndTimeNull = false;

        for (final TrafficAnnouncement announcement : announcements) {
            final var timeAndDuration = announcement.getTimeAndDuration();

            // timeAndDuration is not required by the IMS JSON schema — treat a
            // missing block the same as an open-ended announcement with no startTime.
            if (timeAndDuration == null) {
                anyEndTimeNull = true;
                continue;
            }

            final var announcementStartTime = timeAndDuration.getStartTime();
            final var announcementEndTime = timeAndDuration.getEndTime();

            if (announcementStartTime != null) {
                if (startTime == null || startTime.isAfter(announcementStartTime)) {
                    startTime = announcementStartTime;
                }
            }

            if (announcementEndTime == null) {
                // If any announcement is open-ended, the whole situation is open-ended.
                // A situation is only fully ended when all its announcements have ended.
                anyEndTimeNull = true;
            } else {
                if (endTime == null || endTime.isBefore(announcementEndTime)) {
                    endTime = announcementEndTime;
                }
            }
        }

        return Pair.of(startTime, anyEndTimeNull ? null : endTime);
    }
}
