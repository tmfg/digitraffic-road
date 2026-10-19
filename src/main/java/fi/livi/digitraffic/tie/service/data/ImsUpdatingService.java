package fi.livi.digitraffic.tie.service.data;

import static fi.livi.digitraffic.tie.model.data.IncomingDataTypes.IMS_122;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
 *   <tr><td>DATEX2</td><td>XML string – Datex II d2LogicalModel / d2:payload</td><td>2.2.3, 3.5, 3.7</td></tr>
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
 *   <li>If a {@code DATEX2 v2.2.3} sub-message is present, also forward it together
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
    /// The source has been seen to send this text in place of a message, see {@link #validateContent}.
    private static final String NULL_CONTENT = "null";

    private static final int CONTENT_LOG_MAX_LENGTH = 200;

    private final DataDatex2SituationRepository dataDatex2SituationRepository;

    private final DatexII223UpdateService datexII223UpdateService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private static final Logger log = LoggerFactory.getLogger(ImsUpdatingService.class);

    public ImsUpdatingService(final DataDatex2SituationRepository dataDatex2SituationRepository,
                              final DatexII223UpdateService datexII223UpdateService) {
        this.dataDatex2SituationRepository = dataDatex2SituationRepository;
        this.datexII223UpdateService = datexII223UpdateService;
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
        handleDatexMessages(data, situation, message.getMessageContent());

        dataDatex2SituationRepository.save(situation);
    }

    /**
     * Persists all sub-messages of the IMS envelope as {@link DataDatex2SituationMessage} rows
     * and, if a Datex II 2.2.3 XML sub-message is present, also writes it to the legacy tables.
     *
     * <p>Sub-messages are stored with their raw {@code content} string (XML or JSON),
     * {@code type} (DATEX2 / SIMPPELI) and {@code version}. The content is never re-parsed here –
     * only the routing logic (which legacy table to update) requires inspecting type and version.</p>
     *
     * <p>Every sub-message is checked by {@link #validateContent} before anything is persisted.
     * An unusable content aborts the whole envelope, which marks the {@link DataIncoming} row
     * as {@code FAILED} and leaves it in the database for inspection and replay.</p>
     *
     * @throws IllegalArgumentException if any sub-message content is unusable
     */
    private void handleDatexMessages(final DataIncoming data,
                                     final DataDatex2Situation situation,
                                     final ImsMessage.MessageContent messageContent) {
        messageContent.getMessages().forEach(m -> validateContent(data, situation, m));

        ImsMessage.MessageContent.Message d223Message = null;
        ImsMessage.MessageContent.Message simpleMessage = null;

        for (final ImsMessage.MessageContent.Message message : messageContent.getMessages()) {
            if (message.getType() == MessageTypeEnum.SIMPPELI) {
                simpleMessage = message;
            } else if (message.getType() == MessageTypeEnum.DATEX2 &&
                    message.getVersion().equals(Datex2Version.V_2_2_3.version)) {
                d223Message = message;
            }
        }

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
     * <p>This is only called when the IMS envelope contains a {@code DATEX2 v2.2.3}
     * sub-message in addition to the mandatory {@code SIMPPELI} sub-message.</p>
     */
    private void handle223(final String d223Message, final String simpleMessage) {
        final var models = datexII223UpdateService.createModels(d223Message, simpleMessage, Instant.now());
        datexII223UpdateService.updateTrafficDatex2Messages(models);
    }

    /**
     * Rejects sub-message content that cannot be served later.
     *
     * <p>The source has been seen to send the literal four-character text {@code null} in place of a
     * Datex II message. Such content is not blank, so it passes the sender-side check in
     * {@link fi.livi.digitraffic.tie.service.trafficmessage.ImsUpdateService} and used to be stored
     * verbatim, producing a row that is valid in the database but unusable as XML. Rejecting it here
     * keeps the broken message out of the situation history.</p>
     *
     * @throws IllegalArgumentException if the content is blank, the text {@code null},
     *                                  or a Datex II message that does not start with an XML tag
     */
    private void validateContent(final DataIncoming data,
                                 final DataDatex2Situation situation,
                                 final ExternalMessage subMessage) {
        final String content = subMessage.getContent();
        final String reason;

        if (subMessage.getType() == null) {
            reason = "message type is missing";
        } else if (subMessage.getVersion() == null) {
            reason = "message version is missing";
        } else if (StringUtils.isBlank(content)) {
            reason = "content is blank";
        } else if (NULL_CONTENT.equalsIgnoreCase(content.trim())) {
            reason = "content is the literal text 'null'";
        } else if (subMessage.getType() == MessageTypeEnum.DATEX2 && !content.trim().startsWith("<")) {
            reason = "Datex II content does not start with an XML tag";
        } else {
            return;
        }

        // Logged with every identifier needed to find both the incoming row and the situation it belongs to.
        log.error("method=validateContent Rejecting IMS message, {}. " +
                        "dataId={} messageId={} situationId={} situationVersion={} " +
                        "messageType={} messageVersion={} contentLength={} content=\"{}\"",
                reason, data.getDataId(), data.getMessageId(),
                situation.getSituationId(), situation.getSituationVersion(),
                subMessage.getType(), subMessage.getVersion(),
                content == null ? 0 : content.length(),
                StringUtils.abbreviate(content, CONTENT_LOG_MAX_LENGTH));

        throw new IllegalArgumentException(
                "Unusable %s %s content in situation %s version %d (dataId %d): %s".formatted(
                        subMessage.getType(), subMessage.getVersion(),
                        situation.getSituationId(), situation.getSituationVersion(),
                        data.getDataId(), reason));
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
        final var versionTime = feature.getProperties().getVersionTime();
        final var times = getStartAndEndTimes(feature.getProperties().getAnnouncements(), versionTime);

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
     *   <li>If ALL announcements carry {@code earlyClosing=CANCELED}, {@code endTime} is
     *       set to {@code versionTime} (when this cancellation version was published by the
     *       source system). Falls back to {@code Instant.now()} if {@code versionTime} is
     *       null.</li>
     * </ul>
     *
     * @param versionTime the {@code versionTime} from the SIMPPELI message properties;
     *                    used as {@code endTime} when the situation is fully canceled.
     *                    May be {@code null}, in which case {@code Instant.now()} is used.
     */
    Pair<Instant, Instant> getStartAndEndTimes(final List<TrafficAnnouncement> announcements,
                                               final Instant versionTime) {
        Instant startTime = null;
        Instant endTime = null;
        boolean anyEndTimeNull = false;
        boolean allCanceled = !announcements.isEmpty();

        for (final TrafficAnnouncement announcement : announcements) {
            // null earlyClosing means the road work closes normally — not canceled.
            // Any announcement without CANCELED resets the flag for the whole situation.
            if (announcement.getEarlyClosing() != TrafficAnnouncement.EarlyClosing.CANCELED) {
                allCanceled = false;
            }

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

        // earlyClosing=CANCELED means the road work was canceled before its start time.
        // Consistent with the endTime invariant: a situation is only fully canceled when
        // ALL its announcements carry the canceled flag.
        // Use versionTime (when the source system published this cancellation version) as
        // end_time. Falls back to Instant.now() if versionTime is null.
        // startTime may be null if no announcement provided one; fall back to end_time so the
        // DB NOT NULL constraint on start_time is satisfied.
        if (allCanceled) {
            final Instant canceledEndTime = versionTime != null ? versionTime : Instant.now();
            return Pair.of(startTime != null ? startTime : canceledEndTime, canceledEndTime);
        }

        // Note: startTime may be null here too (if all announcements lack a startTime).
        // This is a pre-existing edge case; the caller is responsible for handling it
        // before persisting to the DB where start_time is NOT NULL.
        return Pair.of(startTime, anyEndTimeNull ? null : endTime);
    }
}
