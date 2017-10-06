package fi.livi.digitraffic.tie.data.service;

import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.TRAFFIC_DISORDER;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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
import fi.livi.digitraffic.tie.data.service.datex2.StringToObjectMarshaller;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.ObservationTimeType;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.RoadworksDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TimestampedRoadworkDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TimestampedTrafficDisorderDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;

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

    @Transactional(readOnly = true)
    public Datex2RootDataObjectDto findActiveTrafficDisorders(final boolean onlyUpdateInfo) {
        final ZonedDateTime updated = findLatestImportTime(TRAFFIC_DISORDER);

        if (onlyUpdateInfo) {
            return new Datex2RootDataObjectDto(updated);
        } else {
            return new Datex2RootDataObjectDto(
                    datex2Repository.findAllActive(TRAFFIC_DISORDER.name()),
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public RoadworksDatex2Response findRoadworks(final String situationId, final int year, final int month) {
        final List<Datex2> datex2s = findDatex2Messages(ROADWORK, situationId, year, month);

        return convertToRoadworksDatex2Response(datex2s);
    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findTrafficDisorders(final String situationId, final int year, final int month) {
        final List<Datex2> datex2s = findDatex2Messages(TRAFFIC_DISORDER, situationId, year, month);

        return convertToTrafficDisordersDatex2Response(datex2s);
    }

    private final List<Datex2> findDatex2Messages(final Datex2MessageType messageType, final String situationId,
        final int year, final int month) {
        if (situationId != null && !datex2Repository.existsWithSituationId(situationId)) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }

        return situationId != null
            ? datex2Repository.findHistoryBySituationId(messageType.name(), situationId, year, month)
            : datex2Repository.findHistory(messageType.name(), year, month);
    }

    public ZonedDateTime findLatestImportTime(final Datex2MessageType messageType) {
        return DateHelper.toZonedDateTime(datex2Repository.findLatestImportTime(messageType.name()));
    }

    @Transactional(readOnly = true)
    public RoadworksDatex2Response getAllRoadworksBySituationId(final String situationId) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndMessageType(situationId, ROADWORK.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToRoadworksDatex2Response(datex2s);
    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response getAllTrafficDisordersBySituationId(final
    String situationId) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndMessageType(situationId, TRAFFIC_DISORDER.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToTrafficDisordersDatex2Response(datex2s);
    }


    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findActiveTrafficDisorders() {
        final List<Datex2> allActive = datex2Repository.findAllActive(TRAFFIC_DISORDER.name());
        return convertToTrafficDisordersDatex2Response(allActive);
    }

    @Transactional(readOnly = true)
    public RoadworksDatex2Response findActiveRoadworks() {
        final List<Datex2> allActive = datex2Repository.findAllActive(ROADWORK.name());
        return convertToRoadworksDatex2Response(allActive);
    }

    private RoadworksDatex2Response convertToRoadworksDatex2Response(final List<Datex2> list) {
        final List<TimestampedRoadworkDatex2> roadworks = list.stream()
            .map(d2 -> unmarshallRoadwork(d2.getMessage(), d2.getImportTime()))
            .collect(Collectors.toList());

        return new RoadworksDatex2Response().withRoadwork(roadworks);
    }

    private TrafficDisordersDatex2Response convertToTrafficDisordersDatex2Response(final List<Datex2> datex2s) {
        final List<TimestampedTrafficDisorderDatex2> timestampedTrafficDisorderDatex2s = new ArrayList<>();
        for (final Datex2 datex2 : datex2s) {
            final String datex2Xml = datex2.getMessage();
            if (!StringUtils.isBlank(datex2Xml)) {
                final TimestampedTrafficDisorderDatex2 tsDatex2 = unmarshallTrafficDisorder(datex2Xml, datex2.getImportTime());
                if (tsDatex2 != null) {
                    timestampedTrafficDisorderDatex2s.add(tsDatex2);
                }
            }
        }
        return new TrafficDisordersDatex2Response().withDisorder(timestampedTrafficDisorderDatex2s);
    }

    private TimestampedTrafficDisorderDatex2 unmarshallTrafficDisorder(final String datex2Xml, final ZonedDateTime importTime) {
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

    private TimestampedRoadworkDatex2 unmarshallRoadwork(final String datex2Xml, final ZonedDateTime importTime) {
        try {
            final D2LogicalModel d2LogicalModel = stringToObjectMarshaller.convertToObject(datex2Xml);
            final ObservationTimeType published =
                new ObservationTimeType()
                    .withLocaltime(DateHelper.toXMLGregorianCalendar(importTime))
                    .withUtc(DateHelper.toXMLGregorianCalendarUtc(importTime));
            final TimestampedRoadworkDatex2 tsDatex2 =
                new TimestampedRoadworkDatex2()
                    .withD2LogicalModel(d2LogicalModel)
                    .withPublished(published);
            return tsDatex2;
        } catch (final XmlMappingException e) {
            log.error("Failed to unmarshal datex2 message: " + datex2Xml, e);
        }

        return null;
    }
}
