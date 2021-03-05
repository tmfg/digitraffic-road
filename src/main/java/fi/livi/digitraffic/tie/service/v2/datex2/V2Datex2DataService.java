package fi.livi.digitraffic.tie.service.v2.datex2;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.datex2.V2Datex2JsonConverterService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2XmlStringToObjectMarshaller;

@ConditionalOnWebApplication
@Service
public class V2Datex2DataService {
    private static final Logger log = LoggerFactory.getLogger(V2Datex2DataService.class);

    private final Datex2Repository datex2Repository;
    private final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;
    private final V2Datex2JsonConverterService v2Datex2JsonConverterService;
    private DataStatusService dataStatusService;

    @Autowired
    public V2Datex2DataService(final Datex2Repository datex2Repository,
                               final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller,
                               final V2Datex2JsonConverterService v2Datex2JsonConverterService,
                               final DataStatusService dataStatusService) {
        this.datex2Repository = datex2Repository;
        this.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;
        this.v2Datex2JsonConverterService = v2Datex2JsonConverterService;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findAllBySituationId(final String situationId, final Datex2MessageType datex2MessageType) {
        final List<Datex2> datex2s = findBySituationIdAndMessageType(situationId, datex2MessageType.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToD2LogicalModel(datex2s);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findAllBySituationIdJson(final String situationId, final Datex2MessageType datex2MessageType) {
        final List<Datex2> datex2s = findBySituationIdAndMessageTypeWithJson(situationId, datex2MessageType.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToFeatureCollection(datex2s);
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findActive(final int inactiveHours,
                                     final Datex2MessageType datex2MessageType) {
        final List<Datex2> allActive = findAllActive(datex2MessageType.name(), inactiveHours);
        return convertToD2LogicalModel(allActive);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findActiveJson(final int inactiveHours,
                                                               final Datex2MessageType datex2MessageType) {
        final List<Datex2> allActive = findAllActiveWithJson(datex2MessageType.name(), inactiveHours);
        return convertToFeatureCollection(allActive);
    }

    private List<Datex2> findAllActive(final String messageType, final int activeInPastHours) {
        return datex2Repository.findAllActive(messageType, activeInPastHours);
    }

    private List<Datex2> findAllActiveWithJson(final String messageType, final int activeInPastHours) {
        return datex2Repository.findAllActiveWithJson(messageType, activeInPastHours);
    }

    private List<Datex2> findBySituationIdAndMessageType(final String situationId, final String messageType) {
        return datex2Repository.findBySituationIdAndMessageType(situationId, messageType);
    }

    private List<Datex2> findBySituationIdAndMessageTypeWithJson(final String situationId, final String messageType) {
        return datex2Repository.findBySituationIdAndMessageTypeWithJson(situationId, messageType);
    }

    public D2LogicalModel convertToD2LogicalModel(final List<Datex2> datex2s) {

        // conver Datex2s to D2LogicalModels
        final List<D2LogicalModel> modelsNewestFirst = datex2s.stream()
            .map(datex2 -> datex2XmlStringToObjectMarshaller.convertToObject(datex2.getMessage()))
            .filter(d2 -> d2.getPayloadPublication() != null)
            .sorted(Comparator.comparing((D2LogicalModel d2) -> d2.getPayloadPublication().getPublicationTime()).reversed())
            .collect(Collectors.toList());

        if (modelsNewestFirst.isEmpty()) {
            return new D2LogicalModel();
        }

        // Append all older situations to newest and return newest that combines all situations
        final D2LogicalModel newesModel = modelsNewestFirst.remove(0);
        final SituationPublication situationPublication = getSituationPublication(newesModel);
        modelsNewestFirst.forEach(d2 -> {
            final SituationPublication toAdd = getSituationPublication(d2);
            situationPublication.getSituations().addAll(toAdd.getSituations());
        });
        return newesModel;
    }

    private TrafficAnnouncementFeatureCollection convertToFeatureCollection(final List<Datex2> datex2s) {
        final ZonedDateTime lastUpdated = dataStatusService.findDataUpdatedTime(DataType.TRAFFIC_MESSAGES_DATA);
        // conver Datex2s to Json objects, newest first, filter out ones without json
        final List<TrafficAnnouncementFeature> features = datex2s.stream()
            .map(d2 -> {
                try {
                    return v2Datex2JsonConverterService.convertToFeatureJsonObjectV2(d2.getJsonMessage(), d2.getMessageType());
                } catch (final Exception e) {
                    log.error("method=convertToFeatureCollection Failed on convertToFeatureJsonObjectV2", e);
                    return null;
                }
            })
            // Filter invalid jsons
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing((TrafficAnnouncementFeature json) -> json.getProperties().releaseTime).reversed())
            .collect(Collectors.toList());
        return new TrafficAnnouncementFeatureCollection(DateHelper.toZonedDateTimeAtUtc(lastUpdated), DateHelper.getZonedDateTimeNowAtUtc(), features);
    }

    static SituationPublication getSituationPublication(final D2LogicalModel model) {
        if (model.getPayloadPublication() instanceof SituationPublication) {
            return (SituationPublication) model.getPayloadPublication();
        } else {
            final String err = "Not SituationPublication available for " + model.getPayloadPublication().getClass();
            log.error(err);
            throw new RuntimeException(err);
        }
    }
}
