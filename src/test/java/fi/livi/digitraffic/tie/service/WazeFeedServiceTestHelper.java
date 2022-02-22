package fi.livi.digitraffic.tie.service;

import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.AlertCLocation;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.Feature;
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

    public void insertSituation() {
        final String situationId = "GUID1234";
        final Integer street = 130;
        insertSituation(situationId, RoadAddressLocation.Direction.BOTH, street);
    }

    public void insertSituation(final String situationId, final RoadAddressLocation.Direction direction,
                                final Integer street) {
        final MultiLineString geometry = new MultiLineString();
        final List<List<Double>> coordinates = new ArrayList<>();

        coordinates.add(List.of(25.180874, 61.569262));
        coordinates.add(List.of(25.180826, 61.569394));

        geometry.addLineString(coordinates);

        insertSituation(situationId, direction, street, geometry);
    }

    public void insertSituation(final String situationId, final RoadAddressLocation.Direction direction,
                                final Integer street, final Geometry<?> geometry) {

        final SituationParams params = new SituationParams(
            situationId,
            new AnnouncementAddress("municipality", "roadName", street),
            ZonedDateTime.now(),
            TrafficAnnouncementType.ACCIDENT_REPORT,
            direction,
            geometry
        );

        insertSituation(params);
    }

    public void insertSituation(final SituationParams params) {
        insertSituation(params, "");
    }

    public void insertSituation(final SituationParams params, final String datex2Message) {
        insertSituation(params.situationId, params.situationId, datex2Message, params, params.trafficAnnouncementType);
    }

    public void insertSituation(final String situationId, final String situationRecordId, final String datex2Message, final SituationParams params,
                                final TrafficAnnouncementType datex2TrafficAnnouncementType) {
        final Datex2 datex2 = new Datex2(SituationType.TRAFFIC_ANNOUNCEMENT, datex2TrafficAnnouncementType);
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
        datex2.setJsonMessage(paramsToJson(params));
        datex2.setMessage(datex2Message);
        datex2.setSituations(List.of(situation));
        datex2Repository.save(datex2);
    }

    public String nextSituationRecord() {
        final String situationIdTemplate = "GUID%s";
        this.situationCounter++;
        return String.format(situationIdTemplate, this.situationCounter);
    }

    public String paramsToJson(final SituationParams params) {
        return params.toJson(this.genericJsonWriter);
    }

    public static String readDatex2MessageFromFile(final String file) throws IOException {
        return readResourceContent("classpath:wazefeed/" + file);
    }

    public static class SituationParams {
        String situationId;
        Geometry<?> geometry;
        TrafficAnnouncementType trafficAnnouncementType;

        final AnnouncementAddress announcementAddress;
        final ZonedDateTime startTime;
        final RoadAddressLocation.Direction direction;

        public SituationParams() {
            this(
                null,
                new AnnouncementAddress(),
                ZonedDateTime.now(),
                TrafficAnnouncementType.ACCIDENT_REPORT,
                RoadAddressLocation.Direction.UNKNOWN
            );
        }

        public SituationParams(final String situationId, final AnnouncementAddress announcementAddress, final ZonedDateTime startTime,
                               final TrafficAnnouncementType trafficAnnouncementType, final RoadAddressLocation.Direction direction) {
            this(situationId, announcementAddress, startTime, trafficAnnouncementType, direction, null);

            this.geometry = createDummyGeometry();
        }

        public SituationParams(final String situationId, final AnnouncementAddress announcementAddress, final ZonedDateTime startTime,
                               final TrafficAnnouncementType trafficAnnouncementType, final RoadAddressLocation.Direction direction,
                               final Geometry<?> geometry) {
            this.situationId = situationId;
            this.announcementAddress = announcementAddress;
            this.startTime = startTime;
            this.trafficAnnouncementType = trafficAnnouncementType;
            this.direction = direction;
            this.geometry = geometry;
        }

        private Geometry<?> createDummyGeometry() {
            final MultiLineString geometry = new MultiLineString();
            final List<List<Double>> coordinates1 = new ArrayList<>();
            final List<List<Double>> coordinates2 = new ArrayList<>();

            coordinates1.add(List.of(25.180874, 61.569262));
            coordinates1.add(List.of(25.180826, 61.569394));
            coordinates1.add(List.of(25.180754, 61.569586));
            coordinates1.add(List.of(25.180681, 61.569794));
            coordinates1.add(List.of(25.180404, 61.570703));

            coordinates2.add(List.of(25.212664, 61.586387));
            coordinates2.add(List.of(25.212674, 61.586397));

            geometry.addLineString(coordinates1);
            geometry.addLineString(coordinates2);

            return geometry;
        }

        public String toJson(final ObjectWriter genericJsonWriter) {
            final TrafficAnnouncementProperties properties = createTrafficAnnouncementProperties();
            final TrafficAnnouncementFeature feature = new TrafficAnnouncementFeature(this.geometry, properties);

            String json;

            try {
                json = genericJsonWriter.writeValueAsString(feature);
            } catch (final JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            return json;
        }

        private TrafficAnnouncementProperties createTrafficAnnouncementProperties() {
            final RoadPoint roadPoint = new RoadPoint();
            roadPoint.municipality = this.announcementAddress.municipality;
            roadPoint.roadName = this.announcementAddress.roadName;
            roadPoint.roadAddress = new RoadAddress(this.announcementAddress.street, 0, 0);
            roadPoint.alertCLocation = new AlertCLocation();

            final RoadAddressLocation roadAddressLocation = new RoadAddressLocation(roadPoint, null, direction, "");

            final TimeAndDuration timeAndDuration = new TimeAndDuration(
                this.startTime,
                null,
                null
            );

            final TrafficAnnouncement announcement =  new TrafficAnnouncement(
                null,
                null,
                null,
                new LocationDetails(null, roadAddressLocation),
                List.of(new Feature("Onnettomuus", null, null, null)),
                new ArrayList<>(),
                null,
                null,
                null,
                timeAndDuration,
                null,
                null
            );

            return new TrafficAnnouncementProperties(
                this.situationId,
                11,
                SituationType.TRAFFIC_ANNOUNCEMENT,
                this.trafficAnnouncementType,
                null,
                null,
                List.of(announcement),
                null
            );
        }
    }

    public static class AnnouncementAddress {
        final String municipality;
        final String roadName;
        final Integer street;

        public AnnouncementAddress() {
            this("Espoo", "Puolarmetsänkatu", 123);
        }

        public AnnouncementAddress(final String municipality, final String roadName, final Integer street) {
            this.municipality = municipality;
            this.roadName = roadName;
            this.street = street;
        }
    }

}