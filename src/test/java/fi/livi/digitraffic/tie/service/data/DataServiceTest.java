package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.AbstractSpringJUnitTest;

import fi.livi.digitraffic.tie.dao.data.DataDatex2SituationRepository;
import fi.livi.digitraffic.tie.dao.data.DataIncomingRepository;

import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.model.data.DataDatex2Situation;
import fi.livi.digitraffic.tie.model.data.DataDatex2SituationMessage;
import fi.livi.digitraffic.tie.model.data.DataIncoming;
import fi.livi.digitraffic.tie.model.data.IncomingDataTypes;

import java.time.Instant;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
public class DataServiceTest extends AbstractSpringJUnitTest {
    @Autowired
    private DataUpdatingService dataUpdatingService;

    @Autowired
    private DataIncomingRepository dataIncomingRepository;

    @Autowired
    private DataDatex2SituationRepository dataDatex2SituationRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @BeforeEach
    void cleanup() {
        transactionTemplate.executeWithoutResult(_ -> {
            entityManager.createNativeQuery("DELETE FROM data_datex2_situation_message").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM data_datex2_situation").executeUpdate();
            dataIncomingRepository.deleteAll();
        });
    }

    private static final String IMS_MESSAGE = """
            {
                "messageId": 4,
                "messageContent": {
                    "messages": [MESSAGES]
                }
            }
            """;

