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

import fi.livi.digitraffic.tie.dao.trafficmessage.datex2.Datex2Repository;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AlertCLocation;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Feature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.LocationDetails;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadAddress;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadAddressLocation;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.RoadPoint;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TimeAndDuration;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Situation;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2SituationRecord;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2SituationRecordValidyStatus;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.TrafficAnnouncementType;

@Component
public class WazeFeedServiceTestHelper {

    private final Datex2Repository datex2Repository;
    private final ObjectWriter genericJsonWriter;

    private Integer situationCounter = 1000;

    @Autowired
    WazeFeedServiceTestHelper(final ObjectMapper objectMapper, final Datex2Repository datex2Repository) {
        this.datex2Repository = datex2Repository;
        this.genericJsonWriter = objectMapper.writer();
    }

    void cleanup() {
        datex2Repository.deleteAll();
    }

    void insertSituation() {
        final String situationId = "GUID1234";
        insertSituation(situationId, RoadAddressLocation.Direction.BOTH);
    }

    void insertSituation(final String situationId, final RoadAddressLocation.Direction direction) {
        final MultiLineString geometry = new MultiLineString();
        final List<List<Double>> coordinates = new ArrayList<>();

        coordinates.add(List.of(25.180874, 61.569262));
        coordinates.add(List.of(25.180826, 61.569394));

        geometry.addLineString(coordinates);

        insertSituation(situationId, direction, geometry);
    }

    void insertSituation(final String situationId, final RoadAddressLocation.Direction direction, final Geometry<?> geometry) {

        final SituationParams params = new SituationParams(
            situationId,
            ZonedDateTime.now(),
            fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType.ACCIDENT_REPORT,
            direction,
            geometry
        );

        insertSituation(params);
    }

    void insertSituation(final SituationParams params) {
        insertSituation(params, "");
    }

    void insertSituation(final SituationParams params, final String datex2Message) {
        insertSituation(params.situationId, params.situationId, datex2Message, params, params.trafficAnnouncementType);
    }

    void insertSituation(final String situationId, final String situationRecordId, final String datex2Message, final SituationParams params,
                                final fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType datex2TrafficAnnouncementType) {
        final Datex2 datex2 = new Datex2(SituationType.TRAFFIC_ANNOUNCEMENT,
                                         TrafficAnnouncementType.fromValue(datex2TrafficAnnouncementType.name()));
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

    String nextSituationRecord() {
        final String situationIdTemplate = "GUID%s";
        this.situationCounter++;
        return String.format(situationIdTemplate, this.situationCounter);
    }

    String paramsToJson(final SituationParams params) {
        return params.toJson(this.genericJsonWriter);
    }

    static String readDatex2MessageFromFile(final String file) throws IOException {
        return readResourceContent("classpath:wazefeed/" + file);
    }

    static class SituationParams {
        String situationId;
        Geometry<?> geometry;
        fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType trafficAnnouncementType;
        final ZonedDateTime startTime;
        final RoadAddressLocation.Direction direction;

        SituationParams() {
            this(
                null,
                ZonedDateTime.now(),
                fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType.ACCIDENT_REPORT,
                RoadAddressLocation.Direction.UNKNOWN
            );
        }

        SituationParams(final String situationId, final ZonedDateTime startTime,
                               final fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType trafficAnnouncementType, final RoadAddressLocation.Direction direction) {
            this(situationId, startTime, trafficAnnouncementType, direction, null);

            this.geometry = createDummyGeometry();
        }

        SituationParams(final String situationId, final ZonedDateTime startTime,
                               final fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementType trafficAnnouncementType, final RoadAddressLocation.Direction direction,
                               final Geometry<?> geometry) {
            this.situationId = situationId;
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

        String toJson(final ObjectWriter genericJsonWriter) {
            final TrafficAnnouncementProperties properties = createTrafficAnnouncementProperties();
            final TrafficAnnouncementFeature feature = new TrafficAnnouncementFeature(this.geometry, properties);

            String json;

            try {
                json = genericJsonWriter.writeValueAsString(feature);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            return json;
        }

        private TrafficAnnouncementProperties createTrafficAnnouncementProperties() {
            final RoadPoint roadPoint = new RoadPoint();
            roadPoint.municipality = null;
            roadPoint.roadName = null;
            roadPoint.roadAddress = new RoadAddress(null, 0, 0);
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
                fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.TRAFFIC_ANNOUNCEMENT,
                this.trafficAnnouncementType,
                null,
                null,
                List.of(announcement),
                null
            );
        }
    }

}