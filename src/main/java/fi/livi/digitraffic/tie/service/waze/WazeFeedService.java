package fi.livi.digitraffic.tie.service.waze;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.waze.WazeDatex2Converter;
import fi.livi.digitraffic.tie.converter.waze.WazeDatex2JsonConverter;
import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedAnnouncementDto;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeFeedIncidentDto;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.model.data.SituationMessage;
import fi.livi.digitraffic.tie.service.data.DatexIIService;

@ConditionalOnWebApplication
@Service
public class WazeFeedService {
    private static final Logger logger = LoggerFactory.getLogger(WazeFeedService.class);

    private final DataDatex2SituationRepository dataDatex2SituationRepository;
    private final WazeDatex2Converter wazeDatex2Converter;
    private final WazeDatex2JsonConverter wazeDatex2JsonConverter;

    @Autowired
    public WazeFeedService(final DataDatex2SituationRepository dataDatex2SituationRepository,
                           final WazeDatex2Converter wazeDatex2Converter,
                           final WazeDatex2JsonConverter wazeDatex2JsonConverter) {
        this.dataDatex2SituationRepository = dataDatex2SituationRepository;
        this.wazeDatex2Converter = wazeDatex2Converter;
        this.wazeDatex2JsonConverter = wazeDatex2JsonConverter;
    }

    @Transactional(readOnly = true)
    public WazeFeedAnnouncementDto findActive() {
        final StopWatch time = StopWatch.createStarted();
        final Instant from = Instant.now().minus(1, ChronoUnit.HOURS);
        final Instant to = Instant.now().plus(3650, ChronoUnit.DAYS);

        final List<SituationMessage> activeMessages = dataDatex2SituationRepository.findActiveSituationMessages(
            List.of(SituationType.TRAFFIC_ANNOUNCEMENT.name(), SituationType.ROAD_WORK.name()),
            from,
            to,
            DatexIIService.MAX_SIMPPELI_VERSION);

        time.stop();
        final StopWatch filterTime = StopWatch.createStarted();
        final List<WazeFeedIncidentDto> incidents = activeMessages.stream()
            .collect(Collectors.groupingBy(SituationMessage::getSituationId))
            .values().stream()
            .map(this::toSituationMessagePair)
            .flatMap(Optional::stream)
            .map(this.wazeDatex2Converter::convertToWazeDatex2FeatureDto)
            .flatMap(Optional::stream)
            .filter(WazeDatex2Converter::isValidWazeEntry)
            .flatMap(this.wazeDatex2JsonConverter::convertToWazeFeedAnnouncementDto)
            .toList();

        filterTime.stop();
        logger.info("method=findActive valid incidents activeCount={} valid count={}, fetchTookMs={} filterTookMS={} tookMs={}",
                    activeMessages.size(), incidents.size(), time.getDuration().toMillis(), filterTime.getDuration().toMillis(), time.getDuration().toMillis() + filterTime.getDuration().toMillis());

        return new WazeFeedAnnouncementDto(incidents);
    }

    private Optional<WazeDatex2Converter.SituationMessagePair> toSituationMessagePair(final List<SituationMessage> messages) {
        // the query orders rows by message version descending, so keeping the first row per type
        // deterministically yields the newest allowed version
        final Map<MessageTypeEnum, SituationMessage> byType = messages.stream().collect(Collectors.toMap(
            m -> MessageTypeEnum.fromValue(m.getMessageType()),
            Function.identity(),
            (first, _) -> first));

        final SituationMessage simppeli = byType.get(MessageTypeEnum.SIMPPELI);
        final SituationMessage datex2 = byType.get(MessageTypeEnum.DATEX2);
        if (simppeli == null || datex2 == null) {
            final String situationId = messages.isEmpty() ? "unknown" : messages.getFirst().getSituationId();
            logger.warn("method=findActive situationId={} missing paired messages simppeliPresent={} datex2Present={}",
                situationId, simppeli != null, datex2 != null);
            return Optional.empty();
        }

        return Optional.of(new WazeDatex2Converter.SituationMessagePair(
            simppeli.getSituationId(),
            fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType.valueOf(simppeli.getSituationType()),
            simppeli.getMessage(),
            datex2.getMessage(),
            Stream.of(simppeli.getModifiedAt(), datex2.getModifiedAt()).max(Instant::compareTo).orElse(datex2.getModifiedAt())));
    }
}
