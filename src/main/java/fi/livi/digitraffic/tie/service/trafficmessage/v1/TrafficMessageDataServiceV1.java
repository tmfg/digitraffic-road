package fi.livi.digitraffic.tie.service.trafficmessage.v1;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.trafficmessage.datex2.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2XmlStringToObjectMarshaller;
import fi.livi.digitraffic.tie.service.trafficmessage.TrafficMessageImsJsonConverterV1;

@ConditionalOnWebApplication
@Service
public class TrafficMessageDataServiceV1 {
    private static final Logger log = LoggerFactory.getLogger(TrafficMessageDataServiceV1.class);

    private final Datex2Repository datex2Repository;
    private final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller;
    private final TrafficMessageImsJsonConverterV1 datex2JsonConverterV1;

    @Autowired
    public TrafficMessageDataServiceV1(final Datex2Repository datex2Repository,
                                       final Datex2XmlStringToObjectMarshaller datex2XmlStringToObjectMarshaller,
                                       final TrafficMessageImsJsonConverterV1 datex2JsonConverterV1) {
        this.datex2Repository = datex2Repository;
        this.datex2XmlStringToObjectMarshaller = datex2XmlStringToObjectMarshaller;
        this.datex2JsonConverterV1 = datex2JsonConverterV1;
    }

    // Isolation.REPEATABLE_READ to prevent another transaction to update data between datex2 query and possible getLastModified query to db.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public TrafficAnnouncementFeatureCollection findActiveJson(final int activeInPastHours,
                                                               boolean includeAreaGeometry, final SituationType... situationTypes) {
        final List<Datex2> allActive = datex2Repository.findAllActiveBySituationTypeWithJson(activeInPastHours, typesAsStrings(situationTypes));
        final Instant lastModified = getLastModified(allActive, situationTypes);
        return convertToFeatureCollection(allActive, includeAreaGeometry, lastModified);
    }

    // Isolation.REPEATABLE_READ to prevent another transaction to update data between datex2 query and possible getLastModified query to db.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Pair<D2LogicalModel, Instant> findActive(final int activeInPastHours,
                                                    final SituationType...situationTypes) {
        final List<Datex2> allActive = datex2Repository.findAllActiveBySituationType(activeInPastHours, typesAsStrings(situationTypes));
        final Instant lastModified = getLastModified(allActive, situationTypes);
        return Pair.of(convertToD2LogicalModel(allActive), lastModified);
    }

    // Isolation.REPEATABLE_READ to prevent another transaction to update data between datex2 query and possible getLastModified query to db.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public TrafficAnnouncementFeatureCollection findBySituationIdJson(final String situationId, final boolean includeAreaGeometry, boolean latest) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdWithJson(situationId);
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Traffic message", situationId);
        }

        final SituationType[] situationTypes = getSituationTypes(datex2s);
        final Instant lastModified = getLastModified(datex2s, situationTypes);

        if (latest) {
            return convertToFeatureCollection(datex2s.subList(0,1), includeAreaGeometry, lastModified);
        }
        return convertToFeatureCollection(datex2s, includeAreaGeometry, lastModified);
    }

    // Isolation.REPEATABLE_READ to prevent another transaction to update data between datex2 query and possible getLastModified query to db.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Pair<D2LogicalModel, Instant> findBySituationId(final String situationId, final boolean latest) {
        final List<Datex2> datex2s = datex2Repository.findBySituationId(situationId);
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }

        final SituationType[] situationTypes = getSituationTypes(datex2s);
        final Instant lastModified = getLastModified(datex2s, situationTypes);

        if (latest) {
            return Pair.of(convertToD2LogicalModel(datex2s.subList(0,1)), lastModified);
        }
        return Pair.of(convertToD2LogicalModel(datex2s), lastModified);
    }

    private SituationType[] getSituationTypes(final List<Datex2> datex2s) {
        return datex2s.stream().map(s -> SituationType.fromValue(s.getSituationType().value())).distinct().toArray(SituationType[]::new);
    }

    private Instant getLastModified(final List<Datex2> datex2s, final SituationType[] situationTypes) {
        return datex2s.stream()
            .map(Datex2::getModified)
            .max(Comparator.naturalOrder())
            .orElse(datex2Repository.getLastModified(typesAsStrings(situationTypes)));
    }

    /**
     * Converts enums to String-array. If empty or null, returns all values of the SituationTypes.
     * @param situationTypes types to convert to string
     * @return types as string-array
     */
    public static String[] typesAsStrings(final SituationType[] situationTypes) {
        if (situationTypes == null || situationTypes.length == 0) {
            return Arrays.stream(SituationType.values()).map(Enum::name).toArray(String[]::new);
        }
        return Arrays.stream(situationTypes).map(Enum::name).toArray(String[]::new);
    }

    private TrafficAnnouncementFeatureCollection convertToFeatureCollection(final List<Datex2> datex2s, boolean includeAreaGeometry,
                                                                            final Instant lastModified) {
        // conver Datex2s to Json objects, newest first, filter out ones without json
        final List<TrafficAnnouncementFeature> features = datex2s.stream()
            .map(d2 -> {
                try {
                    return datex2JsonConverterV1.convertToFeatureJsonObject_V1(d2.getJsonMessage(),
                                                                               d2.getSituationType(),
                                                                               d2.getTrafficAnnouncementType(),
                                                                               includeAreaGeometry,
                                                                               d2.getModified());

                } catch (final Exception e) {
                    log.error(String.format("method=convertToFeatureCollection Failed on convertToFeatureJsonObjectV3 datex2.id: %s", d2.getId()), e);
                    return null;
                }
            })
            // Filter invalid jsons
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing((TrafficAnnouncementFeature json) -> json.getProperties().releaseTime).reversed())
            .collect(Collectors.toList());

        return new TrafficAnnouncementFeatureCollection(lastModified, features);
    }

    private D2LogicalModel convertToD2LogicalModel(final List<Datex2> datex2s) {

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
        final D2LogicalModel containerModel = modelsNewestFirst.remove(0);
        final SituationPublication conainerSituationPublication = getSituationPublication(containerModel);
        modelsNewestFirst.forEach(d2 -> {
            final SituationPublication toAdd = getSituationPublication(d2);
            conainerSituationPublication.getSituations().addAll(toAdd.getSituations());
        });
        return containerModel;
    }

    private static SituationPublication getSituationPublication(final D2LogicalModel model) {
        if (model.getPayloadPublication() instanceof SituationPublication) {
            return (SituationPublication) model.getPayloadPublication();
        } else {
            final String err = "Not SituationPublication available for " + model.getPayloadPublication().getClass();
            log.error(err);
            throw new RuntimeException(err);
        }
    }
}
