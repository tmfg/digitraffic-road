package fi.livi.digitraffic.tie.data.controller.v3;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V3_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_MESSAGES_SIMPLE_PATH;
import static fi.livi.digitraffic.tie.model.v1.datex2.SituationType.TRAFFIC_ANNOUNCEMENT;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getSituationIdForSituationType;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.getVersionTime;
import static fi.livi.digitraffic.tie.service.v2.datex2.RegionGeometryTestHelper.createNewRegionGeometry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.xml.transform.StringSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TimeAndDuration;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncement;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.trafficmessage.Datex2Helper;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2DataService;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2DataService;

public class V3TrafficMessagesControllerTest extends AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock {
    private static final Logger log = getLogger(V3TrafficMessagesControllerTest.class);

    @Autowired
    protected Datex2DataService datex2DataService;

    @Autowired
    protected Datex2Repository datex2Repository;

    @Autowired
    @Qualifier("datex2Jaxb2Marshaller")
    private Jaxb2Marshaller datex2Jaxb2Marshaller;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @BeforeEach
    public void init() {
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(0));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(3));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(7));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(14));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(408));
        whenV3RegionGeometryDataServicGetAreaLocationRegionEffectiveOn(createNewRegionGeometry(5898));
    }

    /**
     * Test all Ims Xml Versions and all message types to be returned by the controller
     */
    @Test
    public void getJsonAndXmlCurrentlyActive() throws Exception {
        for (final TrafficMessageTestHelper.ImsXmlVersion imsXmlVersion : TrafficMessageTestHelper.ImsXmlVersion.values()) {
            for (final TrafficMessageTestHelper.ImsJsonVersion imsJsonVersion : TrafficMessageTestHelper.ImsJsonVersion.values()) {
                for(final SituationType situationType : SituationType.values()) {
                    trafficMessageTestHelper.cleanDb();
                    final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
                    final ZonedDateTime end = start.plusHours(2);
                    trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                    log.info("getJsonAndXmlCurrentlyActive with imsXmlVersion={}, imsJsonVersion={} and situationType={}", imsXmlVersion, imsJsonVersion, situationType);

                    final String xml = getResponse(getUrlWithType(false, 0, situationType));
                    final String json = getResponse(getUrlWithType(true, 0, situationType));
                    assertIsValidDatex2Xml(xml);
                    assertTextIsValidJson(json);
                    assertTimesFormatMatches(xml);
                    assertTimesFormatMatches(json);
                    assertTimesFormatMatches(json);
                    assertContentsMatch(xml, json, situationType, getSituationIdForSituationType(situationType.name()), start, end, imsJsonVersion);
                    assertTraficAnouncmentTypeLowerCase(json, situationType);
                }
            }
        }
    }

    /**
     * Test all Ims Xml Versions and all message types to be returned by the controller when they are inactive and inside inactive hours parameter
     */
    @Test
    public void getJsonAndXmlCurrentlyInactiveWithInactiveHours() throws Exception {
        for (final TrafficMessageTestHelper.ImsXmlVersion imsXmlVersion : TrafficMessageTestHelper.ImsXmlVersion.values()) {
            for (final TrafficMessageTestHelper.ImsJsonVersion imsJsonVersion : TrafficMessageTestHelper.ImsJsonVersion.values()) {
                for(final SituationType situationType : SituationType.values()) {
                    trafficMessageTestHelper.cleanDb();
                    final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(3);
                    final ZonedDateTime end = start.plusHours(2);
                    trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                    log.info("getJsonAndXmlCurrentlyActive with imsXmlVersion={}, imsJsonVersion={} and situationType={}", imsXmlVersion, imsJsonVersion, situationType);

                    final String xml = getResponse(getUrlWithType(false, 2, situationType));
                    final String json = getResponse(getUrlWithType(true, 2, situationType));
                    assertIsValidDatex2Xml(xml);
                    assertTextIsValidJson(json);
                    assertTimesFormatMatches(xml);
                    assertTimesFormatMatches(json);
                    assertContentsMatch(xml, json, situationType, getSituationIdForSituationType(situationType.name()), start, end, imsJsonVersion);
                    assertTraficAnouncmentTypeLowerCase(json, situationType);
                }
            }
        }
    }

    /**
     * Tests all Ims Xml Versions and all message types to not be returned by the controller when they are passive.
     */
    @Test
    public void getJsonAndXmlCurrentlyInactive() throws Exception {
        for (final TrafficMessageTestHelper.ImsXmlVersion imsXmlVersion : TrafficMessageTestHelper.ImsXmlVersion.values()) {
            for (final TrafficMessageTestHelper.ImsJsonVersion imsJsonVersion : TrafficMessageTestHelper.ImsJsonVersion.values()) {
                for(final SituationType situationType : SituationType.values()) {
                    trafficMessageTestHelper.cleanDb();
                    final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(3);
                    final ZonedDateTime end = start.plusHours(2);
                    trafficMessageTestHelper.initDataFromStaticImsResourceContent(imsXmlVersion, situationType.name(), imsJsonVersion, start, end);
                    log.info("getJsonAndXmlCurrentlyPassive with imsXmlVersion={}, imsJsonVersion={} and situationType={}", imsXmlVersion, imsJsonVersion, situationType);

                    final String xml = getResponse(getUrlWithType(false, 0, situationType));
                    final String json = getResponse(getUrlWithType(true, 0, situationType));
                    assertIsValidDatex2Xml(xml);
                    assertTextIsValidJson(json);
                    assertEmptyD2Situations(xml);
                    assertEmptyJsonSituations(json);
                }
            }
        }
    }

    private void assertEmptyD2Situations(String xml) {
        final D2LogicalModel d2 = parseD2LogicalModel(xml);
        assertNull(d2.getPayloadPublication());
    }

    private void assertEmptyJsonSituations(final String simpleJson) throws JsonProcessingException {
        final TrafficAnnouncementFeatureCollection fc = parseSimpleJson(simpleJson);
        assertTrue(fc.getFeatures().isEmpty());
    }

    private TrafficAnnouncementFeatureCollection parseSimpleJson(final String simpleJson) throws JsonProcessingException {
        final ObjectReader r = objectMapper.readerFor(TrafficAnnouncementFeatureCollection.class);
        return r.readValue(simpleJson);
    }

    private D2LogicalModel parseD2LogicalModel(final String d2xml) {
        return (D2LogicalModel) datex2Jaxb2Marshaller.unmarshal(new StringSource(d2xml));
    }

    private void assertContentsMatch(final String d2xml, final String simpleJsonFeatureCollection, final SituationType situationType,
                                     final String situationId,
                                     final ZonedDateTime start, final ZonedDateTime end,
                                     final TrafficMessageTestHelper.ImsJsonVersion imsJsonVersion)
        throws JsonProcessingException {
        final D2LogicalModel d2 = parseD2LogicalModel(d2xml);

        final TrafficAnnouncementFeatureCollection fc = parseSimpleJson(simpleJsonFeatureCollection);
        final TrafficAnnouncementFeature feature = fc.getFeatures().get(0);

        final SituationPublication sp = Datex2Helper.getSituationPublication(d2);
        final Situation situation = sp.getSituations().get(0);
        final TrafficAnnouncementProperties jsonProperties = feature.getProperties();

        assertEquals(situationId, situation.getId());
        assertEquals(situationId, jsonProperties.situationId);

        final TimeAndDuration jsonTimeAndDuration = jsonProperties.announcements.get(0).timeAndDuration;

        assertEquals(start.toInstant(), situation.getSituationRecords().get(0).getValidity().getValidityTimeSpecification().getOverallStartTime());
        assertEquals(start.toInstant(), jsonTimeAndDuration.startTime.toInstant());

        final Instant versionTime = getVersionTime(start, imsJsonVersion).toInstant();
        assertEquals(versionTime, situation.getSituationRecords().get(0).getSituationRecordVersionTime());
        assertEquals(versionTime, jsonProperties.releaseTime.toInstant());
        assertEquals(versionTime, jsonProperties.versionTime.toInstant());

        assertEquals(end.toInstant(), situation.getSituationRecords().get(0).getValidity().getValidityTimeSpecification().getOverallEndTime());
        assertEquals(end.toInstant(), jsonTimeAndDuration.endTime.toInstant());

        final String commentXml = situation.getSituationRecords().get(0).getGeneralPublicComments().get(0).getComment().getValues().getValues().stream()
            .filter(c -> c.getLang().equals("fi")).findFirst().orElseThrow().getValue();

        assertEquals(situationType, jsonProperties.getSituationType());
        if (situationType == TRAFFIC_ANNOUNCEMENT) {
            assertTrue(Sets.newHashSet(TrafficAnnouncementType.values()).contains(jsonProperties.getTrafficAnnouncementType()));
        }

        final TrafficAnnouncement announcement = jsonProperties.announcements.get(0);
        assertTrue(commentXml.contains(announcement.title.trim()));
    }

    private void assertIsValidDatex2Xml(final String xml) {
        try {
            datex2Jaxb2Marshaller.unmarshal(new StringSource(xml));
        } catch (final XmlMappingException e) {
            throw new IllegalArgumentException("Not XML: " + xml, e);
        }
    }

    private void assertTextIsValidJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Not JSON: " + json, e);
        }
    }

    private static String getUrlWithType(final boolean json, final int inactiveHours, final SituationType...messageType) {
        final String[] types = V3Datex2DataService.typesAsStrings(messageType);
        final String params = String.join(",", types);
        return API_V3_BASE_PATH + API_DATA_PART_PATH + (json ? TRAFFIC_MESSAGES_SIMPLE_PATH : TRAFFIC_MESSAGES_DATEX2_PATH) + "?lastUpdated=false&inactiveHours=" + inactiveHours + "&situationType=" + params;
    }

    private String getResponse(final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);
        if (url.contains("datex2")) {
            get.contentType(MediaType.APPLICATION_XML);
        } else {
            get.contentType(MediaType.APPLICATION_JSON);
        }
        return mockMvc.perform(get).andReturn().getResponse().getContentAsString();
    }

    private void assertTraficAnouncmentTypeLowerCase(final String json, final SituationType situationType) {
        if (situationType.equals(TRAFFIC_ANNOUNCEMENT)) {
            final String trafficAnnouncementType =
                StringUtils.substringBefore(
                    StringUtils.substringAfter(
                        StringUtils.substringAfter(
                            StringUtils.substringAfter(json, "trafficAnnouncementType"), ":"), "\""), "\"");
            final Set<String> values = Arrays.stream(TrafficAnnouncementType.values()).map(TrafficAnnouncementType::value).collect(Collectors.toSet());
            assertTrue(values.contains(trafficAnnouncementType));
            assertTrue(StringUtils.isAllLowerCase(trafficAnnouncementType.replace(" ", "")));
        }
    }
}
