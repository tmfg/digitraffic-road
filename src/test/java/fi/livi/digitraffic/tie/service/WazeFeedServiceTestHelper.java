package fi.livi.digitraffic.tie.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.AlertCLocation;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Contact;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.EstimatedDuration;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Feature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Location;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.LocationDetails;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddress;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.RoadPoint;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TimeAndDuration;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2Situation;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecord;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecordValidyStatus;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;

@Component
public class WazeFeedServiceTestHelper {

    private final Datex2Repository datex2Repository;
    private final ObjectWriter genericJsonWriter;

    private Integer situationCounter = 1000;

    @Autowired
    public WazeFeedServiceTestHelper(final ObjectMapper objectMapper, final Datex2Repository datex2Repository) {
        this.datex2Repository = datex2Repository;

        this.genericJsonWriter = objectMapper.writer();
    }

    public void cleanup() {
        datex2Repository.deleteAll();
    }

    public void insertAccident() {
        final String situationId = "GUID1234";
        final String situationRecordId = "GUID12345";
        final String street = "130";
        final String sender = "Liikennevirasto";
        insertAccident(situationId, situationRecordId, RoadAddressLocation.Direction.BOTH, street, sender);
    }

    public void insertAccident(final String situationId, final String situationRecordId, final RoadAddressLocation.Direction direction,
                               final String street, final String sender) {
        final MultiLineString geometry = new MultiLineString();
        final List<List<Double>> coordinates = new ArrayList<>();

        coordinates.add(List.of(25.180874, 61.569262));
        coordinates.add(List.of(25.180826, 61.569394));

        geometry.addLineString(coordinates);

        insertAccident(situationId, situationRecordId, direction, street, sender, geometry);
    }

    public void insertAccident(final String situationId, final String situationRecordId, final RoadAddressLocation.Direction direction,
                               final String street, final String sender, final Geometry<?> geometry) {
        final Map<String, Optional<String>> hm = createIncidentMap(
            "additional information",
            "comment",
            "description",
            "informal estimation",
            ZonedDateTime.now().toOffsetDateTime().toString(),
            "email",
            "phone",
            sender,
            situationId,
            street,
            "general"
        );

        final String jsonMessage = createJsonMessage(geometry, hm, direction, List.of("line1", "line3"));

        insertAccident(situationId, situationRecordId, jsonMessage);
    }

    public void insertAccident(final String situationId, final String situationRecordId, final String jsonMessage) {
        insertAccident(situationId, situationRecordId, jsonMessage, TrafficAnnouncementType.ACCIDENT_REPORT);
    }

    public void insertAccident(final String situationId, final String situationRecordId, final String jsonMessage, final TrafficAnnouncementType trafficAnnouncementType) {
        final Datex2 datex2 = new Datex2(SituationType.TRAFFIC_ANNOUNCEMENT, trafficAnnouncementType);
        final Datex2Situation situation = new Datex2Situation();
        final Datex2SituationRecord situationRecord = new Datex2SituationRecord();
        final ZonedDateTime dateTimeNow = ZonedDateTime.now();

        situationRecord.setSituationRecordId(situationRecordId);
        situationRecord.setValidyStatus(Datex2SituationRecordValidyStatus.ACTIVE);
        situationRecord.setCreationTime(dateTimeNow);
        situationRecord.setVersionTime(dateTimeNow);
        situationRecord.setOverallStartTime(dateTimeNow);
        situationRecord.setType(Datex2SituationRecordType.TRAFFIC_ELEMENT_ACCIDENT);
        situationRecord.setLifeCycleManagementCanceled(false);

        situation.setSituationId(situationId);
        situation.setDatex2(datex2);
        situation.addSituationRecord(situationRecord);

        datex2.setImportTime(dateTimeNow);
        datex2.setJsonMessage(jsonMessage);
        datex2.setMessage("");
        datex2.setSituations(List.of(situation));
        datex2Repository.save(datex2);
    }

    public String createJsonMessage(final Map<String, Optional<String>> params, final RoadAddressLocation.Direction direction,
                                    final List<String> features) {
        final MultiLineString geometry = new MultiLineString();
        final List<List<Double>> coordinates1 = new ArrayList<>();
        final List<List<Double>> coordinates2 = new ArrayList<>();

        coordinates1.add(List.of(25.180874, 61.569262));
        coordinates1.add(List.of(25.180826, 61.569394));
        coordinates1.add(List.of(25.180754, 61.569586));
        coordinates1.add(List.of(25.180681, 61.569794));
        coordinates1.add(List.of(25.180404, 61.570703));

        coordinates2.add(List.of(25.212664, 61.586387));
        coordinates2.add(List.of(25.212664, 61.586387));

        geometry.addLineString(coordinates1);
        geometry.addLineString(coordinates2);

        return createJsonMessage(geometry, params, direction, features);
    }

