package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessagesControllerBeta.API_TRAFFIC_MESSAGE_BETA;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessagesControllerBeta.DATEX2_2_2_3;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessagesControllerBeta.DATEX2_3_5;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessagesControllerBeta.EXEMPTED_TRANSPORTS;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessagesControllerBeta.ROADWORKS;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessagesControllerBeta.TRAFFIC_ANNOUNCEMENTS;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessagesControllerBeta.WEIGHT_RESTRICTIONS;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.JsonAsserter;
import fi.livi.digitraffic.ResponseAsserter;
import fi.livi.digitraffic.XmlAsserter;
import fi.livi.digitraffic.tie.AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock;
import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;
import fi.livi.digitraffic.tie.model.data.DataDatex2SituationMessage;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;

public class TrafficMessageControllerBetaTest extends AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock {
    @Autowired
    private DataDatex2SituationRepository dataDatex2SituationRepository;

    private static final String TIME_PAST = Instant.now().minus(10, ChronoUnit.HOURS).toString();
    private static final String TIME_NOW = Instant.now().toString();
    private static final String TIME_FUTURE = Instant.now().plus(10, ChronoUnit.HOURS).toString();

    private static final String SIMPPELI = """
            {
                 "type": "Feature",
                 "geometry": {"type":"Polygon","coordinates": [[[24.0, 61.0], [24.1, 61.0], [24.1, 61.1], [24.0, 61.1], [24, 61]], [[24.01, 61.01], [24.09, 61.01], [24.09, 61.09], [24.01, 61.09], [24.01, 61.01]]]},
                 "properties": {
                     "situationId": "GUID10000002",
                     "version": "1",
                     "situationType": "special transport",
                     "releaseTime": "2020-12-14T00:00:00.000Z",
                     "announcements": [
                         {
                             "language": "fi",
                             "title": "Erikoiskuljetus. Satakunta",
                             "location": {
                                 "countryCode": 6,
                                 "locationTableNumber": 17,
                                 "locationTableVersion": "1.11.37",
                                 "description": "Erikoiskuljetus alueella Kankaanpää Kuljetus liikkuu 12.01.2021 klo 19:00 - 21:00 reitillä: Kankaanpään katuverkko Liikenne pysäytetään ajoittain. Kuljetuksen leveys: 9,5 m Kuljetuksen korkeus: 10,8 m Kuljetuksen pituus: 40 m Kuljetuksen kokonaismassa: 167 t Kuljetus on perillä 13.01.2021 klo 06:00 mennessä"
                             },
                             "locationDetails": {
                                 "areaLocation": {
                                     "areas": [
                                         {
                                             "name": "Suomi",
                                             "locationCode": 3,
                                             "type": "country"
                                         },
                                         {
                                             "name": "Länsi- ja Sisä-Suomi",
                                             "locationCode": 7,
                                             "type": "regional state administrative agency"
                                         },
                                         {
                                             "name": "Tampereen seutu",
                                             "locationCode": 5898,
                                             "type": "weather region"
                                         },
                                         {
                                             "name": "Pirkanmaa",
                                             "locationCode": 14,
                                             "type": "province"
                                         },
                                         {
                                             "name": "Tampere",
                                             "locationCode": 408,
                                             "type": "municipality"
                                         }
                                     ]
                                 }
                             },
                             "features": [
                                 {
                                     "name" : "Liikenne pysäytetään ajoittain"
                                 },
                                 {
                                     "name" : "Kuljetuksen leveys",
                                     "quantity" : 4.5,
                                     "unit" : "m"
                                 }
                             ],
                             "lastActiveItinerarySegment": {
                                 "startTime": "2020-12-14T00:00:00.000Z",
                                 "endTime": "2020-12-14T00:00:00.000Z",
                                 "legs": [
                                     {
                                         "roadLeg": {
                                             "roadNumber": "12",
                                             "roadName": "Kotikatu 1",
                                             "startArea": "Tien alku",
                                             "endArea": "Tien loppu"
                                         }
                                     }
                                 ]
                             },
                             "comment": "TESTI TESTI TESTI\\nML",
                             "timeAndDuration": {
                                 "startTime": "2020-12-14T00:00:00.000Z",
                                 "endTime": "2020-12-14T00:00:00.000Z",
                                 "estimatedDuration": {
                                     "minimum": "PT6H",
                                     "informal": "Yli 6 tuntia"
                                 }
                             },
                             "additionalInformation": "Liikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/",
                             "sender": "Liikenneviraston tieliikennekeskus Helsinki"
                         }
                     ],
                     "contact": {
                         "phone": "0206373328",
                         "email": "helsinki.liikennekeskus@liikennevirasto.fi"
                     }
                 }
             }
            """;

