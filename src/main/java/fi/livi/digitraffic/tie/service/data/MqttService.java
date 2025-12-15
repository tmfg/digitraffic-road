package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;

import fi.livi.digitraffic.tie.model.data.SituationMqttMessage;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@ConditionalOnNotWebApplication
@Service
public class MqttService {
    private final DataDatex2SituationRepository dataDatex2SituationRepository;

    public MqttService(final DataDatex2SituationRepository dataDatex2SituationRepository) {
        this.dataDatex2SituationRepository = dataDatex2SituationRepository;
    }

    @Transactional(readOnly = true)
    public Pair<Instant, List<SituationMqttMessage>> findMessagesAfter(final Instant lastUpdated) {
        final var messages = dataDatex2SituationRepository.findMessagesForMqtt(lastUpdated);

        // messages are sorted with latest first
        final Instant latestCreated = messages.isEmpty() ? null : messages.getFirst().getModifiedAt();

        return Pair.of(latestCreated, messages);
    }
}
