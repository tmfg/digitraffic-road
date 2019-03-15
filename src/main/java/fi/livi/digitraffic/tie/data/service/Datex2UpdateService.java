package fi.livi.digitraffic.tie.data.service;

import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.TRAFFIC_DISORDER;
import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.WEIGHT_RESTRICTION;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;
import fi.livi.digitraffic.tie.data.model.Datex2Situation;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecord;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecordValidyStatus;
import fi.livi.digitraffic.tie.data.model.SituationRecordCommentI18n;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Comment;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualString;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.OverallPeriod;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PayloadPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Situation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Validity;

@Service
public class Datex2UpdateService {
    private final Datex2Repository datex2Repository;

    private static final Logger log = LoggerFactory.getLogger(Datex2UpdateService.class);

    public Datex2UpdateService(final Datex2Repository datex2Repository) {
        this.datex2Repository = datex2Repository;
    }

    public Map<String, ZonedDateTime> listSituationVersionTimes(final Datex2MessageType messageType) {
        final Map<String, ZonedDateTime> map = new HashMap<>();

        for (final Object[] o : datex2Repository.listDatex2SituationVersionTimes(messageType.name())) {
            final String situationId = (String) o[0];
            final ZonedDateTime versionTime = DateHelper.toZonedDateTimeAtUtc(((Timestamp)o[1]).toInstant());

            if (map.put(situationId, versionTime) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        return map;
    }

    public int updateTrafficAlerts(final List<Datex2MessageDto> data) {
        return updateDatex2Data(data, TRAFFIC_DISORDER);
    }

    @Transactional
    public void updateRoadworks(final List<Datex2MessageDto> messages) {
        updateDatex2Data(messages, ROADWORK);
    }

    @Transactional
    public int updateWeightRestrictions(final List<Datex2MessageDto> data) {
        return updateDatex2Data(data, WEIGHT_RESTRICTION);
    }

    private int updateDatex2Data(final List<Datex2MessageDto> data, final Datex2MessageType messageType) {
        for (final Datex2MessageDto message : data) {
            final Datex2 datex2 = new Datex2();

            if (message.importTime != null) {
                datex2.setImportTime(message.importTime);
            } else {
                datex2.setImportTime(ZonedDateTime.now());
            }
            datex2.setMessage(message.message);
            datex2.setMessageType(messageType);

            final D2LogicalModel datex = message.model;
            parseAndAppendPayloadPublicationData(datex.getPayloadPublication(), datex2);
            datex2Repository.save(datex2);
        }

        return data.size();
    }

    private static void parseAndAppendPayloadPublicationData(final PayloadPublication payloadPublication, final Datex2 datex2) {
        datex2.setPublicationTime(DateHelper.toZonedDateTimeWithoutMillis(payloadPublication.getPublicationTime()));
        if (payloadPublication instanceof SituationPublication) {
            parseAndAppendSituationPublicationData((SituationPublication) payloadPublication, datex2);
        } else {
            log.error("Not implemented handling for Datex2 PayloadPublication type " + payloadPublication.getClass());
        }
    }

    private static void parseAndAppendSituationPublicationData(final SituationPublication situationPublication, final Datex2 datex2) {
        final List<Situation> situations = situationPublication.getSituation();
        for (final Situation situation : situations) {
            final Datex2Situation d2Situation = new Datex2Situation();

            datex2.addSituation(d2Situation);

            d2Situation.setSituationId(situation.getId());
            d2Situation.setVersionTime(DateHelper.toZonedDateTimeWithoutMillis(situation.getSituationVersionTime()));

            parseAndAppendSituationRecordData(situation.getSituationRecord(), d2Situation);
        }
    }

    private static void parseAndAppendSituationRecordData(final List<SituationRecord> situationRecords, final Datex2Situation d2Situation) {
        for (final SituationRecord record : situationRecords) {
            final Datex2SituationRecord d2SituationRecord = new Datex2SituationRecord();

            d2Situation.addSituationRecord(d2SituationRecord);
            d2SituationRecord.setType(Datex2SituationRecordType.fromRecord(record.getClass()));

            // Only first comment seems to be valid
            final List<Comment> pc = record.getGeneralPublicComment();
            if (pc != null && !pc.isEmpty()) {
                final Comment comment = pc.get(0);
                final MultilingualString.Values values = comment.getComment().getValues();
                final List<SituationRecordCommentI18n> comments = joinComments(values.getValue());
                d2SituationRecord.setPublicComments(comments);
            }

            d2SituationRecord.setSituationRecordId(record.getId());
            d2SituationRecord.setCreationTime(DateHelper.toZonedDateTimeWithoutMillis(record.getSituationRecordCreationTime()));
            d2SituationRecord.setVersionTime(DateHelper.toZonedDateTimeWithoutMillis(record.getSituationRecordVersionTime()));
            d2SituationRecord.setObservationTime(DateHelper.toZonedDateTimeWithoutMillis(record.getSituationRecordObservationTime()));

            final Validity validy = record.getValidity();
            d2SituationRecord.setValidyStatus(Datex2SituationRecordValidyStatus.fromValue(validy.getValidityStatus().name()));
            final OverallPeriod period = validy.getValidityTimeSpecification();
            d2SituationRecord.setOverallStartTime(DateHelper.toZonedDateTimeWithoutMillis(period.getOverallStartTime()));
            d2SituationRecord.setOverallEndTime(DateHelper.toZonedDateTimeWithoutMillis(period.getOverallEndTime()));
        }
    }

    /**
     * Joins comments of same language as one comment
     * @param value
     * @return
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

        return langToCommentMap.entrySet().stream()
            .filter(a -> !StringUtils.isBlank(a.getValue().getValue()) )
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }
}
