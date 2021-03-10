package fi.livi.digitraffic.tie.service.v2.datex2;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.conf.jms.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.Comment;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.MultilingualString;
import fi.livi.digitraffic.tie.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.datex2.OverallPeriod;
import fi.livi.digitraffic.tie.datex2.PayloadPublication;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.datex2.Validity;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.LoggerHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2Situation;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecord;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecordValidyStatus;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationRecordCommentI18n;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.datex2.Datex2Helper;
import fi.livi.digitraffic.tie.service.datex2.ImsJsonConverterService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;

@ConditionalOnNotWebApplication
@Service
public class V2Datex2UpdateService {
    private static final Logger log = LoggerFactory.getLogger(V2Datex2UpdateService.class);

    private final Datex2Repository datex2Repository;
    private final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;
    private final DataStatusService dataStatusService;
    private final ImsJsonConverterService imsJsonConverterService;

    @Autowired
    public V2Datex2UpdateService(final Datex2Repository datex2Repository,
                                 final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller,
                                 final DataStatusService dataStatusService,
                                 final ImsJsonConverterService imsJsonConverterService) {
        this.datex2Repository = datex2Repository;
        this.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;
        this.dataStatusService = dataStatusService;
        this.imsJsonConverterService = imsJsonConverterService;
    }

    @Transactional
    public int updateTrafficDatex2ImsMessages(final List<ExternalIMSMessage> imsMessages) {
        final ZonedDateTime now = DateHelper.getZonedDateTimeNowAtUtc();
        final int newAndUpdated = imsMessages.stream().mapToInt(imsMessage -> {
            if (log.isDebugEnabled()) {
                log.debug("method=updateTrafficDatex2ImsMessages imsMessage d2Message datex2: {}",
                          LoggerHelper.objectToStringLoggerSafe(imsMessage.getMessageContent().getD2Message()));
            }
            final List<Datex2MessageDto> models = createModels(imsMessage, now);
            return updateTrafficDatex2Messages(models);
        }).sum();
        log.info("method=updateTrafficDatex2ImsMessages updated={} Datex2ImsMessages", newAndUpdated);
        return newAndUpdated;
    }

    @Transactional
    public int updateTrafficDatex2Messages(final List<Datex2MessageDto> imsMessages) {
        return (int)imsMessages.stream()
            .filter(imsMessage -> isNewOrUpdatedSituation(imsMessage.model, imsMessage.situationType))
            .filter(this::updateDatex2Data)
            .count();
    }

    @Transactional
    public int updateDatex2Data(final List<Datex2MessageDto> data) {
        return (int) data.stream().filter(this::updateDatex2Data).count();
    }

