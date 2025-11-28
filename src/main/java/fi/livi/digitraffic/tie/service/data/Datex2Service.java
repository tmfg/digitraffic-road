package fi.livi.digitraffic.tie.service.data;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.controller.trafficmessage.MessageConverter;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;

import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;

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
        return findDatex223(SituationType.ROAD_WORK, from, to);
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
        return findDatex223(SituationType.TRAFFIC_ANNOUNCEMENT, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findWeightRestrictions223(final Instant from, final Instant to) {
        return findDatex223(SituationType.WEIGHT_RESTRICTION, from, to);
    }

    @Transactional(readOnly = true)
    public Pair<D2LogicalModel, Instant> findExemptedTransports223(final Instant from, final Instant to) {
        return findDatex223(SituationType.EXEMPTED_TRANSPORT, from, to);
    }

    @Transactional(readOnly = true)
    public SituationPublication findRoadworks35(final Instant from, final Instant to) {
        return findDatex35(SituationType.ROAD_WORK, from, to);
    }

    @Transactional(readOnly = true)
    public SituationPublication findTrafficAnnouncements35(final Instant from, final Instant to) {
        return findDatex35(SituationType.TRAFFIC_ANNOUNCEMENT, from, to);
    }

    @Transactional(readOnly = true)
    public SituationPublication findWeightRestrictions35(final Instant from, final Instant to) {
        return findDatex35(SituationType.WEIGHT_RESTRICTION, from, to);
    }

    @Transactional(readOnly = true)
    public SituationPublication findExemptedTransports35(final Instant from, final Instant to) {
        return findDatex35(SituationType.EXEMPTED_TRANSPORT, from, to);
    }

    private Pair<String, Instant> convertSimppeli(final List<DataDatex2Situation> situations, final boolean createFeatureCollection) {
        final var messages = situations.stream()
                .flatMap(s -> s.getMessages().stream())
                .filter(m -> m.getMessageType().equals(MessageTypeEnum.SIMPPELI.value()))
                .map(DataDatex2SituationMessage::getMessage)
                .toList();

        final var template = createFeatureCollection ? FEATURE_COLLECTION_TEMPLATE : HISTORY_JSON_TEMPLATE;
        final var response = String.format(template, String.join(",", messages));

        return Pair.of(response, Instant.now());
    }

    private Pair<D2LogicalModel, Instant> convertDatex223(final List<DataDatex2Situation> situations) {
        final var messages = situations.stream()
                .flatMap(s -> s.getMessages().stream())
                .filter(m -> m.getMessageType().equals(MessageTypeEnum.DATEX_2.value()))
                .filter(m -> m.getMessageVersion().equals(Datex2Version.V_2_2_3.version))
                .toList();

        final var lModel = datexII223Converter.createD2LogicalModel(messages);

        return Pair.of(lModel, Instant.now());
    }

    private SituationPublication convertDatex35(final List<DataDatex2Situation> situations) {
        final var messages = situations.stream()
                .flatMap(s -> s.getMessages().stream())
                .filter(m -> m.getMessageType().equals(MessageTypeEnum.DATEX_2.value()))
                .filter(m -> m.getMessageVersion().equals(Datex2Version.V_3_5.version))
                .map(DataDatex2SituationMessage::getMessage)
                .toList();

        try {
            return datexII35Converter.createPublication(messages);
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

        return convertSimppeli(situations, true);
    }

    private Pair<D2LogicalModel, Instant> findDatex223(final SituationType situationType, final Instant fromParameter, final Instant toParameter) {
        final var from = ObjectUtils.firstNonNull(fromParameter, Instant.now());
        final var to = ObjectUtils.firstNonNull(toParameter, TIME_END);

        final var datex2SituationIds = dataDatex2SituationRepository.findLatestByType(situationType.name(), from, to);
        final var situations = dataDatex2SituationRepository.findAllById(datex2SituationIds);

        return convertDatex223(situations);
    }

    private SituationPublication findDatex35(final SituationType situationType, final Instant fromParameter, final Instant toParameter) {
        final var from = ObjectUtils.firstNonNull(fromParameter, Instant.now());
        final var to = ObjectUtils.firstNonNull(toParameter, TIME_END);

        final var datex2SituationIds = dataDatex2SituationRepository.findLatestByType(situationType.name(), from, to);
        final var situations = dataDatex2SituationRepository.findAllById(datex2SituationIds);

        return convertDatex35(situations);
    }

    @Transactional(readOnly = true)
    public Pair<String, Instant> findSituationHistory(final String situationId) {
        final var situations = dataDatex2SituationRepository.findBySituationId(situationId);

        if(situations.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        return convertSimppeli(situations, false);
    }

    @Transactional(readOnly = true)
    public Pair<String, Instant> findLatestSimppeli(final String situationId, final boolean includeAreaGeometry) {
        final var pair = findLatestSituation(situationId, MessageTypeEnum.SIMPPELI, "0.2.17");

        if(!includeAreaGeometry) {
            // ok, remove area geometry

            final var feature = messageConverter.removeAreaGeometrySafe(pair.getLeft().getMessage());
            return Pair.of(feature, pair.getRight());
        }

        return Pair.of(pair.getLeft().getMessage(), pair.getRight());
    }

    @Transactional(readOnly = true)
    public Pair<DataDatex2SituationMessage, Instant> findLatestSituation(final String situationId, final MessageTypeEnum messageType, final String messageVersion) {
        final var datex2Id = dataDatex2SituationRepository.findLatestSituation(situationId);

        if(datex2Id.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        final var situation = dataDatex2SituationRepository.findById(datex2Id.get());

        if(situation.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        final var datex2Message = situation.get().getMessages().stream()
            .filter(m -> m.getMessageType().equals(messageType.value()))
            .filter(m -> m.getMessageVersion().equals(messageVersion))
            .findFirst();

        if(datex2Message.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Traffic message %s %s", messageType.value(), messageVersion), situationId);
        }

        return Pair.of(datex2Message.get(), Instant.now());
    }
}