    private static final String ROADWORK_DATEXII_3_5 = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <ns2:payload xmlns="http://datex2.eu/schema/3/common" xmlns:ns2="http://datex2.eu/schema/3/situation" xmlns:ns3="http://datex2.eu/schema/3/locationReferencing" xmlns:ns4="http://datex2.eu/schema/3/d2Payload" xmlns:ns5="http://datex2.eu/schema/3/roadTrafficData" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns2:SituationPublication" lang="fi" modelBaseVersion="3" xsi:schemaLocation="https://datex2.eu/schema/3/situation">
                <publicationTime>2025-09-26T13:19:46.460Z</publicationTime>
                <publicationCreator>
                    <country>FI</country>
                    <nationalIdentifier>FTA</nationalIdentifier>
                </publicationCreator>
                <ns2:situation id="GUID50442306">
                    <ns2:headerInformation>
                        <informationStatus>real</informationStatus>
                    </ns2:headerInformation>
                    <ns2:situationRecord xsi:type="ns2:AuthorityOperation" id="GUID5044547601" version="1">
                        <ns2:situationRecordCreationTime>2025-09-26T13:18:47.686Z</ns2:situationRecordCreationTime>
                        <ns2:situationRecordVersionTime>2025-09-26T13:19:45.455Z</ns2:situationRecordVersionTime>
                        <ns2:probabilityOfOccurrence>certain</ns2:probabilityOfOccurrence>
                        <ns2:validity>
                            <validityStatus>active</validityStatus>
                            <validityTimeSpecification>
                                <overallStartTime>2025-09-26T13:18:00.000Z</overallStartTime>
                                <overallEndTime>ENDTIME</overallEndTime>
                            </validityTimeSpecification>
                        </ns2:validity>
                        <ns2:locationReference xsi:type="ns3:SingleRoadLinearLocation">
                            <ns3:alertCLinear xsi:type="ns3:AlertCMethod4Linear">
                                <ns3:alertCLocationCountryCode>6</ns3:alertCLocationCountryCode>
                                <ns3:alertCLocationTableNumber>17</ns3:alertCLocationTableNumber>
                                <ns3:alertCLocationTableVersion>1.11.44</ns3:alertCLocationTableVersion>
                                <ns3:alertCDirection>
                                    <ns3:alertCDirectionCoded>positive</ns3:alertCDirectionCoded>
                                    <ns3:alertCAffectedDirection>unknown</ns3:alertCAffectedDirection>
                                </ns3:alertCDirection>
                                <ns3:alertCMethod4PrimaryPointLocation>
                                    <ns3:alertCLocation>
                                        <ns3:specificLocation>19060</ns3:specificLocation>
                                    </ns3:alertCLocation>
                                    <ns3:offsetDistance>
                                        <ns3:offsetDistance>228</ns3:offsetDistance>
                                    </ns3:offsetDistance>
                                </ns3:alertCMethod4PrimaryPointLocation>
                                <ns3:alertCMethod4SecondaryPointLocation>
                                    <ns3:alertCLocation>
                                        <ns3:specificLocation>19061</ns3:specificLocation>
                                    </ns3:alertCLocation>
                                    <ns3:offsetDistance>
                                        <ns3:offsetDistance>139</ns3:offsetDistance>
                                    </ns3:offsetDistance>
                                </ns3:alertCMethod4SecondaryPointLocation>
                            </ns3:alertCLinear>
                        </ns2:locationReference>
                        <ns2:authorityOperationType>survey</ns2:authorityOperationType>
                    </ns2:situationRecord>
                </ns2:situation>
            </ns2:payload>
            """;

    private static final String ROADWORK_DATEXII_2_2_3 = """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?><d2LogicalModel modelBaseVersion="2" xsi:schemaLocation="http://datex2.eu/schema/2/2_0 https://raw.githubusercontent.com/tmfg/metadata/master/schema/DATEXIISchema_2_2_3_with_definitions_FI.xsd" xmlns="http://datex2.eu/schema/2/2_0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><exchange><supplierIdentification><country>fi</country><nationalIdentifier>FTA</nationalIdentifier></supplierIdentification></exchange><payloadPublication xsi:type="SituationPublication" lang="fi"><publicationTime>2025-09-26T13:19:46.460Z</publicationTime><publicationCreator><country>fi</country><nationalIdentifier>FTA</nationalIdentifier></publicationCreator><situation id="GUID50442306" version="1"><headerInformation><confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality><informationStatus>real</informationStatus></headerInformation><situationRecord xsi:type="AuthorityOperation" id="GUID5044547601" version="1"><situationRecordCreationTime>2025-09-26T13:18:47.686Z</situationRecordCreationTime><situationRecordVersionTime>2025-09-26T13:19:45.455Z</situationRecordVersionTime><situationRecordFirstSupplierVersionTime>2025-09-26T13:18:47.686Z</situationRecordFirstSupplierVersionTime><probabilityOfOccurrence>certain</probabilityOfOccurrence><severity>high</severity><validity><validityStatus>active</validityStatus><validityTimeSpecification><overallStartTime>2025-09-26T13:18:00.000Z</overallStartTime></validityTimeSpecification></validity><groupOfLocations xsi:type="Linear"><alertCLinear xsi:type="AlertCMethod4Linear"><alertCLocationCountryCode>6</alertCLocationCountryCode><alertCLocationTableNumber>17</alertCLocationTableNumber><alertCLocationTableVersion>1.11.44</alertCLocationTableVersion><alertCDirection><alertCDirectionCoded>unknown</alertCDirectionCoded></alertCDirection><alertCMethod4PrimaryPointLocation><alertCLocation><specificLocation>19060</specificLocation></alertCLocation><offsetDistance><offsetDistance>228</offsetDistance></offsetDistance></alertCMethod4PrimaryPointLocation><alertCMethod4SecondaryPointLocation><alertCLocation><specificLocation>19061</specificLocation></alertCLocation><offsetDistance><offsetDistance>139</offsetDistance></offsetDistance></alertCMethod4SecondaryPointLocation></alertCLinear></groupOfLocations><authorityOperationType>survey</authorityOperationType></situationRecord></situation></payloadPublication></d2LogicalModel>""";

