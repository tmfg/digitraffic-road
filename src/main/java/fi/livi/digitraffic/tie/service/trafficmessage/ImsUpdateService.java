package fi.livi.digitraffic.tie.service.trafficmessage;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalIMSMessage;
import fi.livi.digitraffic.tie.helper.LoggerHelper;
import fi.livi.digitraffic.tie.model.data.DataIncoming;
import fi.livi.digitraffic.tie.service.data.DataUpdatingService;

/**
 * First stage of the IMS message processing pipeline (daemon/worker side only).
 *
 * <h2>Overall pipeline</h2>
 * <pre>
 *  JMS broker (Artemis)
 *       │
 *       │  ActiveMQTextMessage  (body = IMS v1.2.2 XML)
 *       ▼
 *  ImsJMSMessageMarshaller
 *       │  JAXB unmarshal: XML  →  ExternalIMSMessage Java object
 *       ▼
 *  ImsUpdateService.handleImsMessages()          ← THIS CLASS
 *       │  Jackson serialize: ExternalIMSMessage  →  JSON string
 *       │  Persist as DataIncoming (type = IMS_122) in the incoming-data table
 *       ▼
 *  ImsUpdatingService.handleIms()
 *       │  Jackson deserialize: JSON  →  ImsMessage
 *       │  Extract per-message content strings:
 *       │    • SIMPPELI  → content is a JSON string  (ImsGeoJsonFeature / SIMPPELI schema)
 *       │    • DATEX_2   → content is an XML string  (Datex II 2.2.3 / 3.5 / 3.7)
 *       │  Persist DataDatex2Situation + DataDatex2SituationMessage rows
 *       │  If DATEX_2 v2.2.3 present → also update legacy Datex II 2.2.3 tables
 *       ▼
 *  Done
 * </pre>
 *
 * <h2>Message content structure</h2>
 * One IMS envelope ({@link fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalIMSMessage})
 * carries a {@code messageContent} that contains a list of typed sub-messages.
 * After Jackson serialisation the JSON looks like:
 * <pre>
 * {
 *   "messageId": 12345,
 *   "messageContent": {
 *     "messages": [
 *       { "type": "DATEX_2",  "version": "2.2.3",  "content": "&lt;?xml ...&gt;&lt;d2LogicalModel ...&gt;...&lt;/d2LogicalModel&gt;" },
 *       { "type": "DATEX_2",  "version": "3.5",    "content": "&lt;?xml ...&gt;&lt;d2:payload ...&gt;...&lt;/d2:payload&gt;"    },
 *       { "type": "SIMPPELI", "version": "0.2.17", "content": "{\"geometry\":{...}, \"properties\":{...}}"                      }
 *     ]
 *   }
 * }
 * </pre>
 * The {@code content} field is always a raw escaped string – never a nested JSON object –
 * because it is defined as {@code xs:string} in the ImsXmlMessage_1_2_2.xsd schema.
 *
 * @see fi.livi.digitraffic.tie.service.jms.marshaller.ImsJMSMessageMarshaller  JMS → Java (JAXB, XML)
 * @see fi.livi.digitraffic.tie.service.data.ImsUpdatingService  Java → DB (second stage)
 */
@ConditionalOnNotWebApplication
@Service
public class ImsUpdateService {
    private static final Logger log = LoggerFactory.getLogger(ImsUpdateService.class);

    private final DataUpdatingService dataUpdatingService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Autowired
    public ImsUpdateService(final DataUpdatingService dataUpdatingService) {
        this.dataUpdatingService = dataUpdatingService;
    }

    /**
     * Serialises each incoming {@link ExternalIMSMessage} to JSON and persists it as a
     * {@link DataIncoming} record (type {@code IMS_122}) for deferred processing.
     *
     * <p>The messages have already been JAXB-unmarshalled from XML at this point
     * (see {@link fi.livi.digitraffic.tie.service.jms.marshaller.ImsJMSMessageMarshaller}).  Jackson re-serialises the Java objects
     * back to JSON so that the raw content strings (Datex II XML, SIMPPELI GeoJSON)
     * are preserved verbatim inside the JSON envelope.</p>
     *
     * <p>Messages that fail validation (missing content, type, or version on any
     * sub-message) are logged as errors and skipped – they are <em>not</em> persisted.</p>
     *
     * @param imsMessages list of deserialized IMS v1.2.2 messages from JMS
     * @return number of messages accepted and forwarded to the data store
     * @see fi.livi.digitraffic.tie.service.data.ImsUpdatingService#handleIms  second-stage processing
     */
    @Transactional
    public int handleImsMessages(final List<ExternalIMSMessage> imsMessages) {
        final StopWatch sw = StopWatch.createStarted();
        final int newAndUpdated = imsMessages.stream().mapToInt(imsMessage -> {
            if (log.isDebugEnabled()) {
                log.debug("method=handleTrafficDatex2ImsMessages messageContent: {}",
                        LoggerHelper.objectToStringLoggerSafe(imsMessage));
            }

            try {
                final var messageAsString = objectMapper.writeValueAsString(imsMessage);
                final var data = DataIncoming.ims122(String.valueOf(imsMessage.getMessageId()), messageAsString);

                if(validate(imsMessage)) {
                    dataUpdatingService.insertData(data);
                } else {
                    log.error("method=handleTrafficDatex2ImsMessage invalid ims {}", messageAsString);
                }
            } catch (final JacksonException e) {
                throw new RuntimeException(e);
            }

            return 1;
        }).sum();
        log.info("method=handleTrafficDatex2ImsMessages updateCount={} tookMs={}", newAndUpdated, sw.getDuration().toMillis());
        return newAndUpdated;
    }

    /**
     * Validates that every sub-message inside the IMS envelope has a non-blank
     * {@code content} string, a non-null {@code type}, and a non-null {@code version}.
     *
     * <p>These are the minimum fields required by the downstream
     * {@link fi.livi.digitraffic.tie.service.data.ImsUpdatingService} to route and
     * parse each sub-message (SIMPPELI JSON or DATEX_2 XML).</p>
     */
    private boolean validate(final ExternalIMSMessage imsMessage) {
        return imsMessage.getMessageContent().getMessages().stream()
                .noneMatch(m -> StringUtils.isBlank(m.getContent()) || m.getType() == null || m.getVersion() == null);
    }
}