    private List<Datex2MessageDto> createModels(final ExternalIMSMessage imsMessage, final ZonedDateTime importTime) {
        final D2LogicalModel d2 = datex2XmlStringToObjectMarshaller.convertToObject(imsMessage.getMessageContent().getD2Message());
        final String jMessage = imsMessage.getMessageContent().getJMessage();
        final SituationPublication sp = Datex2Helper.getSituationPublication(d2);
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> situationIdJsonMap =
            imsJsonConverterService.parseFeatureJsonsFromImsJson(jMessage);
        final Map<String, Situation> situationIdSituationMap = parseDatex2Situations(sp);

        return situationIdJsonMap.entrySet().stream()
            .map(entry -> {
                final String situationId = entry.getKey();
                final Triple<String, SituationType, TrafficAnnouncementType> properties = entry.getValue();
                final String json = properties.getLeft();
                final SituationType situationType = properties.getMiddle();
                final TrafficAnnouncementType trafficAnnouncementType = properties.getRight();
                final Situation situation = situationIdSituationMap.get(situationId);
                if (situationId == null || situationType == null || situation == null || json == null) {
                    log.error(String.format("method=createModels Failed with IMSMessage situationId=%s situationType: %s, json: %b, situation: %b, imsMessage: %s",
                                            situationId, situationType, StringUtils.isBlank(json), situation != null,
                                            LoggerHelper.objectToStringLoggerSafe(ToStringHelper.toStringFull(imsMessage))));
                    return null;
                }
                return convertToDatex2MessageDto(situationType, trafficAnnouncementType, situation, json, importTime, d2, sp);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Map<String, Situation> parseDatex2Situations(final SituationPublication sp) {
        return sp.getSituations().stream()
            .collect(Collectors.toMap(Situation::getId, Function.identity()));
    }

    private String combineJsonsForErroLogging(final Map<String, String> situationIdJsonMap) {
        return String.join("\n\n", situationIdJsonMap.values());
    }

    private boolean isNewOrUpdatedSituation(final D2LogicalModel d2, final SituationType situationType) {
        final SituationPublication sp = Datex2Helper.getSituationPublication(d2);
        final Situation situation = sp.getSituations().get(0);
        final Instant versionTime = findSituationLatestVersionTime(situation.getId(), situationType);
        return Datex2Helper.isNewOrUpdatedSituation(versionTime, situation);
    }

    private Instant findSituationLatestVersionTime(final String situationId, final SituationType situationType) {
        return datex2Repository.findDatex2SituationLatestVersionTime(situationId, situationType.name());
    }

    private Datex2MessageDto convertToDatex2MessageDto(final SituationType situationType, final TrafficAnnouncementType trafficAnnouncementType,
                                                      final Situation situation, final String jsonValue,
                                                      final ZonedDateTime importTime,
                                                      final D2LogicalModel sourceD2, final SituationPublication sourceSituationPublication) {
        final D2LogicalModel d2 = new D2LogicalModel();
        final SituationPublication newSp = new SituationPublication();

        newSp.setPublicationTime(sourceSituationPublication.getPublicationTime());
        newSp.setPublicationCreator(sourceSituationPublication.getPublicationCreator());
        newSp.setLang(sourceSituationPublication.getLang());
        newSp.withSituations(situation);

        d2.setModelBaseVersion(sourceD2.getModelBaseVersion());
        d2.setExchange(sourceD2.getExchange());
        d2.setPayloadPublication(newSp);

        final String messageValue = datex2XmlStringToObjectMarshaller.convertToString(d2);
        return new Datex2MessageDto(d2, situationType, trafficAnnouncementType, messageValue, jsonValue, importTime, situation.getId());
    }

    /* COPIED */

    /**
     *
     * @param message datex2 message
     * @return true if message was new or updated otherwise false
     */
    @Transactional
    public boolean updateDatex2Data(final Datex2MessageDto message) {

        Datex2Helper.checkD2HasOnlyOneSituation(message.model);

        if (isNewOrUpdatedSituation(message.model, message.situationType)) {
            final Datex2 datex2 = new Datex2(message.situationType, message.trafficAnnouncementType);
            final D2LogicalModel d2 = message.model;

            final ZonedDateTime latestVersionTime = getLatestSituationRecordVersionTime(d2);

            datex2.setImportTime(
                Objects.requireNonNullElseGet(message.importTime, () -> latestVersionTime != null ? latestVersionTime : ZonedDateTime.now()));
            datex2.setMessage(message.message);
            datex2.setJsonMessage(message.jsonMessage);
            parseAndAppendPayloadPublicationData(d2.getPayloadPublication(), datex2);
            datex2Repository.save(datex2);
            dataStatusService.updateDataUpdated(DataType.TRAFFIC_MESSAGES_DATA);

            final String situationId = Datex2Helper.getSituationPublication(d2).getSituations().get(0).getId();
            log.info("Update Datex2 situationId={} messageType={} situationType={} trafficAnnouncementType: {} with importTime={}",
                situationId, message.situationType.getDatex2MessageType(), message.situationType, message.trafficAnnouncementType, datex2.getImportTime());
            return true;
        } else {
            log.info("method=updateDatex2Data Not updating situationId={} messageType={} situationType={} trafficAnnouncementType: {} as it is already uptodate", message.situationId, message.situationType.getDatex2MessageType(), message.situationType, message.trafficAnnouncementType);
        }
        return false;
    }

    private ZonedDateTime getLatestSituationRecordVersionTime(final D2LogicalModel d2) {
        final Instant latest = Datex2Helper.getSituationPublication(d2).getSituations().stream()
            .map(s -> s.getSituationRecords().stream()
                .map(SituationRecord::getSituationRecordVersionTime).max(Comparator.naturalOrder()).orElseThrow())
            .max(Comparator.naturalOrder()).orElseThrow();
        return DateHelper.toZonedDateTimeAtUtc(latest);
    }

    private static void parseAndAppendPayloadPublicationData(final PayloadPublication payloadPublication, final Datex2 datex2) {
        datex2.setPublicationTime(DateHelper.toZonedDateTimeAtUtc(payloadPublication.getPublicationTime()));
        if (payloadPublication instanceof SituationPublication) {
            parseAndAppendSituationPublicationData((SituationPublication) payloadPublication, datex2);
        } else {
            log.error("Not implemented handling for Datex2 PayloadPublication type " + payloadPublication.getClass());
        }
    }

    private static void parseAndAppendSituationPublicationData(final SituationPublication situationPublication, final Datex2 datex2) {
        final List<Situation> situations = situationPublication.getSituations();
        for (final Situation situation : situations) {
            final Datex2Situation d2Situation = new Datex2Situation();

            datex2.addSituation(d2Situation);

            d2Situation.setSituationId(situation.getId());
            d2Situation.setVersionTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(situation.getSituationVersionTime()));

            parseAndAppendSituationRecordData(situation.getSituationRecords(), d2Situation);
        }
    }

    private static void parseAndAppendSituationRecordData(final List<SituationRecord> situationRecords, final Datex2Situation d2Situation) {
        for (final SituationRecord record : situationRecords) {
            final Datex2SituationRecord d2SituationRecord = new Datex2SituationRecord();

            d2Situation.addSituationRecord(d2SituationRecord);
            d2SituationRecord.setType(Datex2SituationRecordType.fromRecord(record.getClass()));

            // Only first comment seems to be valid
            final List<Comment> pc = record.getGeneralPublicComments();
            if (pc != null && !pc.isEmpty()) {
                final Comment comment = pc.get(0);
                final MultilingualString.Values values = comment.getComment().getValues();
                final List<SituationRecordCommentI18n> comments = joinComments(values.getValues());
                d2SituationRecord.setPublicComments(comments);
            }

            d2SituationRecord.setSituationRecordId(record.getId());
            d2SituationRecord.setCreationTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(record.getSituationRecordCreationTime()));
            d2SituationRecord.setVersionTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(record.getSituationRecordVersionTime()));
            d2SituationRecord.setObservationTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(record.getSituationRecordObservationTime()));

            final Validity validy = record.getValidity();
            d2SituationRecord.setValidyStatus(Datex2SituationRecordValidyStatus.fromValue(validy.getValidityStatus().name()));
            final OverallPeriod period = validy.getValidityTimeSpecification();
            d2SituationRecord.setOverallStartTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(period.getOverallStartTime()));
            d2SituationRecord.setOverallEndTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(period.getOverallEndTime()));
        }
    }

    /**
     * Joins comments of same language as one comment
     * @param value comments to join
     * @return joined comments
     */
    private static List<SituationRecordCommentI18n> joinComments(final List<MultilingualStringValue> value) {
        if (value == null) {
            return Collections.emptyList();
        }
        final Map<String, SituationRecordCommentI18n> langToCommentMap = new HashMap<>();
        for (final MultilingualStringValue msv : value) {
            final String lang = msv.getLang();
            SituationRecordCommentI18n i18n = langToCommentMap.get(lang);
            if (i18n == null) {
                i18n = new SituationRecordCommentI18n(lang);
                langToCommentMap.put(lang, i18n);
            }
            i18n.setValue(StringUtils.join(i18n.getValue(), msv.getValue()));
        }

        return langToCommentMap.values().stream()
            .filter(situationRecordCommentI18n -> !StringUtils.isBlank(situationRecordCommentI18n.getValue()))
            .collect(Collectors.toList());
    }
}