    public static final String SIMPPELI_0_2_17 = """
            {"geometry":{"type":"Point","coordinates":[25.534783,65.027065]},"type":"Feature","properties":{"situationId":"GUID50000221","version":1,"situationType":"traffic announcement","trafficAnnouncementType":"general","releaseTime":"2025-09-04T11:53:40.955Z","versionTime":"2025-09-04T11:53:55.331Z","announcements":[{"language":"fi","title":"Tie 9, Lieto, Aura. Liikennetiedote. ","location":{"countryCode":6,"locationTableNumber":17,"locationTableVersion":"1.11.31","description":"Tie 9 välillä Turku - Tampere.\\nTarkempi paikka: Paikasta Päivästön risteyssilta 600 m, vaikutusalue 1,0 km, suuntaan Liedon aseman liittymä."},"locationDetails":{"roadAddressLocation":{"primaryPoint":{"municipality":"Aura","province":"Varsinais-Suomi","country":"Suomi","roadAddress":{"road":9,"roadSection":106,"distance":69},"alertCLocation":{"locationCode":22759,"name":"Päivästön risteyssilta","distance":550}},"secondaryPoint":{"municipality":"Lieto","province":"Varsinais-Suomi","country":"Suomi","roadAddress":{"road":9,"roadSection":105,"distance":3},"alertCLocation":{"locationCode":41801,"name":"Liedon aseman liittymä","distance":550}},"direction":"unknown"}},"features":[{"name":"Huono ajokeli"},{"name":"Kova sivutuuli vaikeuttaa liikennettä"}],"timeAndDuration":{"startTime":"2025-09-04T11:53:00Z"},"additionalInformation":"Liikenne- ja kelitiedot verkossa: https://liikennetilanne.fintraffic.fi/","sender":"Liikenneviraston tieliikennekeskus Tampere"}],"contact":{"phone":"0206373330","email":"tampere.liikennekeskus@liikennevirasto.fi"}}}
            """;
    private static final String DATEX_3_5 = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?><ns16:payload xsi:type="ns8:SituationPublication" lang="fi" xmlns:ns2="http://datex2.eu/schema/3/vms" xmlns:ns4="http://datex2.eu/schema/3/locationExtension" xmlns:ns3="http://datex2.eu/schema/3/locationReferencing" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ns6="http://datex2.eu/schema/3/faultAndStatus" xmlns:ns5="http://datex2.eu/schema/3/common" xmlns:ns8="http://datex2.eu/schema/3/situation" xmlns:ns7="http://datex2.eu/schema/3/parking" xmlns:ns13="http://datex2.eu/schema/3/trafficManagementPlan" xmlns:ns9="http://datex2.eu/schema/3/urbanExtensions" xmlns:ns12="http://datex2.eu/schema/3/facilities" xmlns:ns11="http://datex2.eu/schema/3/roadTrafficData" xmlns:ns10="http://datex2.eu/schema/3/commonExtension" xmlns:ns16="http://datex2.eu/schema/3/d2Payload" xmlns:ns15="http://datex2.eu/schema/3/energyInfrastructure" xmlns:ns14="http://datex2.eu/schema/3/reroutingManagementEnhanced"><ns5:publicationTime>2025-09-04T11:53:55.719Z</ns5:publicationTime><ns5:publicationCreator><ns5:country>FI</ns5:country><ns5:nationalIdentifier>FTA</ns5:nationalIdentifier></ns5:publicationCreator><ns8:situation id="GUID50000221"><ns8:headerInformation><ns5:confidentiality>restrictedToAuthoritiesAndTrafficOperators</ns5:confidentiality><ns5:informationStatus>real</ns5:informationStatus></ns8:headerInformation><ns8:situationRecord xsi:type="ns8:PoorEnvironmentConditions" id="GUID5000022401" version="1"><ns8:situationRecordCreationTime>2025-09-04T11:53:40.955Z</ns8:situationRecordCreationTime><ns8:situationRecordVersionTime>2025-09-04T11:53:55.331Z</ns8:situationRecordVersionTime><ns8:situationRecordFirstSupplierVersionTime>2025-09-04T11:53:40.955Z</ns8:situationRecordFirstSupplierVersionTime><ns8:probabilityOfOccurrence>certain</ns8:probabilityOfOccurrence><ns8:severity>high</ns8:severity><ns8:validity><ns5:validityStatus>active</ns5:validityStatus><ns5:validityTimeSpecification><ns5:overallStartTime>2025-09-04T11:53:00.000Z</ns5:overallStartTime></ns5:validityTimeSpecification></ns8:validity><ns8:locationReference xsi:type="ns3:SingleRoadLinearLocation"><ns3:alertCLinear xsi:type="ns3:AlertCMethod4Linear"><ns3:alertCLocationCountryCode>6</ns3:alertCLocationCountryCode><ns3:alertCLocationTableNumber>17</ns3:alertCLocationTableNumber><ns3:alertCLocationTableVersion>1.11.31</ns3:alertCLocationTableVersion><ns3:alertCDirection><ns3:alertCDirectionCoded>positive</ns3:alertCDirectionCoded><ns3:alertCAffectedDirection>unknown</ns3:alertCAffectedDirection></ns3:alertCDirection><ns3:alertCMethod4PrimaryPointLocation><ns3:alertCLocation><ns3:specificLocation>22759</ns3:specificLocation></ns3:alertCLocation><ns3:offsetDistance><ns3:offsetDistance>550</ns3:offsetDistance></ns3:offsetDistance></ns3:alertCMethod4PrimaryPointLocation><ns3:alertCMethod4SecondaryPointLocation><ns3:alertCLocation><ns3:specificLocation>41801</ns3:specificLocation></ns3:alertCLocation><ns3:offsetDistance><ns3:offsetDistance>550</ns3:offsetDistance></ns3:offsetDistance></ns3:alertCMethod4SecondaryPointLocation></ns3:alertCLinear></ns8:locationReference><ns8:drivingConditionType>hazardous</ns8:drivingConditionType><ns8:poorEnvironmentType>crosswinds</ns8:poorEnvironmentType></ns8:situationRecord></ns8:situation></ns16:payload>""";

    private static final String DATEX_2_3 = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?><d2LogicalModel modelBaseVersion="2" xsi:schemaLocation="http://datex2.eu/schema/2/2_0 https://raw.githubusercontent.com/tmfg/metadata/master/schema/DATEXIISchema_2_2_3_with_definitions_FI.xsd" xmlns="http://datex2.eu/schema/2/2_0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><exchange><supplierIdentification><country>fi</country><nationalIdentifier>FTA</nationalIdentifier></supplierIdentification></exchange><payloadPublication xsi:type="SituationPublication" lang="fi"><publicationTime>2025-09-04T11:53:55.719Z</publicationTime><publicationCreator><country>fi</country><nationalIdentifier>FTA</nationalIdentifier></publicationCreator><situation id="GUID50000221" version="1"><headerInformation><confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality><informationStatus>real</informationStatus></headerInformation><situationRecord xsi:type="PoorEnvironmentConditions" id="GUID5000022401" version="1"><situationRecordCreationTime>2025-09-04T11:53:40.955Z</situationRecordCreationTime><situationRecordVersionTime>2025-09-04T11:53:55.331Z</situationRecordVersionTime><situationRecordFirstSupplierVersionTime>2025-09-04T11:53:40.955Z</situationRecordFirstSupplierVersionTime><probabilityOfOccurrence>certain</probabilityOfOccurrence><severity>high</severity><validity><validityStatus>active</validityStatus><validityTimeSpecification><overallStartTime>2025-09-04T11:53:00.000Z</overallStartTime></validityTimeSpecification></validity><groupOfLocations xsi:type="Linear"><alertCLinear xsi:type="AlertCMethod4Linear"><alertCLocationCountryCode>6</alertCLocationCountryCode><alertCLocationTableNumber>17</alertCLocationTableNumber><alertCLocationTableVersion>1.11.31</alertCLocationTableVersion><alertCDirection><alertCDirectionCoded>unknown</alertCDirectionCoded></alertCDirection><alertCMethod4PrimaryPointLocation><alertCLocation><specificLocation>22759</specificLocation></alertCLocation><offsetDistance><offsetDistance>550</offsetDistance></offsetDistance></alertCMethod4PrimaryPointLocation><alertCMethod4SecondaryPointLocation><alertCLocation><specificLocation>41801</specificLocation></alertCLocation><offsetDistance><offsetDistance>550</offsetDistance></offsetDistance></alertCMethod4SecondaryPointLocation></alertCLinear></groupOfLocations><drivingConditionType>hazardous</drivingConditionType><poorEnvironmentType>crosswinds</poorEnvironmentType></situationRecord></situation></payloadPublication></d2LogicalModel>""";

