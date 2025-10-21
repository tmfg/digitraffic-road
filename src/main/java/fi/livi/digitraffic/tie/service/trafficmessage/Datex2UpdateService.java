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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.livi.digitraffic.tie.conf.kca.artemis.jms.message.ExternalIMSMessage;
import fi.livi.digitraffic.tie.helper.LoggerHelper;
import fi.livi.digitraffic.tie.model.data.DataIncoming;
import fi.livi.digitraffic.tie.service.data.DataUpdatingService;

@ConditionalOnNotWebApplication
@Service
public class Datex2UpdateService {
    private static final Logger log = LoggerFactory.getLogger(Datex2UpdateService.class);

    private final DataUpdatingService dataUpdatingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public Datex2UpdateService(final DataUpdatingService dataUpdatingService) {
        this.dataUpdatingService = dataUpdatingService;

        // this is needed to handle Instant
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public int handleTrafficDatex2ImsMessages(final List<ExternalIMSMessage> imsMessages) {
        final StopWatch sw = StopWatch.createStarted();
        final int newAndUpdated = imsMessages.stream().mapToInt(imsMessage -> {
            if (log.isDebugEnabled()) {
                log.debug("method=handleTrafficDatex2ImsMessages messageContent: {}",
                        LoggerHelper.objectToStringLoggerSafe(imsMessage));
            }

            try {
                final var messageAsString = objectMapper.writeValueAsString(imsMessage);
                final var data = new DataIncoming(String.valueOf(imsMessage.getMessageId()), "1.2.2",
                        "IMS", messageAsString);

                if(validate(imsMessage)) {
                    dataUpdatingService.insertData(data);
                } else {
                    log.error("method=handleTrafficDatex2ImsMessage invalid ims {}", messageAsString);
                }
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            return 1;
        }).sum();
        log.info("method=handleTrafficDatex2ImsMessages updateCount={} tookMs={}", newAndUpdated, sw.getDuration().toMillis());
        return newAndUpdated;
    }

    ///  validate that all messages have content and type and version defined
    private boolean validate(final ExternalIMSMessage imsMessage) {
        return imsMessage.getMessageContent().getMessages().stream()
                .noneMatch(m -> StringUtils.isBlank(m.getContent()) || m.getType() == null || m.getVersion() == null);
    }
}
