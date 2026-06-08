package fi.livi.digitraffic.tie;

import java.time.Instant;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties.SituationType;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;
import fi.livi.digitraffic.tie.model.data.DataDatex2SituationMessage;

/**
 * Test helper for inserting {@link DataDatex2Situation} rows.
 * Initialise once per test class (e.g. in {@code @BeforeEach}) with the
 * repository, then call the insert methods without repeating the repo argument.
 *
 * <pre>
 *   private DataDatex2SituationTestHelper helper;
 *
 *   {@literal @}BeforeEach
 *   void setUpHelper() {
 *       helper = new DataDatex2SituationTestHelper(dataDatex2SituationRepository);
 *   }
 * </pre>
 */
public class DataDatex2SituationTestHelper {

    /** A minimal valid SIMPPELI JSON message usable in service/controller tests. */
    public static final String DEFAULT_SIMPPELI_MESSAGE = """
            {
                 "type": "Feature",
                 "geometry": {"type":"Polygon","coordinates": [[[24.0, 61.0], [24.1, 61.0], [24.1, 61.1], [24.0, 61.1], [24, 61]], [[24.01, 61.01], [24.09, 61.01], [24.09, 61.09], [24.01, 61.09], [24.01, 61.01]]]},
                 "properties": {
                     "situationId": "TEST-GUID-001",
                     "version": "1",
                     "situationType": "traffic announcement",
                     "releaseTime": "2020-12-14T00:00:00.000Z",
                     "announcements": [
                         {
                             "language": "fi",
                             "title": "Test traffic announcement",
                             "location": {
                                 "countryCode": 6,
                                 "locationTableNumber": 17,
                                 "locationTableVersion": "1.11.37",
                                 "description": "Test description"
                             },
                             "locationDetails": {
                                 "areaLocation": {
                                     "areas": [
                                         {
                                             "name": "Suomi",
                                             "locationCode": 3,
                                             "type": "country"
                                         }
                                     ]
                                 }
                             },
                             "timeAndDuration": {
                                 "startTime": "2020-12-14T00:00:00.000Z",
                                 "endTime": "2020-12-14T02:00:00.000Z"
                             },
                             "sender": "Test"
                         }
                     ]
                 }
             }
            """;

    /** Default version string for SIMPPELI messages. */
    public static final String DEFAULT_SIMPPELI_VERSION = "0.2.17";

    private static final String POINT_GEOMETRY_JSON = """
            {"type": "Point", "coordinates": [24.0, 61.0]}
            """;

    private final DataDatex2SituationRepository repo;

    public DataDatex2SituationTestHelper(final DataDatex2SituationRepository repo) {
        this.repo = repo;
    }

    /**
     * Inserts a situation row <em>without</em> a message. Useful for DAO tests that only
     * need rows in {@code data_datex2_situation} (e.g. testing {@code findLatestByType}).
     *
     * <p>Flushes after saving so subsequent native queries see the data.</p>
     */
    public void insertSituation(final String situationId,
                                final long situationVersion,
                                final SituationType situationType,
                                final Instant startTime,
                                final Instant endTime) throws ParseException {
        repo.save(createSituation(situationId, situationVersion, situationType, startTime, endTime));
        repo.flush();
    }

    /**
     * Inserts a situation row <em>with</em> one attached message. Useful for service and
     * controller tests where the message content is converted and returned by the API.
     *
     * <p>Flushes after saving so subsequent native queries see the data.</p>
     */
    public void insertSituation(final String situationId,
                                final long situationVersion,
                                final SituationType situationType,
                                final MessageTypeEnum messageType,
                                final String messageVersion,
                                final String message,
                                final Instant startTime,
                                final Instant endTime) throws ParseException {
        final var situation = createSituation(situationId, situationVersion, situationType, startTime, endTime);
        situation.addMessage(new DataDatex2SituationMessage(messageVersion, messageType.value(), message));
        repo.save(situation);
        repo.flush();
    }

    private DataDatex2Situation createSituation(final String situationId,
                                                final long situationVersion,
                                                final SituationType situationType,
                                                final Instant startTime,
                                                final Instant endTime) throws ParseException {
        final Geometry geometry = PostgisGeometryUtils.convertGeoJsonGeometryToGeometry(POINT_GEOMETRY_JSON);
        return new DataDatex2Situation(situationId, situationVersion, situationType,
                geometry, Instant.now(), startTime, endTime);
    }
}