    public String createJsonMessage(final Geometry<?> geometry, final Map<String, Optional<String>> params,
                                    final RoadAddressLocation.Direction direction, final List<String> features) {

        final TrafficAnnouncementProperties properties = createTrafficAnnouncementProperties(params, direction, features);
        final TrafficAnnouncementFeature feature = new TrafficAnnouncementFeature(geometry, properties);

        String json;

        try {
            json = genericJsonWriter.writeValueAsString(feature);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return json;
    }

    private TrafficAnnouncementProperties createTrafficAnnouncementProperties(final Map<String, Optional<String>> params,
                                                                              final RoadAddressLocation.Direction direction,
                                                                              final List<String> features) {
        final List<Feature> ftrs = convertToFeatures(features);

        final RoadPoint roadPoint = new RoadPoint();
        roadPoint.roadAddress = new RoadAddress(params.get("street").map(Integer::parseInt).orElse(null), 0, 0);
        roadPoint.alertCLocation = new AlertCLocation();

        final RoadAddressLocation roadAddressLocation = new RoadAddressLocation(roadPoint, null, direction, "");

        final TimeAndDuration timeAndDuration = new TimeAndDuration(
            params.get("startTime")
                .map(ZonedDateTime::parse)
                .orElse(null),
            null,
            params.get("estimatedDurationInformal")
                .map(x -> new EstimatedDuration("PT1H", "PT3H", x))
                .orElse(null)
        );

        final TrafficAnnouncement announcement =  new TrafficAnnouncement(
            null,
            null,
            params.get("description")
                .map(x -> new Location(6, 17, "1_11_40", x))
                .orElse(null),
            new LocationDetails(null, roadAddressLocation),
            ftrs,
            new ArrayList<>(),
            null,
            null,
            params.get("comment")
                .orElse(null),
            timeAndDuration,
            params.get("additionalInformation")
                .orElse(null),
            params.get("sender")
                .orElse(null)
        );

        final Contact contact = params.get("phone")
            .flatMap(phone -> params.get("email").map(email -> new Contact(phone, email)))
            .orElse(null);

        return new TrafficAnnouncementProperties(
            params.get("situationId").orElse(null),
            11,
            SituationType.TRAFFIC_ANNOUNCEMENT,
            params.get("trafficAnnouncementType").map(this::convertToTrafficAnnouncementType).orElse(null),
            null,
            List.of(announcement),
            contact
        );
    }

    private List<Feature> convertToFeatures(final List<String> features) {
        if (features == null) {
            return null;
        }

        return features.stream()
            .map(x -> new Feature(x, null, null, null))
            .collect(Collectors.toList());
    }

    private TrafficAnnouncementType convertToTrafficAnnouncementType(final String stringValue) {
        switch (stringValue) {
        case "preliminary_accident_report":
            return TrafficAnnouncementType.PRELIMINARY_ACCIDENT_REPORT;
        case "accident_report":
            return TrafficAnnouncementType.ACCIDENT_REPORT;
        case "general":
        default:
            return TrafficAnnouncementType.GENERAL;
        }
    }

    public String nextSituationRecord() {
        final String situationIdTemplate = "GUID%s";
        this.situationCounter++;
        return String.format(situationIdTemplate, this.situationCounter);
    }

    public static Map<String, Optional<String>> createIncidentMap(final String additionalInformation, final String comment, final String description,
                                                                  final String estimatedDurationInformal, final String startTime, final String email,
                                                                  final String phone, final String sender, final String situationId,
                                                                  final String street, final String trafficAnnouncementType) {
        final Map<String, Optional<String>> hm = new HashMap<>();

        hm.put("additionalInformation", Optional.ofNullable(additionalInformation));
        hm.put("comment", Optional.ofNullable(comment));
        hm.put("description", Optional.ofNullable(description));
        hm.put("estimatedDurationInformal", Optional.ofNullable(estimatedDurationInformal));
        hm.put("startTime", Optional.ofNullable(startTime));
        hm.put("email", Optional.ofNullable(email));
        hm.put("phone", Optional.ofNullable(phone));
        hm.put("sender", Optional.ofNullable(sender));
        hm.put("situationId", Optional.ofNullable(situationId));
        hm.put("street", Optional.ofNullable(street));
        hm.put("trafficAnnouncementType", Optional.ofNullable(trafficAnnouncementType));

        return hm;
    }
}