    private static final String EXEMPTED_TRANSPORT_3_5 = """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?><ns15:payload xsi:type="ns6:SituationPublication" lang="fi" xmlns="http://datex2.eu/schema/3/trafficManagementPlan" xmlns:ns2="http://datex2.eu/schema/3/common" xmlns:ns4="http://datex2.eu/schema/3/facilities" xmlns:ns3="http://datex2.eu/schema/3/roadTrafficData" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns6="http://datex2.eu/schema/3/situation" xmlns:ns5="http://datex2.eu/schema/3/parking" xmlns:ns8="http://datex2.eu/schema/3/commonExtension" xmlns:ns7="http://datex2.eu/schema/3/urbanExtensions" xmlns:ns13="http://datex2.eu/schema/3/energyInfrastructure" xmlns:ns9="http://datex2.eu/schema/3/locationExtension" xmlns:ns12="http://datex2.eu/schema/3/vms" xmlns:ns11="http://datex2.eu/schema/3/reroutingManagementEnhanced" xmlns:ns10="http://datex2.eu/schema/3/locationReferencing" xmlns:ns15="http://datex2.eu/schema/3/d2Payload" xmlns:ns14="http://datex2.eu/schema/3/faultAndStatus"><ns2:publicationTime>2025-09-30T04:46:31.999Z</ns2:publicationTime><ns2:publicationCreator><ns2:country>FI</ns2:country><ns2:nationalIdentifier>FTA</ns2:nationalIdentifier></ns2:publicationCreator><ns6:situation id="GUID50442308"><ns6:headerInformation><ns2:confidentiality>restrictedToAuthoritiesAndTrafficOperators</ns2:confidentiality><ns2:informationStatus>real</ns2:informationStatus></ns6:headerInformation><ns6:situationRecord xsi:type="ns6:VehicleObstruction" id="GUID5044547801" version="1"><ns6:situationRecordCreationTime>2025-09-30T04:46:31.010Z</ns6:situationRecordCreationTime><ns6:situationRecordVersionTime>2025-09-30T04:46:31.012Z</ns6:situationRecordVersionTime><ns6:situationRecordFirstSupplierVersionTime>2025-09-30T04:46:31.010Z</ns6:situationRecordFirstSupplierVersionTime><ns6:probabilityOfOccurrence>certain</ns6:probabilityOfOccurrence><ns6:validity><ns2:validityStatus>definedByValidityTimeSpec</ns2:validityStatus><ns2:validityTimeSpecification><ns2:overallStartTime>2025-09-30T07:00:00.000Z</ns2:overallStartTime><ns2:overallEndTime>2025-10-01T09:00:00.000Z</ns2:overallEndTime></ns2:validityTimeSpecification></ns6:validity><ns6:locationReference xsi:type="ns10:LocationGroupByList"><ns10:locationContainedInGroup xsi:type="ns10:AreaLocation"><ns10:alertCArea><ns10:alertCLocationCountryCode>6</ns10:alertCLocationCountryCode><ns10:alertCLocationTableNumber>17</ns10:alertCLocationTableNumber><ns10:alertCLocationTableVersion>1.11.44</ns10:alertCLocationTableVersion><ns10:areaLocation><ns10:specificLocation>119</ns10:specificLocation></ns10:areaLocation></ns10:alertCArea></ns10:locationContainedInGroup><ns10:locationContainedInGroup xsi:type="ns10:AreaLocation"><ns10:alertCArea><ns10:alertCLocationCountryCode>6</ns10:alertCLocationCountryCode><ns10:alertCLocationTableNumber>17</ns10:alertCLocationTableNumber><ns10:alertCLocationTableVersion>1.11.44</ns10:alertCLocationTableVersion><ns10:areaLocation><ns10:specificLocation>83</ns10:specificLocation></ns10:areaLocation></ns10:alertCArea></ns10:locationContainedInGroup></ns6:locationReference><ns6:vehicleObstructionType>other</ns6:vehicleObstructionType></ns6:situationRecord><ns6:situationRecord xsi:type="ns6:RoadOrCarriagewayOrLaneManagement" id="GUID5044547802" version="1"><ns6:situationRecordCreationTime>2025-09-30T04:46:31.010Z</ns6:situationRecordCreationTime><ns6:situationRecordVersionTime>2025-09-30T04:46:31.012Z</ns6:situationRecordVersionTime><ns6:situationRecordFirstSupplierVersionTime>2025-09-30T04:46:31.010Z</ns6:situationRecordFirstSupplierVersionTime><ns6:probabilityOfOccurrence>certain</ns6:probabilityOfOccurrence><ns6:validity><ns2:validityStatus>definedByValidityTimeSpec</ns2:validityStatus><ns2:validityTimeSpecification><ns2:overallStartTime>2025-09-30T07:00:00.000Z</ns2:overallStartTime><ns2:overallEndTime>2025-10-01T09:00:00.000Z</ns2:overallEndTime></ns2:validityTimeSpecification></ns6:validity><ns6:locationReference xsi:type="ns10:LocationGroupByList"><ns10:locationContainedInGroup xsi:type="ns10:AreaLocation"><ns10:alertCArea><ns10:alertCLocationCountryCode>6</ns10:alertCLocationCountryCode><ns10:alertCLocationTableNumber>17</ns10:alertCLocationTableNumber><ns10:alertCLocationTableVersion>1.11.44</ns10:alertCLocationTableVersion><ns10:areaLocation><ns10:specificLocation>119</ns10:specificLocation></ns10:areaLocation></ns10:alertCArea></ns10:locationContainedInGroup><ns10:locationContainedInGroup xsi:type="ns10:AreaLocation"><ns10:alertCArea><ns10:alertCLocationCountryCode>6</ns10:alertCLocationCountryCode><ns10:alertCLocationTableNumber>17</ns10:alertCLocationTableNumber><ns10:alertCLocationTableVersion>1.11.44</ns10:alertCLocationTableVersion><ns10:areaLocation><ns10:specificLocation>83</ns10:specificLocation></ns10:areaLocation></ns10:alertCArea></ns10:locationContainedInGroup></ns6:locationReference><ns6:complianceOption>mandatory</ns6:complianceOption><ns6:roadOrCarriagewayOrLaneManagementType>intermittentShortTermClosures</ns6:roadOrCarriagewayOrLaneManagementType></ns6:situationRecord><ns6:situationRecord xsi:type="ns6:VehicleObstruction" id="GUID5044547803" version="1"><ns6:situationRecordCreationTime>2025-09-30T04:46:31.010Z</ns6:situationRecordCreationTime><ns6:situationRecordVersionTime>2025-09-30T04:46:31.012Z</ns6:situationRecordVersionTime><ns6:situationRecordFirstSupplierVersionTime>2025-09-30T04:46:31.010Z</ns6:situationRecordFirstSupplierVersionTime><ns6:probabilityOfOccurrence>certain</ns6:probabilityOfOccurrence><ns6:validity><ns2:validityStatus>definedByValidityTimeSpec</ns2:validityStatus><ns2:validityTimeSpecification><ns2:overallStartTime>2025-09-30T07:00:00.000Z</ns2:overallStartTime><ns2:overallEndTime>2025-10-01T09:00:00.000Z</ns2:overallEndTime></ns2:validityTimeSpecification></ns6:validity><ns6:locationReference xsi:type="ns10:LocationGroupByList"><ns10:locationContainedInGroup xsi:type="ns10:AreaLocation"><ns10:alertCArea><ns10:alertCLocationCountryCode>6</ns10:alertCLocationCountryCode><ns10:alertCLocationTableNumber>17</ns10:alertCLocationTableNumber><ns10:alertCLocationTableVersion>1.11.44</ns10:alertCLocationTableVersion><ns10:areaLocation><ns10:specificLocation>119</ns10:specificLocation></ns10:areaLocation></ns10:alertCArea></ns10:locationContainedInGroup><ns10:locationContainedInGroup xsi:type="ns10:AreaLocation"><ns10:alertCArea><ns10:alertCLocationCountryCode>6</ns10:alertCLocationCountryCode><ns10:alertCLocationTableNumber>17</ns10:alertCLocationTableNumber><ns10:alertCLocationTableVersion>1.11.44</ns10:alertCLocationTableVersion><ns10:areaLocation><ns10:specificLocation>83</ns10:specificLocation></ns10:areaLocation></ns10:alertCArea></ns10:locationContainedInGroup></ns6:locationReference><ns6:vehicleObstructionType>abnormalLoad</ns6:vehicleObstructionType></ns6:situationRecord></ns6:situation></ns15:payload>""";

