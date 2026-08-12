package fi.livi.digitraffic.tie.service.data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import fi.livi.digitraffic.tie.controller.trafficmessage.MessageConverter;
import fi.livi.digitraffic.tie.dto.trafficmessage.v2.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v2.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;

import fi.livi.digitraffic.tie.model.ModifiedAt;

import fi.livi.digitraffic.tie.model.data.MessageAndModified;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;

import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v3_5.SituationPublication;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@Service
public class DatexIIService {
    private final DataDatex2SituationRepository dataDatex2SituationRepository;
    private final DatexII35Converter datexII35Converter;
    private final DatexII37Converter datexII37Converter;
    private final DatexII223Converter datexII223Converter;
    private final MessageConverter messageConverter;
    private final boolean rttiEnabled;

    private static final Logger log = LoggerFactory.getLogger(DatexIIService.class);

    private static final Instant TIME_END = Instant.ofEpochMilli(32503683600000L);

    /** Default value for {@code from} when not provided by the caller: current time minus one hour. */
    private static Instant defaultFrom() {
        return Instant.now().minus(1, ChronoUnit.HOURS);
    }

    public DatexIIService(final DataDatex2SituationRepository dataDatex2SituationRepository,
                          final DatexII35Converter datexII35Converter, final DatexII37Converter datexII37Converter,
                          final DatexII223Converter datexII223Converter,
                          final MessageConverter messageConverter,
                          @Value("${dt.trafficMessage.rtti.enabled:true}") final boolean rttiEnabled) {
        this.dataDatex2SituationRepository = dataDatex2SituationRepository;
        this.datexII35Converter = datexII35Converter;
        this.datexII37Converter = datexII37Converter;
        this.datexII223Converter = datexII223Converter;
        this.messageConverter = messageConverter;
        this.rttiEnabled = rttiEnabled;
    }

    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findRoadworks223(final Instant from, final Instant to) {
        return findDatexII223(SituationType.ROAD_WORK, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findTrafficAnnouncements223(final Instant from, final Instant to) {
        return findDatexII223(SituationType.TRAFFIC_ANNOUNCEMENT, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findWeightRestrictions223(final Instant from, final Instant to) {
        return findDatexII223(SituationType.WEIGHT_RESTRICTION, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findExemptedTransports223(final Instant from, final Instant to) {
        return findDatexII223(SituationType.EXEMPTED_TRANSPORT, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findRoadworks35(final Instant from, final Instant to, final Polygon bbox) {
        return findDatexII35(SituationType.ROAD_WORK, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findTrafficAnnouncements35(final Instant from, final Instant to, final Polygon bbox) {
        return findDatexII35(SituationType.TRAFFIC_ANNOUNCEMENT, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findWeightRestrictions35(final Instant from, final Instant to, final Polygon bbox) {
        return findDatexII35(SituationType.WEIGHT_RESTRICTION, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findExemptedTransports35(final Instant from, final Instant to, final Polygon bbox) {
        return findDatexII35(SituationType.EXEMPTED_TRANSPORT, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> findRoadworks37(final Instant from, final Instant to, final Polygon bbox) {
        return findDatexII37(SituationType.ROAD_WORK, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> findTrafficAnnouncements37(final Instant from, final Instant to, final Polygon bbox) {
        return findDatexII37(SituationType.TRAFFIC_ANNOUNCEMENT, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> findWeightRestrictions37(final Instant from, final Instant to, final Polygon bbox) {
        return findDatexII37(SituationType.WEIGHT_RESTRICTION, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> findExemptedTransports37(final Instant from, final Instant to, final Polygon bbox) {
        return findDatexII37(SituationType.EXEMPTED_TRANSPORT, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findTrafficData35(final Instant fromParameter, final Instant toParameter, final boolean srtiOnly) {
        if (!rttiEnabled) {
            log.info("method=findTrafficData35 RTTI publishing disabled, returning empty SituationPublication");
            return toDatexII35Publication(List.of());
        }
        final var from = firstNonNull(fromParameter, defaultFrom());
        final var to = firstNonNull(toParameter, TIME_END);
        final var messages = dataDatex2SituationRepository.findAllTrafficData(from, to, srtiOnly);
        return toDatexII35Publication(messages);
    }

    @Transactional(readOnly = true)
    public Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> findTrafficData37(final Instant fromParameter, final Instant toParameter, final boolean srtiOnly) {
        if (!rttiEnabled) {
            log.info("method=findTrafficData37 RTTI publishing disabled, returning empty SituationPublication");
            return toDatexII37Publication(List.of());
        }
        final var from = firstNonNull(fromParameter, defaultFrom());
        final var to = firstNonNull(toParameter, TIME_END);
        final var messages = dataDatex2SituationRepository.findAllTrafficData(from, to, srtiOnly);
        return toDatexII37Publication(messages);
    }

    private Instant getMaxModified(final List<? extends ModifiedAt> messages) {
        final var maxModifiedAt = messages.stream()
                .map(ModifiedAt::getModifiedAt)
                .max(Instant::compareTo);

        return maxModifiedAt.orElse(Instant.now());
    }

    private Pair<D2LogicalModel, Instant> toDatexII223Publication(final List<MessageAndModified> messages) {
        final var maxModifiedAt = getMaxModified(messages);
        return Pair.of(datexII223Converter.createD2LogicalModel(messages), maxModifiedAt);
    }

    private Pair<SituationPublication, Instant> toDatexII35Publication(final List<MessageAndModified> messages) {
        final var maxModifiedAt = getMaxModified(messages);
        try {
            return Pair.of(datexII35Converter.createPublication(messages), maxModifiedAt);
        } catch (final Exception e) {
            log.error("Error creating Datex II 3.5 publication", e);
            throw e;
        }
    }

    private Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> toDatexII37Publication(final List<MessageAndModified> messages) {
        final var maxModifiedAt = getMaxModified(messages);
        try {
            return Pair.of(datexII37Converter.createPublication(messages), maxModifiedAt);
        } catch (final Exception e) {
            log.error("Error creating Datex II 3.7 publication", e);
            throw e;
        }
    }

    private Pair<D2LogicalModel, Instant> findDatexII223(final SituationType situationType, final Instant fromParameter, final Instant toParameter) {
        final var from = firstNonNull(fromParameter, defaultFrom());
        final var to = firstNonNull(toParameter, TIME_END);
        final var messages = dataDatex2SituationRepository.findMessagesByType(situationType.name(), from, to, null, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_2_2_3.version);
        return toDatexII223Publication(messages);
    }

    private Pair<SituationPublication, Instant> findDatexII35(final SituationType situationType, final Instant fromParameter, final Instant toParameter, final Polygon bbox) {
        final var from = firstNonNull(fromParameter, defaultFrom());
        final var to = firstNonNull(toParameter, TIME_END);
        final var messages = dataDatex2SituationRepository.findMessagesByType(situationType.name(), from, to, bbox, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);
        return toDatexII35Publication(messages);
    }

    private Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> findDatexII37(final SituationType situationType, final Instant fromParameter, final Instant toParameter, final Polygon bbox) {
        final var from = firstNonNull(fromParameter, defaultFrom());
        final var to = firstNonNull(toParameter, TIME_END);
        final var messages = dataDatex2SituationRepository.findMessagesByType(situationType.name(), from, to, bbox, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_7.version);
        return toDatexII37Publication(messages);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findLatestTrafficDataMessage(final String situationId, final boolean latestOnly) {
        if (!rttiEnabled) {
            throw new ObjectNotFoundException("Traffic data message", situationId);
        }
        final var messages = latestOnly
                           ? dataDatex2SituationRepository.findLatestTrafficDataMessageBySituationId(situationId)
                           : dataDatex2SituationRepository.findTrafficDataMessagesBySituationId(situationId);

        if (messages.isEmpty()) {
            throw new ObjectNotFoundException("Traffic data message", situationId);
        }

        return toDatexII35Publication(messages);
    }

    @Transactional(readOnly = true)
    public Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> findLatestTrafficDataMessage37(final String situationId, final boolean latestOnly) {
        if (!rttiEnabled) {
            throw new ObjectNotFoundException("Traffic data message", situationId);
        }
        final var messages = latestOnly
                           ? dataDatex2SituationRepository.findLatestTrafficDataMessageBySituationId(situationId)
                           : dataDatex2SituationRepository.findTrafficDataMessagesBySituationId(situationId);

        if (messages.isEmpty()) {
            throw new ObjectNotFoundException("Traffic data message", situationId);
        }

        return toDatexII37Publication(messages);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findRoadworks(final Instant from, final Instant to, final Polygon bbox) {
        return findSimppeli(SituationType.ROAD_WORK, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findTrafficAnnouncements(final Instant from, final Instant to, final Polygon bbox) {
        return findSimppeli(SituationType.TRAFFIC_ANNOUNCEMENT, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findWeightRestrictions(final Instant from, final Instant to, final Polygon bbox) {
        return findSimppeli(SituationType.WEIGHT_RESTRICTION, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findExemptedTransports(final Instant from, final Instant to, final Polygon bbox) {
        return findSimppeli(SituationType.EXEMPTED_TRANSPORT, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findSimppeliSituations(final String situationId, final boolean latestOnly, final boolean includeAreaGeometry) {
        final var messages = latestOnly
                ? dataDatex2SituationRepository.findLatestMessagesBySituationId(situationId, MessageTypeEnum.SIMPPELI.value(), null)
                : dataDatex2SituationRepository.findAllMessagesBySituationId(situationId, MessageTypeEnum.SIMPPELI.value(), null);

        if (messages.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        return convertSimppeli(messages, includeAreaGeometry);
    }

    private TrafficAnnouncementFeatureCollection findSimppeli(final SituationType situationType, final Instant fromParameter, final Instant toParameter, final Polygon bbox) {
        final var from = firstNonNull(fromParameter, defaultFrom());
        final var to = firstNonNull(toParameter, TIME_END);
        final var messages = dataDatex2SituationRepository.findMessagesByType(situationType.name(), from, to, bbox, MessageTypeEnum.SIMPPELI.value(), null);
        return convertSimppeli(messages, true);
    }

    private TrafficAnnouncementFeatureCollection convertSimppeli(final List<MessageAndModified> messages, final boolean includeAreaGeometry) {
        final var maxModifiedAt = getMaxModified(messages);

        final var features = messages.stream()
                .map(m -> {
                    try {
                        return messageConverter.convertToFeature(m.getMessage(), includeAreaGeometry);
                    } catch (final Exception e) {
                        log.error("method=convertSimppeli Failed to convert message id={}", m.getMessageId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(f -> f.getProperties() != null)
                .sorted(Comparator.comparing((TrafficAnnouncementFeature f) -> f.getProperties().releaseTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return new TrafficAnnouncementFeatureCollection(maxModifiedAt, features);
    }


    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findDatexII223Situations(final String situationId, final boolean latestOnly) {
        final var messages = latestOnly
                ? dataDatex2SituationRepository.findLatestMessagesBySituationId(situationId, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_2_2_3.version)
                : dataDatex2SituationRepository.findAllMessagesBySituationId(situationId, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_2_2_3.version);

        if (messages.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        final var model = toDatexII223Publication(messages);

        if (((fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication) model.getLeft().getPayloadPublication()).getSituations().isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        return model;
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findDatexII35Situations(final String situationId, final boolean latestOnly) {
        final var messages = latestOnly
                ? dataDatex2SituationRepository.findLatestMessagesBySituationId(situationId, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version)
                : dataDatex2SituationRepository.findAllMessagesBySituationId(situationId, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        if (messages.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        return toDatexII35Publication(messages);
    }

    @Transactional(readOnly = true)
    public Pair<fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication, Instant> findDatexII37Situations(final String situationId, final boolean latestOnly) {
        final var messages = latestOnly
                ? dataDatex2SituationRepository.findLatestMessagesBySituationId(situationId, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_7.version)
                : dataDatex2SituationRepository.findAllMessagesBySituationId(situationId, MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_7.version);

        if (messages.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        return toDatexII37Publication(messages);
    }
}