    private void assertIncomingData(final int expectedNew, final int expectedFailed, final int expectedProcessed) {
        final var allData = dataIncomingRepository.findAll();
        var newCount = 0;
        var failedCount = 0;
        var processedCount = 0;

        for(final DataIncoming data : allData) {
            switch (data.getStatus()) {
                case NEW: newCount++; break;
                case FAILED: failedCount++; break;
                case PROCESSED: processedCount++; break;
            }
        }

        Assertions.assertEquals(expectedNew, newCount, "New count should be " + expectedNew);
        Assertions.assertEquals(expectedFailed, failedCount, "Failed count should be " + expectedFailed);
        Assertions.assertEquals(expectedProcessed, processedCount, "Processed count should be " + expectedProcessed);
    }

    private void assertDataDatex2(final int expectedCount, final String version) {
        transactionTemplate.executeWithoutResult(status -> {
            final var allSituations = dataDatex2SituationRepository.findAll();
            Assertions.assertEquals(1, allSituations.size());
            final var situation = allSituations.getFirst();

            final var messageCount = situation.getMessages().stream().filter(f -> f.getMessageVersion().equals(version)).count();
            Assertions.assertEquals(expectedCount, messageCount);
        });
    }

    private void insertNewData(final String data) {
        final var incoming = DataIncoming.ims122("1234", data);
        dataUpdatingService.insertData(incoming);
    }

