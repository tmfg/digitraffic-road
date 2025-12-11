package fi.livi.digitraffic.tie.service.data;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.controller.trafficmessage.MessageConverter;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;

import fi.livi.digitraffic.tie.model.ModifiedAt;
import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;

import fi.livi.digitraffic.tie.model.data.MessageAndModified;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v3_5.SituationPublication;
import fi.livi.digitraffic.tie.model.data.DataDatex2SituationMessage;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@Service
public class Datex2Service {
    private final DataDatex2SituationRepository dataDatex2SituationRepository;
    private final DatexII35Converter datexII35Converter;
    private final DatexII223Converter datexII223Converter;

    private final MessageConverter messageConverter;

    private static final Logger log = LoggerFactory.getLogger(Datex2Service.class);

    private static final Instant TIME_END = Instant.ofEpochMilli(32503683600000L);

    private static final String HISTORY_JSON_TEMPLATE = "[%s]";
    private static final String FEATURE_COLLECTION_TEMPLATE = """
    {
        "type": "FeatureCollection",
        "features": [
        %s
        ]
    }""";

    public Datex2Service(final DataDatex2SituationRepository dataDatex2SituationRepository,
                         final DatexII35Converter datexII35Converter, final DatexII223Converter datexII223Converter,
                         final MessageConverter messageConverter) {
        this.dataDatex2SituationRepository = dataDatex2SituationRepository;
        this.datexII35Converter = datexII35Converter;
        this.datexII223Converter = datexII223Converter;
        this.messageConverter = messageConverter;
    }

    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findRoadworks223(final Instant from, final Instant to) {
        return findDatexII223(SituationType.ROAD_WORK, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<String, Instant> findRoadworks(final Instant from, final Instant to, final Polygon bbox) {
        return findSimppeli(SituationType.ROAD_WORK, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<String, Instant> findTrafficAnnouncements(final Instant from, final Instant to, final Polygon bbox) {
        return findSimppeli(SituationType.TRAFFIC_ANNOUNCEMENT, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<String, Instant> findWeightRestrictions(final Instant from, final Instant to, final Polygon bbox) {
        return findSimppeli(SituationType.WEIGHT_RESTRICTION, from, to, bbox);
    }

    @Transactional(readOnly = true)
    public Pair<String, Instant> findExemptedTransports(final Instant from, final Instant to, final Polygon bbox) {
        return findSimppeli(SituationType.EXEMPTED_TRANSPORT, from, to, bbox);
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
    public Pair<SituationPublication, Instant> findRoadworks35(final Instant from, final Instant to) {
        return findDatexII35(SituationType.ROAD_WORK, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findTrafficAnnouncements35(final Instant from, final Instant to) {
        return findDatexII35(SituationType.TRAFFIC_ANNOUNCEMENT, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findWeightRestrictions35(final Instant from, final Instant to) {
        return findDatexII35(SituationType.WEIGHT_RESTRICTION, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findExemptedTransports35(final Instant from, final Instant to) {
        return findDatexII35(SituationType.EXEMPTED_TRANSPORT, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findTrafficData35(final Instant fromParameter, final Instant toParameter, final boolean srtiOnly) {
        final var from = ObjectUtils.firstNonNull(fromParameter, Instant.now());
        final var to = ObjectUtils.firstNonNull(toParameter, TIME_END);

        final var messages = dataDatex2SituationRepository.findAllTrafficData(from, to, srtiOnly);
        final var messageData = messages.stream().map(MessageAndModified::getMessage).toList();
        final var maxModified = getMaxModified(messages);

        try {
            return Pair.of(datexII35Converter.createPublication(messageData), maxModified);
        } catch(final Exception e) {
            log.error("Error creating publication", e);

            throw e;
        }
    }

    private Instant getMaxModified(final List<? extends ModifiedAt> messages) {
        final var maxModifiedAt = messages.stream()
                .map(ModifiedAt::getModifiedAt)
                .max(Instant::compareTo);

        return maxModifiedAt.orElse(Instant.now());
    }

    private Pair<String, Instant> convertSimppeli(final List<DataDatex2Situation> situations, final boolean createFeatureCollection, final boolean includeAreaGeometry) {
        final var messages = situations.stream()
                .flatMap(s -> s.getMessages().stream())
                .filter(m -> m.getMessageType().equals(MessageTypeEnum.SIMPPELI.value()))
                .toList();

        final var messageData = messages.stream()
                .map(DataDatex2SituationMessage::getMessage)
                .map(m -> includeAreaGeometry ? m : messageConverter.removeAreaGeometrySafe(m))
                .toList();

        final var maxModifiedAt = getMaxModified(messages);
        final var template = createFeatureCollection ? FEATURE_COLLECTION_TEMPLATE : HISTORY_JSON_TEMPLATE;

        return Pair.of(String.format(template, String.join(",", messageData)),  maxModifiedAt);
    }

    private Pair<D2LogicalModel, Instant> convertDatexII223(final List<DataDatex2Situation> situations) {
        final var messages = situations.stream()
                .flatMap(s -> s.getMessages().stream())
                .filter(m -> m.getMessageType().equals(MessageTypeEnum.DATEX_2.value()))
                .filter(m -> m.getMessageVersion().equals(Datex2Version.V_2_2_3.version))
                .toList();

        final var maxModifiedAt = getMaxModified(messages);

        return Pair.of(datexII223Converter.createD2LogicalModel(messages), maxModifiedAt);
    }

    private Pair<SituationPublication, Instant> convertDatexII35(final List<DataDatex2Situation> situations) {
        final var messages = situations.stream()
                .flatMap(s -> s.getMessages().stream())
                .filter(m -> m.getMessageType().equals(MessageTypeEnum.DATEX_2.value()))
                .filter(m -> m.getMessageVersion().equals(Datex2Version.V_3_5.version))
                .toList();

        final var messageData = messages.stream()
                .map(DataDatex2SituationMessage::getMessage)
                .toList();

        final var maxModifiedAt = getMaxModified(messages);

        try {
            return Pair.of(datexII35Converter.createPublication(messageData), maxModifiedAt);
        } catch(final Exception e) {
            log.error("Error creating publication", e);

            throw e;
        }
    }

    private Pair<String, Instant> findSimppeli(final SituationType situationType, final Instant fromParameter, final Instant toParameter, final Polygon bbox) {
        final var from = ObjectUtils.firstNonNull(fromParameter, Instant.now());
        final var to = ObjectUtils.firstNonNull(toParameter, TIME_END);

        final var datex2SituationIds = dataDatex2SituationRepository.findLatestByType(situationType.name(), from, to, bbox);
        final var situations = dataDatex2SituationRepository.findAllById(datex2SituationIds);

        return convertSimppeli(situations, true, true);
    }

    private Pair<D2LogicalModel, Instant> findDatexII223(final SituationType situationType, final Instant fromParameter, final Instant toParameter) {
        final var from = ObjectUtils.firstNonNull(fromParameter, Instant.now());
        final var to = ObjectUtils.firstNonNull(toParameter, TIME_END);

        final var datex2SituationIds = dataDatex2SituationRepository.findLatestByType(situationType.name(), from, to);
        final var situations = dataDatex2SituationRepository.findAllById(datex2SituationIds);

        return convertDatexII223(situations);
    }

    private Pair<SituationPublication, Instant> findDatexII35(final SituationType situationType, final Instant fromParameter, final Instant toParameter) {
        final var from = ObjectUtils.firstNonNull(fromParameter, Instant.now());
        final var to = ObjectUtils.firstNonNull(toParameter, TIME_END);

        final var datex2SituationIds = dataDatex2SituationRepository.findLatestByType(situationType.name(), from, to);
        final var situations = dataDatex2SituationRepository.findAllById(datex2SituationIds);

        return convertDatexII35(situations);
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findLatestTrafficDataMessage(final String situationId, final boolean latestOnly) {
        final var messages = latestOnly
                           ? dataDatex2SituationRepository.findLatestTrafficDataMessageBySituationId(situationId)
                           : dataDatex2SituationRepository.findTrafficDataMessagesBySituationId(situationId);

        if(messages.isEmpty()) {
            throw new ObjectNotFoundException("Traffic data message", situationId);
        }

        final var messageData = messages.stream()
                .map(MessageAndModified::getMessage)
                .toList();

        final var maxModifiedAt = getMaxModified(messages);

        return Pair.of(datexII35Converter.createPublication(messageData), maxModifiedAt);
    }

    @Transactional(readOnly = true)
    public Pair<String, Instant> findSimppeliSituations(final String situationId, final boolean latestOnly, final boolean includeAreaGeometry) {
        final var situations = getSituations(situationId, latestOnly);

        return convertSimppeli(situations, latestOnly, includeAreaGeometry);
    }

    private List<DataDatex2Situation> getSituations(final String situationId, final boolean latestOnly) {
        final var datex2Ids = latestOnly ? dataDatex2SituationRepository.findLatestSituationBySituationId(situationId)
                                         : dataDatex2SituationRepository.findAllBySituationId(situationId);

        if(datex2Ids.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        final var situations = dataDatex2SituationRepository.findAllById(datex2Ids);

        if(situations.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        return situations;
    }

    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findDatexII223Situations(final String situationId, final boolean latestOnly) {
        final var situations = getSituations(situationId, latestOnly);
        final var model = convertDatexII223(situations);

        if(((fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication)model.getLeft().getPayloadPublication()).getSituations().isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        return model;
    }

    @Transactional(readOnly = true)
    public Pair<SituationPublication, Instant> findDatexII35Situations(final String situationId, final boolean latestOnly) {
        final var situations = getSituations(situationId, latestOnly);
        final var model = convertDatexII35(situations);

        if(model.getLeft().getSituations().isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        return model;
    }
}