    private MockHttpServletResponse getResponse(final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);

        if (url.contains("datex2")) {
            get.contentType(MediaType.APPLICATION_XML);
        } else {
            get.contentType(MediaType.APPLICATION_JSON);
        }
        final ResultActions result = mockMvc.perform(get);

        return result.andReturn().getResponse();
    }

    private void insertSituation(final SituationType situationType, final MessageTypeEnum messageType,
                                 final String version, final String message) throws ParseException {
        final Geometry geometry = PostgisGeometryUtils.convertGeoJsonGeometryToGeometry("""
                {
                    "type": "Point",
                    "coordinates": [24.0, 61.0]
                  }
                """);

        final var situation = new DataDatex2Situation("id1", 1L, situationType,
                geometry, ZonedDateTime.now(), ZonedDateTime.now().minusMinutes(30), ZonedDateTime.now().plusMinutes(20));

        final var situationMessage = new DataDatex2SituationMessage(version, messageType.value(), message);
        situation.addMessage(situationMessage);

        dataDatex2SituationRepository.save(situation);
    }

    @Test
    public void noMessages_35() throws Exception {
        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + "/messages/id1/datex2-3.5.xml");

        ResponseAsserter.notFound(response).expectJson();
    }

    @Test
    public void withMessages_35() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + "/messages/id1/datex2-3.5.xml");

        XmlAsserter.ok(response).run();
    }

    @Test
    public void message223() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_2_2_3.version, ROADWORK_DATEXII_2_2_3);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + "/messages/id1/datex2-2.2.3.xml");

        XmlAsserter.ok(response).run();
        assertValidXml223(response);
    }

    @Test
    public void message223NotFound() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + "/messages/id1/datex2-2.2.3.xml");

        ResponseAsserter.notFound(response).run();
    }

    @Test
    public void messageWithoutGeometry() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + "/messages/id1?includeAreaGeometry=false");
        Assertions.assertFalse(response.getContentAsString().contains("coordinates"));

        JsonAsserter.ok(response).run();
    }

    @Test
    public void messageWithGeometry() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + "/messages/id1?includeAreaGeometry=true");
        Assertions.assertTrue(response.getContentAsString().contains("coordinates"));

        JsonAsserter.ok(response).run();
    }

    @Test
    public void trafficAnnouncement35() throws Exception {
        insertSituation(SituationType.TRAFFIC_ANNOUNCEMENT, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS + DATEX2_3_5);

        XmlAsserter.ok(response).expectContent(xmlNode -> {
            Assertions.assertEquals("ns2:SituationPublication", xmlNode.get("type").textValue());

            final var situation = xmlNode.get("situation");
            Assertions.assertNotNull(situation);
            Assertions.assertEquals("GUID50442306", situation.get("id").textValue());
        });
    }

    @Test
    public void roadWorks35NoData() throws Exception {
        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_3_5);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains("http://datex2.eu"));
        Assertions.assertEquals("application/xml;charset=UTF-8", response.getContentType());

        XmlAsserter.ok(response).run();
    }

    @Test
    public void roadWorksJson() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS);

        JsonAsserter.ok(response)
                .expectType("FeatureCollection")
                .expectFeatureCount(1)
                .run();
    }

    @Test
    public void roadWorksJsonWithBrokenBoundingBox() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + "?xMin=22&xMax=23");

        JsonAsserter.bad(response).run();
    }
    @Test
    public void roadWorksJsonWithInvalidBoundingBox() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + "?xMin=22&xMax=23&yMin=1&yMax=4");

        JsonAsserter.bad(response).run();
    }

    @Test
    public void roadWorksJsonWithBoundingBox() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + "?xMin=22&xMax=31&yMin=60&yMax=70");

        JsonAsserter.ok(response)
                .expectType("FeatureCollection")
                .expectFeatureCount(1)
                .run();
    }

    @Test
    public void roadWorksJsonWithWarpedBoundingBox() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + "?xMin=31&xMax=22&yMin=60&yMax=70");

        JsonAsserter.ok(response)
                .expectType("FeatureCollection")
                .expectFeatureCount(1)
                .run();
    }

    @Test
    public void roadWorksJsonWithBoundingBoxNoHits() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + "?xMin=21&xMax=22&yMin=60&yMax=61");

        JsonAsserter.ok(response)
                .expectType("FeatureCollection")
                .expectFeatureCount(0)
                .run();
    }

    @Test
    public void roadWorks35LimitFromHits() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_3_5 + String.format("?from=%s", TIME_NOW));

        XmlAsserter.ok(response).expectContent(xmlNode -> {
            Assertions.assertEquals("ns2:SituationPublication", xmlNode.get("type").textValue());

            final var situation = xmlNode.get("situation");
            Assertions.assertNotNull(situation);
        });
    }

    @Test
    public void roadWorks35LimitFromMisses() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_3_5 + String.format("?from=%s", TIME_FUTURE));

        XmlAsserter.ok(response).expectContent(xmlNode -> {
            Assertions.assertEquals("ns2:SituationPublication", xmlNode.get("type").textValue());

            final var situation = xmlNode.get("situation");
            Assertions.assertNull(situation);
        });
    }

    @Test
    public void roadWorks35LimitToHits() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_3_5 + String.format("?to=%s", TIME_NOW));

        XmlAsserter.ok(response).expectContent(xmlNode -> {
            Assertions.assertEquals("ns2:SituationPublication", xmlNode.get("type").textValue());

            final var situation = xmlNode.get("situation");
            Assertions.assertNotNull(situation);
        });
    }

    @Test
    public void roadWorks35LimitToMisses() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_3_5 + String.format("?from=%s&to=%s", TIME_PAST, TIME_PAST));

        XmlAsserter.ok(response).expectContent(xmlNode -> {
            Assertions.assertEquals("ns2:SituationPublication", xmlNode.get("type").textValue());

            final var situation = xmlNode.get("situation");
            Assertions.assertNull(situation);
        });
    }

    private void assertValidXml223(final MockHttpServletResponse response)
            throws UnsupportedEncodingException, JsonProcessingException {
        XmlAsserter.ok(response).expectContent(xmlNode -> {
            final var publication = xmlNode.get("payloadPublication");
            Assertions.assertNotNull(publication);

            Assertions.assertEquals("SituationPublication", publication.get("type").textValue());

            final var situation = publication.get("situation");
            Assertions.assertNotNull(situation);

            Assertions.assertEquals("GUID50442306", situation.get("id").textValue());
        });
    }

    @Test
    public void roadWorks223() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_2_2_3.version, ROADWORK_DATEXII_2_2_3);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_2_2_3);

        assertValidXml223(response);
    }

    @Test
    public void trafficAnnouncement223() throws Exception {
        insertSituation(SituationType.TRAFFIC_ANNOUNCEMENT, MessageTypeEnum.DATEX_2, Datex2Version.V_2_2_3.version, ROADWORK_DATEXII_2_2_3);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS + DATEX2_2_2_3);

        assertValidXml223(response);
    }

    @Test
    public void weightRestriction223() throws Exception {
        insertSituation(SituationType.WEIGHT_RESTRICTION, MessageTypeEnum.DATEX_2, Datex2Version.V_2_2_3.version, ROADWORK_DATEXII_2_2_3);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS + DATEX2_2_2_3);

        assertValidXml223(response);
    }

    @Test
    public void exemptedTransport223() throws Exception {
        insertSituation(SituationType.EXEMPTED_TRANSPORT, MessageTypeEnum.DATEX_2, Datex2Version.V_2_2_3.version, ROADWORK_DATEXII_2_2_3);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS + DATEX2_2_2_3);

        assertValidXml223(response);
    }

    private void assertValid35(final MockHttpServletResponse response, final String expectedGuid)
            throws UnsupportedEncodingException, JsonProcessingException {
        XmlAsserter.ok(response).expectContent(xmlNode -> {
            Assertions.assertEquals("ns2:SituationPublication", xmlNode.get("type").textValue());

            final var situation = xmlNode.get("situation");
            Assertions.assertNotNull(situation);
            Assertions.assertEquals(expectedGuid, situation.get("id").textValue());
        });
    }

    @Test
    public void weightRestrictionsWorks35() throws Exception {
        insertSituation(SituationType.WEIGHT_RESTRICTION, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS + DATEX2_3_5);

        assertValid35(response, "GUID50442306");
    }


    @Test
    public void exemptedTransports35() throws Exception {
        insertSituation(SituationType.EXEMPTED_TRANSPORT, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, EXEMPTED_TRANSPORT_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS + DATEX2_3_5);

        assertValid35(response, "GUID50442308");
    }

    @Test
    public void roadWorks35() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, ROADWORK_DATEXII_3_5);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + ROADWORKS + DATEX2_3_5);

        assertValid35(response, "GUID50442306");
    }

    @Test
    public void messageHistoryEmpty() throws Exception {
        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + "/messages/id1/history");

        ResponseAsserter.notFound(response).run();
    }

    @Test
    public void messageHistory() throws Exception {
        insertSituation(SituationType.ROAD_WORK, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + "/messages/id1/history");

        JsonAsserter.ok(response).expectContent(jsonNode -> Assertions.assertEquals(1, jsonNode.size())
        );
    }

    @Test
    public void trafficAnnouncementJson() throws Exception {
        insertSituation(SituationType.TRAFFIC_ANNOUNCEMENT, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + TRAFFIC_ANNOUNCEMENTS);

        JsonAsserter.ok(response)
                .expectType("FeatureCollection")
                .expectFeatureCount(1)
                .run();
    }

    @Test
    public void weightRestrictionsJson() throws Exception {
        insertSituation(SituationType.WEIGHT_RESTRICTION, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + WEIGHT_RESTRICTIONS);

        JsonAsserter.ok(response)
                .expectType("FeatureCollection")
                .expectFeatureCount(1)
                .run();
    }

    @Test
    public void exemptedTransportsJson() throws Exception {
        insertSituation(SituationType.EXEMPTED_TRANSPORT, MessageTypeEnum.SIMPPELI, "0.2.17", SIMPPELI);

        final var response = getResponse(API_TRAFFIC_MESSAGE_BETA + EXEMPTED_TRANSPORTS);

        JsonAsserter.ok(response)
                .expectType("FeatureCollection")
                .expectFeatureCount(1)
                .run();
    }
}
