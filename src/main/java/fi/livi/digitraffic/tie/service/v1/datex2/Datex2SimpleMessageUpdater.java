package fi.livi.digitraffic.tie.service.v1.datex2;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.external.tloik.ims.ImsMessage;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;

@Service
public class Datex2SimpleMessageUpdater {
    private final Datex2WeightRestrictionsHttpClient datex2WeightRestrictionsHttpClient;
    private final Datex2RoadworksHttpClient datex2RoadworksHttpClient;

    private final Datex2TrafficAlertHttpClient datex2TrafficAlertHttpClient;
    private final Datex2UpdateService datex2UpdateService;

    private final Datex2Repository datex2Repository;

    private static StringToObjectMarshaller<D2LogicalModel> stringToObjectMarshaller;

    private static final Logger log = LoggerFactory.getLogger(Datex2SimpleMessageUpdater.class);

    @Autowired
    public Datex2SimpleMessageUpdater(final Datex2WeightRestrictionsHttpClient datex2WeightRestrictionsHttpClient,
                                      final Datex2RoadworksHttpClient datex2RoadworksHttpClient,
                                      final Datex2TrafficAlertHttpClient datex2TrafficAlertHttpClient,
                                      final Datex2UpdateService datex2UpdateService,
                                      final Datex2Repository datex2Repository,
                                      final StringToObjectMarshaller stringToObjectMarshaller) {
        this.datex2WeightRestrictionsHttpClient = datex2WeightRestrictionsHttpClient;
        this.datex2RoadworksHttpClient = datex2RoadworksHttpClient;
        this.datex2TrafficAlertHttpClient = datex2TrafficAlertHttpClient;
        this.datex2UpdateService = datex2UpdateService;
        this.datex2Repository = datex2Repository;
        Datex2SimpleMessageUpdater.stringToObjectMarshaller = stringToObjectMarshaller;
    }

    @Transactional
    public int updateTrafficIncidentImsMessages(final List<ImsMessage> imsMessages) {
        return imsMessages.stream()
            .map(ims -> ims.getMessageContent().getD2Message())
            .map(d2Xml -> stringToObjectMarshaller.convertToObject(d2Xml))
            .map(d2 -> createModels(d2, Datex2MessageType.TRAFFIC_INCIDENT, null))
            .mapToInt(datex2UpdateService::updateTrafficAlerts).sum();
    }

    @Transactional
    public int updateDatex2TrafficAlertMessages() {
        final Instant latest = datex2Repository.findLatestImportTime(Datex2MessageType.TRAFFIC_INCIDENT.name());
        final List<Pair<String, Instant>> messages = datex2TrafficAlertHttpClient.getTrafficAlertMessages(latest);

        final List<Datex2MessageDto> unmarshalled = convert(messages);
        return datex2UpdateService.updateTrafficAlerts(unmarshalled);
    }

    private List<Datex2MessageDto> convert(List<Pair<String, Instant>> messages) {
        return messages.stream()
            .map(m -> convert(m.getLeft(), Datex2MessageType.TRAFFIC_INCIDENT, DateHelper.toZonedDateTimeAtUtc(m.getRight())))
            .flatMap(m -> m.stream())
            .collect(Collectors.toList());
    }

    @Transactional
    public void updateDatex2RoadworksMessages() {
        final String message = datex2RoadworksHttpClient.getRoadWorksMessage();

        datex2UpdateService.updateRoadworks(convert(message, Datex2MessageType.ROADWORK, null));
    }

    @Transactional
    public void updateDatex2WeightRestrictionMessages() {
        final String message = datex2WeightRestrictionsHttpClient.getWeightRestrictionsMessage();

        datex2UpdateService.updateWeightRestrictions(convert(message, Datex2MessageType.WEIGHT_RESTRICTION, null));
    }

    @Transactional(readOnly = true)
    public List<Datex2MessageDto> convert(final String message, final Datex2MessageType messageType, final ZonedDateTime importTime) {
        final D2LogicalModel model = stringToObjectMarshaller.convertToObject(message);

        return createModels(model, messageType, importTime);
    }

    private List<Datex2MessageDto> createModels(final D2LogicalModel main, final Datex2MessageType messageType, final ZonedDateTime importTime) {
        final SituationPublication sp = (SituationPublication) main.getPayloadPublication();

        final Map<String, ZonedDateTime> versionTimes = datex2UpdateService.listSituationVersionTimes(messageType);
        final long updatedCount = sp.getSituations().stream().filter(s -> versionTimes.get(s.getId()) != null &&
                                                                    isNewOrUpdatedSituation(versionTimes.get(s.getId()), s)).count();
        final long newCount = sp.getSituations().stream().filter(s -> versionTimes.get(s.getId()) == null).count();

        log.info("situations.updated={} situations.new={}", updatedCount, newCount);

        return sp.getSituations().stream()
            .filter(s -> isNewOrUpdatedSituation(versionTimes.get(s.getId()), s))
            .map(s -> convert(main, sp, s, importTime))
            .collect(Collectors.toList());
    }

    private static boolean isNewOrUpdatedSituation(final ZonedDateTime latestVersionTime, final Situation situation) {
        // does any record have new version time?
        return situation.getSituationRecords().stream().anyMatch(r -> isNewOrUpdatedRecord(latestVersionTime, r));
    }

    private static boolean isNewOrUpdatedRecord(final ZonedDateTime latestVersionTime, final SituationRecord record) {
        // different resolution, so remove fractions of second
        final Instant vTime = DateHelper.withoutMillis(record.getSituationRecordVersionTime());
        return latestVersionTime == null || vTime.isAfter(DateHelper.withoutMillis(latestVersionTime.toInstant()) );
    }

    private Datex2MessageDto convert(final D2LogicalModel main, final SituationPublication sp,
                                     final Situation situation, final ZonedDateTime importTime) {
        final D2LogicalModel d2 = new D2LogicalModel();
        final SituationPublication newSp = new SituationPublication();

        newSp.setPublicationTime(sp.getPublicationTime());
        newSp.setPublicationCreator(sp.getPublicationCreator());
        newSp.setLang(sp.getLang());
        newSp.withSituations(situation);

        d2.setModelBaseVersion(main.getModelBaseVersion());
        d2.setExchange(main.getExchange());
        d2.setPayloadPublication(newSp);

        return new Datex2MessageDto(stringToObjectMarshaller.convertToString(d2), importTime, d2);
    }
}
