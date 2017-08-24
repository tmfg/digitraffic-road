package fi.livi.digitraffic.tie.data.service;

import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.TRAFFIC_DISORDER;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.dto.datex2.Datex2RootDataObjectDto;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;
import fi.livi.digitraffic.tie.data.model.Datex2Situation;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecord;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecordValidyStatus;
import fi.livi.digitraffic.tie.data.model.SituationRecordCommentI18n;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.data.service.datex2.StringToObjectMarshaller;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Comment;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualString;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.ObservationTimeType;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.OverallPeriod;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PayloadPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Situation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TimestampedTrafficDisorderDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Validity;

@Service
public class Datex2DataService {
    private static final Logger log = LoggerFactory.getLogger(Datex2DataService.class);

    private final Datex2Repository datex2Repository;
    private final StringToObjectMarshaller stringToObjectMarshaller;

    @Autowired
    public Datex2DataService(final Datex2Repository datex2Repository, final StringToObjectMarshaller stringToObjectMarshaller) {
        this.datex2Repository = datex2Repository;
        this.stringToObjectMarshaller = stringToObjectMarshaller;
    }

    @Transactional
    public void updateRoadworks(final List<Datex2MessageDto> messages) {
        updateDatex2Data(messages, ROADWORK);
    }

    public Map<String, LocalDateTime> listRoadworkSituationVersionTimes() {
        final Map<String, LocalDateTime> map = new HashMap<>();

        for (final Object[] o : datex2Repository.listRoadworkSituationVersionTimes()) {
            final String situationId = (String) o[0];
            final LocalDateTime versionTime = ((Timestamp)o[1]).toLocalDateTime();

            if (map.put(situationId, versionTime) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }

        return map;
    }

    public int updateTrafficAlerts(final List<Datex2MessageDto> data) {
        return updateDatex2Data(data, TRAFFIC_DISORDER);
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

    @Transactional(readOnly = true)
    public Datex2RootDataObjectDto findDatex2TrafficAlerts(final String situationId, final int year, final int month) {
        if (situationId != null && !datex2Repository.existsWithSituationId(situationId)) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        final ZonedDateTime updated = getLatestImportTime(TRAFFIC_DISORDER);

        return new Datex2RootDataObjectDto(
                situationId != null ? datex2Repository.findHistoryBySituationId(situationId, year, month) : datex2Repository.findHistory
                    (TRAFFIC_DISORDER.name(), year, month),
                updated);
    }

    @Transactional(readOnly = true)
    public Datex2RootDataObjectDto findActiveDatex2TrafficDisorders(final boolean onlyUpdateInfo) {
        final ZonedDateTime updated = getLatestImportTime(TRAFFIC_DISORDER);

        if (onlyUpdateInfo) {
            return new Datex2RootDataObjectDto(updated);
        } else {
            return new Datex2RootDataObjectDto(
                    datex2Repository.findAllActive(TRAFFIC_DISORDER.name()),
                    updated);
        }
    }

    public Datex2RootDataObjectDto findAllDatex2TrafficDisordersBySituationId(final String situationId) {
        final ZonedDateTime updated = getLatestImportTime(TRAFFIC_DISORDER);

        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndMessageType(situationId, TRAFFIC_DISORDER.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return new Datex2RootDataObjectDto(
                datex2s,
                updated);

    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findDatex2Responses(final Datex2MessageType messageType, final String situationId,
        final int year, final int month) {
        if (situationId != null && !datex2Repository.existsWithSituationId(situationId)) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        final List<Datex2> datex2s =
                situationId != null ? datex2Repository.findHistory(situationId, year, month) : datex2Repository.findHistory(messageType.name(),
                    year, month);
        return convertToTrafficDisordersDatex2Response(datex2s);
    }

    public ZonedDateTime getLatestImportTime(final Datex2MessageType messageType) {
        return DateHelper.toZonedDateTime(datex2Repository.findLatestImportTime(messageType.name()));
    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findAllDatex2ResponsesBySituationId(final Datex2MessageType messageType, final String
        situationId) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndMessageType(situationId, messageType.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToTrafficDisordersDatex2Response(datex2s);
    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findActiveDatex2TrafficDisorders() {
        final List<Datex2> allActive = datex2Repository.findAllActive(TRAFFIC_DISORDER.name());
        return convertToTrafficDisordersDatex2Response(allActive);
    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findActiveDatex2Roadworks() {
        final List<Datex2> allActive = datex2Repository.findAllActive(ROADWORK.name());
        return convertToTrafficDisordersDatex2Response(allActive);
    }

    private TrafficDisordersDatex2Response convertToTrafficDisordersDatex2Response(final List<Datex2> datex2s) {
        final List<TimestampedTrafficDisorderDatex2> timestampedTrafficDisorderDatex2s = new ArrayList<>();
        for (final Datex2 datex2 : datex2s) {
            final String datex2Xml = datex2.getMessage();
            if (!StringUtils.isBlank(datex2Xml)) {
                final TimestampedTrafficDisorderDatex2 tsDatex2 = unMarshallDatex2Message(datex2Xml, datex2.getImportTime());
                if (tsDatex2 != null) {
                    timestampedTrafficDisorderDatex2s.add(tsDatex2);
                }
            }
        }
        return new TrafficDisordersDatex2Response().withDisorder(timestampedTrafficDisorderDatex2s);
    }

    public TimestampedTrafficDisorderDatex2 unMarshallDatex2Message(final String datex2Xml, final ZonedDateTime importTime) {
        try {
            final D2LogicalModel d2LogicalModel = stringToObjectMarshaller.convertToObject(datex2Xml);
            final ObservationTimeType published =
                    new ObservationTimeType()
                            .withLocaltime(DateHelper.toXMLGregorianCalendar(importTime))
                            .withUtc(DateHelper.toXMLGregorianCalendarUtc(importTime));
            final TimestampedTrafficDisorderDatex2 tsDatex2 =
                    new TimestampedTrafficDisorderDatex2()
                            .withD2LogicalModel(d2LogicalModel)
                            .withPublished(published);
            return tsDatex2;
        } catch (final XmlMappingException e) {
            log.error("Failed to unmarshal datex2 message: " + datex2Xml, e);
        }

        return null;
    }
}