    private String createMessage(final String type, final String version, final String content) {
        return """
                {
                    "type": "TYPE",
                    "version": "VERSION",
                    "content": "CONTENT"
                }
                """
                .replace("TYPE", type)
                .replace("VERSION", version)
                .replace("CONTENT", StringEscapeUtils.escapeJson(content));
    }

    private String createImsMessage(final String ...messages) {
        return IMS_MESSAGE.replace("MESSAGES", String.join(",\n", messages));
    }

    @Test
    public void handleEmpty() {
        dataUpdatingService.handleNewData();
        assertIncomingData(0, 0, 0);
    }

    @Test
    public void handleDatex2_35() {
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.5", DATEX_3_5)));
        assertIncomingData(1, 0, 0);

        dataUpdatingService.handleNewData();
        assertIncomingData(0, 0, 1);
        assertDataDatex2(1, "3.5");
    }

    @Test
    public void handleDatex2_23() {
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "2.3", DATEX_2_3)));
        assertIncomingData(1, 0, 0);

        dataUpdatingService.handleNewData();
        assertIncomingData(0, 0, 1);
        assertDataDatex2(1, "2.3");
    }

    @Test
    public void handleInfoXml() {
        insertNewData(createImsMessage(createMessage("INFOXML", "2.1", DATEX_3_5)));
        assertIncomingData(1, 0, 0);

        dataUpdatingService.handleNewData();
        assertIncomingData(0, 1, 0);
    }

    @Test
    public void handleUpdateWithTwoVersions() {
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.5", DATEX_3_5),
                createMessage("DATEX2", "2.3", DATEX_2_3)));
        dataUpdatingService.handleNewData();
        assertDataDatex2(1, "3.5");
        assertDataDatex2(1, "2.3");
    }

    /**
     * When a duplicate situation (same situationId + version) is in the batch,
     * other valid messages in the same batch must still be processed successfully.
     */
    @Test
    public void duplicateSituationDoesNotBlockOtherMessages() {
        // First: insert and process a message normally
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.5", DATEX_3_5)));
        dataUpdatingService.handleNewData();
        assertIncomingData(0, 0, 1);

        // Now insert a duplicate (same situationId GUID50000221, version 1) AND a different valid message
        final String simppeli2 = SIMPPELI_0_2_17.replace("GUID50000221", "GUID50000222");
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.5", DATEX_3_5)));
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", simppeli2),
                createMessage("DATEX2", "3.5", DATEX_3_5)));

        dataUpdatingService.handleNewData();

        // The duplicate should be marked FAILED, the valid one PROCESSED
        // Total: 1 processed (first run) + 1 failed (duplicate) + 1 processed (new situation) = 3
        assertIncomingData(0, 1, 2);
    }

    private void assertFailedWithReason(final String expectedReasonPart) {
        final var failed = dataIncomingRepository.findAll().stream()
                .filter(d -> d.getStatus() == IncomingDataTypes.DataStatus.FAILED)
                .toList();

        Assertions.assertEquals(1, failed.size(), "Exactly one row should have failed");
        Assertions.assertNotNull(failed.getFirst().getFailureReason(), "Failure reason should be stored");
        Assertions.assertTrue(failed.getFirst().getFailureReason().contains(expectedReasonPart),
                "Failure reason should mention '" + expectedReasonPart + "' but was: "
                        + failed.getFirst().getFailureReason());
    }

    private void assertNoSituationsStored() {
        transactionTemplate.executeWithoutResult(status ->
                Assertions.assertEquals(0, dataDatex2SituationRepository.findAll().size(),
                        "Nothing should be stored when the message is rejected"));
    }

    /**
     * The source has been seen to send the literal text {@code null} instead of a Datex II message.
     * Such content is not blank, so it used to pass validation and be stored verbatim, producing a row
     * that is valid in the database but unusable as XML.
     */
    @Test
    public void literalNullContentIsRejectedAndReasonStored() {
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.5", DATEX_3_5),
                createMessage("DATEX2", "3.7", "null")));

        dataUpdatingService.handleNewData();

        assertIncomingData(0, 1, 0);
        assertFailedWithReason("literal text 'null'");
        assertNoSituationsStored();
    }

    @Test
    public void blankContentIsRejectedAndReasonStored() {
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.7", "   ")));

        dataUpdatingService.handleNewData();

        assertIncomingData(0, 1, 0);
        assertFailedWithReason("content is blank");
        assertNoSituationsStored();
    }

    @Test
    public void datexContentThatIsNotXmlIsRejectedAndReasonStored() {
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.7", "Internal server error")));

        dataUpdatingService.handleNewData();

        assertIncomingData(0, 1, 0);
        assertFailedWithReason("does not start with an XML tag");
        assertNoSituationsStored();
    }

    /**
     * The simple json part is mandatory: without it the situation cannot be created. The reason comes
     * from the exception message rather than from the content checks, so it is worth verifying that it
     * reaches the database too.
     */
    @Test
    public void messageWithoutSimpleJsonIsMarkedFailedWithReason() {
        insertNewData(createImsMessage(createMessage("DATEX2", "3.5", DATEX_3_5)));

        dataUpdatingService.handleNewData();

        assertIncomingData(0, 1, 0);
        assertFailedWithReason("No Simple-json found");
        assertNoSituationsStored();
    }

    /**
     * A rejected message must stay in the database so the broken content can be inspected,
     * and it must not be picked up again by the processing job.
     */
    @Test
    public void rejectedMessageIsKeptAndNotRetried() {
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.7", "null")));

        dataUpdatingService.handleNewData();
        dataUpdatingService.handleNewData();

        assertIncomingData(0, 1, 0);
        Assertions.assertTrue(dataIncomingRepository.findAll().getFirst().getData().contains("null"),
                "The original message must still be available for inspection");
    }

    /**
     * The situation is created from the simple json alone and the Datex II content is stored without
     * being parsed, so nothing in the write path notices if the two messages of one incoming message
     * describe different situations. This reads both stored messages back and compares them.
     */
    @Test
    public void datex2AndSimpleJsonDescribeTheSameSituation() {
        insertNewData(createImsMessage(
                createMessage("SIMPPELI", "0.2.17", SIMPPELI_0_2_17),
                createMessage("DATEX2", "3.5", DATEX_3_5)));

        dataUpdatingService.handleNewData();

        transactionTemplate.executeWithoutResult(status -> {
            final var situation = dataDatex2SituationRepository.findAll().getFirst();
            final var datex2 = storedMessage(situation, MessageTypeEnum.DATEX2.value());
            final var simpleJson = storedMessage(situation, MessageTypeEnum.SIMPPELI.value());

            Assertions.assertEquals(situation.getSituationId(), valueOf(datex2, "situation id=\"([^\"]+)\""),
                    "The situation id of the Datex II content should be the one the situation was created with");
            Assertions.assertEquals(
                    Instant.parse(valueOf(simpleJson, "\"startTime\":\"([^\"]+)\"")),
                    Instant.parse(valueOf(datex2, "overallStartTime>([^<]+)<")),
                    "Both messages should state the same start time");
        });
    }

    private static String storedMessage(final DataDatex2Situation situation, final String messageType) {
        return situation.getMessages().stream()
                .filter(m -> m.getMessageType().equals(messageType))
                .findFirst()
                .map(DataDatex2SituationMessage::getMessage)
                .orElseThrow(() -> new AssertionError("No " + messageType + " message stored"));
    }

    private static String valueOf(final String message, final String pattern) {
        final var matcher = Pattern.compile(pattern).matcher(message);
        Assertions.assertTrue(matcher.find(), "Pattern " + pattern + " not found in the stored message");
        return matcher.group(1);
    }
}
