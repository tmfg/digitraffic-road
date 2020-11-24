package fi.livi.digitraffic.tie.service.v1.datex2;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

@Service
public class Datex2SimpleMessageUpdater {
    private static final Logger log = LoggerFactory.getLogger(Datex2SimpleMessageUpdater.class);

    private final Datex2WeightRestrictionsHttpClient datex2WeightRestrictionsHttpClient;
    private final Datex2RoadworksHttpClient datex2RoadworksHttpClient;

    private final Datex2TrafficAlertHttpClient datex2TrafficAlertHttpClient;
    private final Datex2UpdateService datex2UpdateService;

    private final Datex2Repository datex2Repository;

    private static Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;

    private V2Datex2UpdateService v2Datex2UpdateService;

    @Autowired
    public Datex2SimpleMessageUpdater(final Datex2WeightRestrictionsHttpClient datex2WeightRestrictionsHttpClient,
                                      final Datex2RoadworksHttpClient datex2RoadworksHttpClient,
                                      final Datex2TrafficAlertHttpClient datex2TrafficAlertHttpClient,
                                      final Datex2UpdateService datex2UpdateService,
                                      final Datex2Repository datex2Repository,
                                      final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller,
                                      final V2Datex2UpdateService v2Datex2UpdateService) {
        this.datex2WeightRestrictionsHttpClient = datex2WeightRestrictionsHttpClient;
        this.datex2RoadworksHttpClient = datex2RoadworksHttpClient;
        this.datex2TrafficAlertHttpClient = datex2TrafficAlertHttpClient;
        this.datex2UpdateService = datex2UpdateService;
        this.datex2Repository = datex2Repository;
        Datex2SimpleMessageUpdater.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;
        this.v2Datex2UpdateService = v2Datex2UpdateService;
    }

    // Log only on warn and error level as normal execution time is 3-4 s.
    @PerformanceMonitor(maxInfoExcecutionTime = 100000)
    @Transactional
    public int updateDatex2TrafficAlertMessages() {
        final Instant latest = datex2Repository.findLatestImportTime(Datex2MessageType.TRAFFIC_INCIDENT.name());
        final List<Pair<String, Instant>> messages = datex2TrafficAlertHttpClient.getTrafficAlertMessages(latest);

        final List<Datex2MessageDto> unmarshalled = convert(messages);
        return datex2UpdateService.updateDatex2Data(unmarshalled);
    }

    private List<Datex2MessageDto> convert(final List<Pair<String, Instant>> messages) {
        return messages.stream()
            .map(m -> convert(m.getLeft(), Datex2MessageType.TRAFFIC_INCIDENT, DateHelper.toZonedDateTimeAtUtc(m.getRight())))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Transactional
    public void updateDatex2RoadworksMessages() {
        final String message = datex2RoadworksHttpClient.getRoadWorksMessage();
        final int updatedOrInserted =
            datex2UpdateService.updateDatex2Data(convert(message, Datex2MessageType.ROADWORK, null));
        log.info("method=updateDatex2RoadworksMessages updated={}", updatedOrInserted);
    }

    @Transactional
    public void updateDatex2WeightRestrictionMessages() {
        final String message = datex2WeightRestrictionsHttpClient.getWeightRestrictionsMessage();
        final int updatedOrInserted =
            datex2UpdateService.updateDatex2Data(convert(message, Datex2MessageType.WEIGHT_RESTRICTION, null));
        log.info("method=updateDatex2WeightRestrictionMessages updated={}", updatedOrInserted);
    }

    @Transactional(readOnly = true)
    public List<Datex2MessageDto> convert(final String message, final Datex2MessageType messageType, final ZonedDateTime importTime) {
        final D2LogicalModel model = datex2XmlStringToObjectMarshaller.convertToObject(message);
        final List<Datex2MessageDto> models = v2Datex2UpdateService.createModels(model, null, importTime);
        models.forEach(m -> {
            // This is for debug for now, to see that automatic message type identification works correctly
            if (messageType != m.messageType.getDatex2MessageType()) {
                log.error("method=convert Wrong Datex2 message type for situationId={} should be {} but was {}", m.situationId, message, m.messageType.getDatex2MessageType());
            }
        });
        return models;
    }
}
