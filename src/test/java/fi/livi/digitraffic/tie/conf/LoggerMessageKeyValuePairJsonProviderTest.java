package fi.livi.digitraffic.tie.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;

public class LoggerMessageKeyValuePairJsonProviderTest  {
    private static final Logger log = LoggerFactory.getLogger(LoggerMessageKeyValuePairJsonProviderTest.class);

    final LoggerMessageKeyValuePairJsonProvider provider = new LoggerMessageKeyValuePairJsonProvider();

    private ObjectMapper objectMapper = new ObjectMapper();

    private JsonFactory factory;

    @BeforeEach
    public void init() {
        factory = new JsonFactory();
        factory.setCodec(objectMapper);
    }

    private JsonGenerator createJsonGenerator(final OutputStream out) throws IOException {
        return factory.createGenerator(out, JsonEncoding.UTF8);
    }

    final static String[] ALLOWED_KEYS = {
        "a",
        "a1",
        "a-b",
        "a_b",
        "a.b",
        "abc1",
        "fi.livi",
        "abc_def1",
        "a.b.c123"
    };

    final static String[] NOT_ALLOWED_KEYS = {
        "123",
        "a..b",
        "a__b",
        "a--b",
        "a_.b",
        "a.-b",
        "a_-b",
        "\"",
        "\"&",
        "\"\\'",
        "\"\\001",
        "\"\\002",
        "\"\\002y",
        "\"xMin",
        ",aliverkonPeite",
        ",asemanTila",
        ",id",
        "19.0,violatingParameter\n",
        "OPERATOR(pg_catalog\n",
        "Oy,organisaatio",
        "fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO@5fea7b58[jarjestys",
        "Esiasento@abc",
        "abc[jarjestys",
        "(ka)],sijainti",
        "(ka)",
        "]sijainti",
        "a]sijainti",
        "a[sijainti",
        "a}sijainti",
        "a{sijainti",
        "a)sijainti",
        "a(sijainti",
        "metadata-api?group",
        "a\\/b",
        "a/b",
        };

    @Test
    public void allowedKeys() throws IOException {
        for (String allowedKey : ALLOWED_KEYS) {
            log.info("Test key {}", allowedKey);
            final String result = sendEventWithFormatedMessageAndReturnResultJson(allowedKey + "=bar");
            assertEquals(String.format("{\"%s\":\"bar\"}", allowedKey), result);
        }

    }

    @Test
    public void notAllowedKeys() throws IOException {
        for (String notAllowedKey : NOT_ALLOWED_KEYS) {
            log.info("Test key {}", notAllowedKey);
            final String result = sendEventWithFormatedMessageAndReturnResultJson(notAllowedKey + "=bar");
            assertEquals("{}", result);
        }

    }

    @Test
    public void simpleKeyValuePair() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=bar");
        assertEquals("{\"foo\":\"bar\"}", result);
    }

    @Test
    public void simpleKeyValuePair3() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("healthCheckValue=<status>ok</status>");
        assertEquals("{\"healthCheckValue\":\"<status>ok</status>\"}", result);
    }


    @Test
    public void simpleKeyValuePair2() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("  \n  \t    foo=bar     \n   ");
        assertEquals("{\"foo\":\"bar\"}", result);
    }

    @Test
    public void intValue() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=1");
        assertEquals("{\"foo\":1}", result);
    }

    @Test
    public void doubleValue() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=1.4");
        assertEquals("{\"foo\":1.4}", result);
    }

    @Test
    public void doubleValueWithComma() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=1,4");
        assertEquals("{\"foo\":14}", result);
    }

    @Test
    public void isoDateTimeOffset() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=2020-05-01T12:00+02:00");
        assertEquals("{\"foo\":\"2020-05-01T10:00:00Z\"}", result);
    }

    @Test
    public void isoDateTimeZ() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=2020-05-01T12:00:00Z");
        assertEquals("{\"foo\":\"2020-05-01T12:00:00Z\"}", result);
    }

    @Test
    public void isoDateTimeZMillis() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=2020-05-01T12:00:00.123Z");
        assertEquals("{\"foo\":\"2020-05-01T12:00:00.123Z\"}", result);
    }

    @Test
    public void keyValueChainTakesFirstPair() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=bar=hello=world and=this");
        assertEquals("{\"foo\":\"bar\",\"and\":\"this\"}", result);
    }

    @Test
    public void xmlTagsAreStripped() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("a=b " + LONG_XML);
        assertEquals("{\"a\":\"b\"}", result);
    }

    @Test
    public void xmlTagsAreStripped2() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("a=b " + LONG_XML2);
        assertEquals("{\"a\":\"b\"}", result);
    }

    @Test
    public void nullMessage() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson(null);
        assertEquals("{}", result);
    }

    @Test
    public void emptyMessage() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("");
        assertEquals("{}", result);
    }

    @Test
    public void emptyMessage2() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("    ");
        assertEquals("{}", result);
    }

    @Test
    public void emptyResultWhenSpaces() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson(" a = b ");
        assertEquals("{}", result);
    }

    @Test
    public void emptyResultWhenSpaces2() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo =bar hello= world");
        assertEquals("{}", result);
    }

    @Test
    public void s3VersionId() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("s3VersionId=\"1_9XcT207HmV5yyEExF7GhsaSzUoeNFY\"");
        assertEquals("{\"s3VersionId\":\"1_9XcT207HmV5yyEExF7GhsaSzUoeNFY\"}", result);
    }

    private String sendEventWithFormatedMessageAndReturnResultJson(final String formattedMessage) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final JsonGenerator jsonGenerator = createJsonGenerator(out);
        jsonGenerator.writeStartObject();
        provider.writeTo(jsonGenerator, createEvent(formattedMessage));
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        final String result = out.toString(StandardCharsets.UTF_8);
        log.info("Result: {}", result);
        return result;
    }

    private ILoggingEvent createEvent(final String formattedMessage) {
        return new ILoggingEvent() {
            @Override
            public String getThreadName() {
                return null;
            }

            @Override
            public Level getLevel() {
                return null;
            }

            @Override
            public String getMessage() {
                return null;
            }

            @Override
            public Object[] getArgumentArray() {
                return new Object[0];
            }

            @Override
            public String getFormattedMessage() {
                return formattedMessage;
            }

            @Override
            public String getLoggerName() {
                return null;
            }

            @Override
            public LoggerContextVO getLoggerContextVO() {
                return null;
            }

            @Override
            public IThrowableProxy getThrowableProxy() {
                return null;
            }

            @Override
            public StackTraceElement[] getCallerData() {
                return new StackTraceElement[0];
            }

            @Override
            public boolean hasCallerData() {
                return false;
            }

            @Override
            public Marker getMarker() {
                return null;
            }

            @Override
            public Map<String, String> getMDCPropertyMap() {
                return null;
            }

            @Override
            public Map<String, String> getMdc() {
                return null;
            }

            @Override
            public long getTimeStamp() {
                return 0;
            }

            @Override
            public void prepareForDeferredProcessing() {

            }
        };
    }

    private final static String LONG_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "  <d2LogicalModel xmlns=\"http://datex2.eu/schema/2/2_0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" modelBaseVersion=\"2\" xsi:schemaLocation=\"http://datex2.eu/schema/2/2_0 https://tie-test.digitraffic.fi/schemas/datex2/DATEXIISchema_2_2_3_with_definitions_FI.xsd\">\n" +
            "    <exchange>\n" +
            "      <supplierIdentification>\n" +
            "        <country>fi</country>\n" +
            "        <nationalIdentifier>FTA</nationalIdentifier>\n" +
            "      </supplierIdentification>\n" +
            "    </exchange>\n" +
            "    <payloadPublication xsi:type=\"SituationPublication\" lang=\"fi\">\n" +
            "      <publicationTime>2020-05-11T12:38:56.130Z</publicationTime>\n" +
            "      <publicationCreator>\n" +
            "        <country>fi</country>\n" +
            "        <nationalIdentifier>FTA</nationalIdentifier>\n" +
            "      </publicationCreator>\n" +
            "      <situation id=\"GUID50369644\" version=\"1\">\n" +
            "        <headerInformation>\n" +
            "          <confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality>\n" +
            "          <informationStatus>real</informationStatus>\n" +
            "        </headerInformation>\n" +
            "        <situationRecord xsi:type=\"TransitInformation\" id=\"GUID5037034101\" version=\"1\">\n" +
            "          <situationRecordCreationTime>2020-05-11T12:38:56.130Z</situationRecordCreationTime>\n" +
            "          <situationRecordVersionTime>2020-05-11T12:40:50.394Z</situationRecordVersionTime>\n" +
            "          <situationRecordFirstSupplierVersionTime>2020-05-11T12:38:56.130Z</situationRecordFirstSupplierVersionTime>\n" +
            "          <probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
            "          <severity>low</severity>\n" +
            "          <validity>\n" +
            "            <validityStatus>active</validityStatus>\n" +
            "            <validityTimeSpecification>\n" +
            "              <overallStartTime>2020-05-11T12:38:56.130Z</overallStartTime>\n" +
            "            </validityTimeSpecification>\n" +
            "          </validity>\n" +
            "          <generalPublicComment>\n" +
            "            <comment>\n" +
            "              <values>\n" +
            "                <value lang=\"fi\">Tie 14866, eli Kyläniementie, Ruokolahti. Liikennetiedote. \n" +
            "\n" +
            "Tie 14866, eli Kyläniementie, Ruokolahti.\n" +
            "Tarkempi paikka: Kyläniemen lossi. \n" +
            "\n" +
            "Lautan kantavuus on muuttunut. \n" +
            "\n" +
            "Lisätieto: Lautan kantavuus 44 tonnia.\n" +
            "\n" +
            "Ajankohta: 11.05.2020 klo 15:38 alkaen toistaiseksi.\n" +
            "\n" +
            "\uFEFFLiikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/ \n" +
            "\n" +
            "Tieliikennekeskus Tampere - ITM Finland\n" +
            "Puh: 0206373330\n" +
            "Sähköposti: tampere.liikennekeskus@tmfg.fi\n" +
            "http://extranet.liikennevirasto.fi/webloik/kartta/liikennetilanne/50369644</value>\n" +
            "              </values>\n" +
            "            </comment>\n" +
            "          </generalPublicComment>\n" +
            "          <groupOfLocations xsi:type=\"Point\">\n" +
            "            <alertCPoint xsi:type=\"AlertCMethod2Point\">\n" +
            "              <alertCLocationCountryCode>6</alertCLocationCountryCode>\n" +
            "              <alertCLocationTableNumber>17</alertCLocationTableNumber>\n" +
            "              <alertCLocationTableVersion>1.11.37</alertCLocationTableVersion>\n" +
            "              <alertCDirection>\n" +
            "                <alertCDirectionCoded>unknown</alertCDirectionCoded>\n" +
            "              </alertCDirection>\n" +
            "              <alertCMethod2PrimaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>36971</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "              </alertCMethod2PrimaryPointLocation>\n" +
            "            </alertCPoint>\n" +
            "          </groupOfLocations>\n" +
            "          <transitServiceInformation>loadCapacityChanged</transitServiceInformation>\n" +
            "          <transitServiceType>ferry</transitServiceType>\n" +
            "        </situationRecord>\n" +
            "      </situation>\n" +
            "      <situation id=\"GUID50359968\" version=\"24\">\n" +
            "        <headerInformation>\n" +
            "          <confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality>\n" +
            "          <informationStatus>real</informationStatus>\n" +
            "        </headerInformation>\n" +
            "        <situationRecord xsi:type=\"RoadOrCarriagewayOrLaneManagement\" id=\"GUID5036050601\" version=\"24\">\n" +
            "          <situationRecordCreationTime>2020-05-07T12:27:30.140Z</situationRecordCreationTime>\n" +
            "          <situationRecordVersionTime>2020-05-07T12:29:09.295Z</situationRecordVersionTime>\n" +
            "          <situationRecordFirstSupplierVersionTime>2020-05-07T12:27:30.140Z</situationRecordFirstSupplierVersionTime>\n" +
            "          <probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
            "          <severity>low</severity>\n" +
            "          <validity>\n" +
            "            <validityStatus>definedByValidityTimeSpec</validityStatus>\n" +
            "            <validityTimeSpecification>\n" +
            "              <overallStartTime>2019-06-23T21:00:00.000Z</overallStartTime>\n" +
            "              <overallEndTime>2020-09-29T21:00:00.000Z</overallEndTime>\n" +
            "            </validityTimeSpecification>\n" +
            "          </validity>\n" +
            "          <generalPublicComment>\n" +
            "            <comment>\n" +
            "              <values>\n" +
            "                <value lang=\"fi\">Tie 40, eli Turun kehätie, Kaarina. Liikennetiedote. Tilanne jatkuu. \n" +
            "\n" +
            "Tie 40, eli Turun kehätie, Kaarina.\n" +
            "Tarkempi paikka: Paikasta Hepojoki 800 m, vaikutusalue 500 m, suuntaan Piikkiö, Makarla. \n" +
            "\n" +
            "Tie on suljettu liikenteeltä. \n" +
            "Paikalla on kiertotieopastus. \n" +
            "\n" +
            "Lisätieto: Siltatyö\n" +
            "\n" +
            "\n" +
            "Ajankohta: 24.06.2019 klo 00:00 - 30.09.2020 klo 00:00.\n" +
            "\n" +
            "\uFEFFLiikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/ \n" +
            "\n" +
            "Tieliikennekeskus Turku - ITM Finland\n" +
            "Puh: 0206373329\n" +
            "Sähköposti: turku.liikennekeskus@tmfg.fi\n" +
            "http://extranet.liikennevirasto.fi/webloik/kartta/liikennetilanne/50359968</value>\n" +
            "              </values>\n" +
            "            </comment>\n" +
            "          </generalPublicComment>\n" +
            "          <groupOfLocations xsi:type=\"Linear\">\n" +
            "            <alertCLinear xsi:type=\"AlertCMethod4Linear\">\n" +
            "              <alertCLocationCountryCode>6</alertCLocationCountryCode>\n" +
            "              <alertCLocationTableNumber>17</alertCLocationTableNumber>\n" +
            "              <alertCLocationTableVersion>1.11.37</alertCLocationTableVersion>\n" +
            "              <alertCDirection>\n" +
            "                <alertCDirectionCoded>unknown</alertCDirectionCoded>\n" +
            "              </alertCDirection>\n" +
            "              <alertCMethod4PrimaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>2686</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "                <offsetDistance>\n" +
            "                  <offsetDistance>828</offsetDistance>\n" +
            "                </offsetDistance>\n" +
            "              </alertCMethod4PrimaryPointLocation>\n" +
            "              <alertCMethod4SecondaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>2687</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "                <offsetDistance>\n" +
            "                  <offsetDistance>1</offsetDistance>\n" +
            "                </offsetDistance>\n" +
            "              </alertCMethod4SecondaryPointLocation>\n" +
            "            </alertCLinear>\n" +
            "          </groupOfLocations>\n" +
            "          <complianceOption>mandatory</complianceOption>\n" +
            "          <roadOrCarriagewayOrLaneManagementType>roadClosed</roadOrCarriagewayOrLaneManagementType>\n" +
            "        </situationRecord>\n" +
            "        <situationRecord xsi:type=\"ReroutingManagement\" id=\"GUID5036050602\" version=\"24\">\n" +
            "          <situationRecordCreationTime>2020-05-07T12:27:30.140Z</situationRecordCreationTime>\n" +
            "          <situationRecordVersionTime>2020-05-07T12:29:09.295Z</situationRecordVersionTime>\n" +
            "          <situationRecordFirstSupplierVersionTime>2020-05-07T12:27:30.140Z</situationRecordFirstSupplierVersionTime>\n" +
            "          <probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
            "          <severity>low</severity>\n" +
            "          <validity>\n" +
            "            <validityStatus>definedByValidityTimeSpec</validityStatus>\n" +
            "            <validityTimeSpecification>\n" +
            "              <overallStartTime>2019-06-23T21:00:00.000Z</overallStartTime>\n" +
            "              <overallEndTime>2020-09-29T21:00:00.000Z</overallEndTime>\n" +
            "            </validityTimeSpecification>\n" +
            "          </validity>\n" +
            "          <groupOfLocations xsi:type=\"Point\"/>\n" +
            "          <complianceOption>mandatory</complianceOption>\n" +
            "          <reroutingManagementType>followDiversionSigns</reroutingManagementType>\n" +
            "        </situationRecord>\n" +
            "      </situation>\n" +
            "      <situation id=\"GUID50369344\" version=\"8\">\n" +
            "        <headerInformation>\n" +
            "          <confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality>\n" +
            "          <informationStatus>real</informationStatus>\n" +
            "        </headerInformation>\n" +
            "        <situationRecord xsi:type=\"RoadOrCarriagewayOrLaneManagement\" id=\"GUID5037003501\" version=\"8\">\n" +
            "          <situationRecordCreationTime>2020-05-03T09:20:24.334Z</situationRecordCreationTime>\n" +
            "          <situationRecordVersionTime>2020-05-03T09:28:43.068Z</situationRecordVersionTime>\n" +
            "          <situationRecordFirstSupplierVersionTime>2020-05-03T09:20:24.334Z</situationRecordFirstSupplierVersionTime>\n" +
            "          <probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
            "          <severity>low</severity>\n" +
            "          <validity>\n" +
            "            <validityStatus>definedByValidityTimeSpec</validityStatus>\n" +
            "            <validityTimeSpecification>\n" +
            "              <overallStartTime>2020-05-04T04:00:00.000Z</overallStartTime>\n" +
            "              <overallEndTime>2020-08-31T12:00:00.000Z</overallEndTime>\n" +
            "            </validityTimeSpecification>\n" +
            "          </validity>\n" +
            "          <generalPublicComment>\n" +
            "            <comment>\n" +
            "              <values>\n" +
            "                <value lang=\"fi\">Tie 12684, eli Pysäkintie, Eura. Liikennetiedote. \n" +
            "\n" +
            "Tie 12684, eli Pysäkintie, Eura.\n" +
            "Tarkempi paikka: Paikasta Euran kirkko 100 m, suuntaan Sorkkisten tienhaara. Pappilan silta. \n" +
            "\n" +
            "Tie on suljettu liikenteeltä. \n" +
            "Paikalla on kiertotieopastus. \n" +
            "\n" +
            "Lisätieto: Siltatyö\n" +
            "\n" +
            "\n" +
            "Ajankohta: 04.05. klo 07:00 - 31.08.2020 klo 15:00.\n" +
            "\n" +
            "\uFEFFLiikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/ \n" +
            "\n" +
            "Tieliikennekeskus Turku - ITM Finland\n" +
            "Puh: 0206373329\n" +
            "Sähköposti: turku.liikennekeskus@tmfg.fi\n" +
            "http://extranet.liikennevirasto.fi/webloik/kartta/liikennetilanne/50369344</value>\n" +
            "              </values>\n" +
            "            </comment>\n" +
            "          </generalPublicComment>\n" +
            "          <groupOfLocations xsi:type=\"Linear\">\n" +
            "            <alertCLinear xsi:type=\"AlertCMethod4Linear\">\n" +
            "              <alertCLocationCountryCode>6</alertCLocationCountryCode>\n" +
            "              <alertCLocationTableNumber>17</alertCLocationTableNumber>\n" +
            "              <alertCLocationTableVersion>1.11.37</alertCLocationTableVersion>\n" +
            "              <alertCDirection>\n" +
            "                <alertCDirectionCoded>unknown</alertCDirectionCoded>\n" +
            "              </alertCDirection>\n" +
            "              <alertCMethod4PrimaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>24860</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "                <offsetDistance>\n" +
            "                  <offsetDistance>133</offsetDistance>\n" +
            "                </offsetDistance>\n" +
            "              </alertCMethod4PrimaryPointLocation>\n" +
            "              <alertCMethod4SecondaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>24861</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "                <offsetDistance>\n" +
            "                  <offsetDistance>2508</offsetDistance>\n" +
            "                </offsetDistance>\n" +
            "              </alertCMethod4SecondaryPointLocation>\n" +
            "            </alertCLinear>\n" +
            "          </groupOfLocations>\n" +
            "          <complianceOption>mandatory</complianceOption>\n" +
            "          <roadOrCarriagewayOrLaneManagementType>roadClosed</roadOrCarriagewayOrLaneManagementType>\n" +
            "        </situationRecord>\n" +
            "        <situationRecord xsi:type=\"ReroutingManagement\" id=\"GUID5037003502\" version=\"8\">\n" +
            "          <situationRecordCreationTime>2020-05-03T09:20:24.334Z</situationRecordCreationTime>\n" +
            "          <situationRecordVersionTime>2020-05-03T09:28:43.068Z</situationRecordVersionTime>\n" +
            "          <situationRecordFirstSupplierVersionTime>2020-05-03T09:20:24.334Z</situationRecordFirstSupplierVersionTime>\n" +
            "          <probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
            "          <severity>low</severity>\n" +
            "          <validity>\n" +
            "            <validityStatus>definedByValidityTimeSpec</validityStatus>\n" +
            "            <validityTimeSpecification>\n" +
            "              <overallStartTime>2020-05-04T04:00:00.000Z</overallStartTime>\n" +
            "              <overallEndTime>2020-08-31T12:00:00.000Z</overallEndTime>\n" +
            "            </validityTimeSpecification>\n" +
            "          </validity>\n" +
            "          <groupOfLocations xsi:type=\"Point\"/>\n" +
            "          <complianceOption>mandatory</complianceOption>\n" +
            "          <reroutingManagementType>followDiversionSigns</reroutingManagementType>\n" +
            "        </situationRecord>\n" +
            "      </situation>\n" +
            "      <situation id=\"GUID50369133\" version=\"1\">\n" +
            "        <headerInformation>\n" +
            "          <confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality>\n" +
            "          <informationStatus>real</informationStatus>\n" +
            "        </headerInformation>\n" +
            "        <situationRecord xsi:type=\"TransitInformation\" id=\"GUID5036981901\" version=\"1\">\n" +
            "          <situationRecordCreationTime>2020-04-26T05:19:26.351Z</situationRecordCreationTime>\n" +
            "          <situationRecordVersionTime>2020-04-26T05:21:15.755Z</situationRecordVersionTime>\n" +
            "          <situationRecordFirstSupplierVersionTime>2020-04-26T05:19:26.351Z</situationRecordFirstSupplierVersionTime>\n" +
            "          <probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
            "          <severity>low</severity>\n" +
            "          <validity>\n" +
            "            <validityStatus>active</validityStatus>\n" +
            "            <validityTimeSpecification>\n" +
            "              <overallStartTime>2020-04-26T05:19:26.351Z</overallStartTime>\n" +
            "            </validityTimeSpecification>\n" +
            "          </validity>\n" +
            "          <generalPublicComment>\n" +
            "            <comment>\n" +
            "              <values>\n" +
            "                <value lang=\"fi\">Tie 15147, eli Hirvensalon tie, Mikkeli. Liikennetiedote. \n" +
            "\n" +
            "Tie 15147, eli Hirvensalon tie, Mikkeli.\n" +
            "Tarkempi paikka: Kuparovirran lossi. \n" +
            "\n" +
            "Lautan kantavuus on muuttunut. \n" +
            "\n" +
            "Lisätieto: Käytössä kantavuudeltaan 44 tonnin lossi.\n" +
            "\n" +
            "Ajankohta: 26.04.2020 klo 08:19 alkaen toistaiseksi.\n" +
            "\n" +
            "\uFEFFLiikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/ \n" +
            "\n" +
            "Tieliikennekeskus Tampere - ITM Finland\n" +
            "Puh: 0206373330\n" +
            "Sähköposti: tampere.liikennekeskus@tmfg.fi\n" +
            "http://extranet.liikennevirasto.fi/webloik/kartta/liikennetilanne/50369133</value>\n" +
            "              </values>\n" +
            "            </comment>\n" +
            "          </generalPublicComment>\n" +
            "          <groupOfLocations xsi:type=\"Point\">\n" +
            "            <alertCPoint xsi:type=\"AlertCMethod2Point\">\n" +
            "              <alertCLocationCountryCode>6</alertCLocationCountryCode>\n" +
            "              <alertCLocationTableNumber>17</alertCLocationTableNumber>\n" +
            "              <alertCLocationTableVersion>1.11.37</alertCLocationTableVersion>\n" +
            "              <alertCDirection>\n" +
            "                <alertCDirectionCoded>unknown</alertCDirectionCoded>\n" +
            "              </alertCDirection>\n" +
            "              <alertCMethod2PrimaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>28355</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "              </alertCMethod2PrimaryPointLocation>\n" +
            "            </alertCPoint>\n" +
            "          </groupOfLocations>\n" +
            "          <transitServiceInformation>loadCapacityChanged</transitServiceInformation>\n" +
            "          <transitServiceType>ferry</transitServiceType>\n" +
            "        </situationRecord>\n" +
            "      </situation>\n" +
            "      <situation id=\"GUID50368928\" version=\"1\">\n" +
            "        <headerInformation>\n" +
            "          <confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality>\n" +
            "          <informationStatus>real</informationStatus>\n" +
            "        </headerInformation>\n" +
            "        <situationRecord xsi:type=\"RoadOrCarriagewayOrLaneManagement\" id=\"GUID5036960801\" version=\"1\">\n" +
            "          <situationRecordCreationTime>2020-04-20T02:58:05.766Z</situationRecordCreationTime>\n" +
            "          <situationRecordVersionTime>2020-04-20T03:04:35.147Z</situationRecordVersionTime>\n" +
            "          <situationRecordFirstSupplierVersionTime>2020-04-20T02:58:05.766Z</situationRecordFirstSupplierVersionTime>\n" +
            "          <probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
            "          <severity>low</severity>\n" +
            "          <validity>\n" +
            "            <validityStatus>definedByValidityTimeSpec</validityStatus>\n" +
            "            <validityTimeSpecification>\n" +
            "              <overallStartTime>2020-04-20T04:00:00.000Z</overallStartTime>\n" +
            "              <overallEndTime>2020-06-07T16:00:00.000Z</overallEndTime>\n" +
            "            </validityTimeSpecification>\n" +
            "          </validity>\n" +
            "          <generalPublicComment>\n" +
            "            <comment>\n" +
            "              <values>\n" +
            "                <value lang=\"fi\">Tie 18409, eli Joutenniva, Haapavesi. Liikennetiedote. \n" +
            "\n" +
            "Tie 18409, eli Joutenniva, Haapavesi.\n" +
            "Tarkempi paikka: Paikasta Joutenniva 1,4 km, vaikutusalue 200 m, suuntaan Ylijoki. Joutennivan silta. \n" +
            "\n" +
            "Tie on suljettu liikenteeltä. \n" +
            "\n" +
            "Lisätieto: Joutennivantie on suljettu liikenteeltä Joutennivan sillan kohdalta korjaustyön vuoksi\n" +
            "\n" +
            "\n" +
            "Ajankohta: 20.04. klo 07:00 - 07.06.2020 klo 19:00.\n" +
            "\n" +
            "\uFEFFLiikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/ \n" +
            "\n" +
            "Tieliikennekeskus Oulu - ITM Finland\n" +
            "Puh: 0206373331\n" +
            "Sähköposti: oulu.liikennekeskus@tmfg.fi\n" +
            "http://extranet.liikennevirasto.fi/webloik/kartta/liikennetilanne/50368928</value>\n" +
            "              </values>\n" +
            "            </comment>\n" +
            "          </generalPublicComment>\n" +
            "          <groupOfLocations xsi:type=\"Linear\">\n" +
            "            <alertCLinear xsi:type=\"AlertCMethod4Linear\">\n" +
            "              <alertCLocationCountryCode>6</alertCLocationCountryCode>\n" +
            "              <alertCLocationTableNumber>17</alertCLocationTableNumber>\n" +
            "              <alertCLocationTableVersion>1.11.37</alertCLocationTableVersion>\n" +
            "              <alertCDirection>\n" +
            "                <alertCDirectionCoded>unknown</alertCDirectionCoded>\n" +
            "              </alertCDirection>\n" +
            "              <alertCMethod4PrimaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>34297</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "                <offsetDistance>\n" +
            "                  <offsetDistance>1430</offsetDistance>\n" +
            "                </offsetDistance>\n" +
            "              </alertCMethod4PrimaryPointLocation>\n" +
            "              <alertCMethod4SecondaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>34298</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "                <offsetDistance>\n" +
            "                  <offsetDistance>442</offsetDistance>\n" +
            "                </offsetDistance>\n" +
            "              </alertCMethod4SecondaryPointLocation>\n" +
            "            </alertCLinear>\n" +
            "          </groupOfLocations>\n" +
            "          <complianceOption>mandatory</complianceOption>\n" +
            "          <roadOrCarriagewayOrLaneManagementType>roadClosed</roadOrCarriagewayOrLaneManagementType>\n" +
            "        </situationRecord>\n" +
            "      </situation>\n" +
            "      <situation id=\"GUID50368927\" version=\"1\">\n" +
            "        <headerInformation>\n" +
            "          <confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality>\n" +
            "          <informationStatus>real</informationStatus>\n" +
            "        </headerInformation>\n" +
            "        <situationRecord xsi:type=\"RoadOrCarriagewayOrLaneManagement\" id=\"GUID5036960701\" version=\"1\">\n" +
            "          <situationRecordCreationTime>2020-04-20T02:45:47.469Z</situationRecordCreationTime>\n" +
            "          <situationRecordVersionTime>2020-04-20T02:55:48.553Z</situationRecordVersionTime>\n" +
            "          <situationRecordFirstSupplierVersionTime>2020-04-20T02:45:47.469Z</situationRecordFirstSupplierVersionTime>\n" +
            "          <probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
            "          <severity>low</severity>\n" +
            "          <validity>\n" +
            "            <validityStatus>definedByValidityTimeSpec</validityStatus>\n" +
            "            <validityTimeSpecification>\n" +
            "              <overallStartTime>2020-04-20T04:00:00.000Z</overallStartTime>\n" +
            "              <overallEndTime>2020-05-29T16:00:00.000Z</overallEndTime>\n" +
            "            </validityTimeSpecification>\n" +
            "          </validity>\n" +
            "          <generalPublicComment>\n" +
            "            <comment>\n" +
            "              <values>\n" +
            "                <value lang=\"fi\">Tie 18888, eli Särkiluoma, Kuusamo. Liikennetiedote. \n" +
            "\n" +
            "Tie 18888, eli Särkiluoma, Kuusamo.\n" +
            "Tarkempi paikka: Paikasta Määttälänvaara 5,0 km, suuntaan Lehto. Varisjoen silta. \n" +
            "\n" +
            "Tie on suljettu liikenteeltä. \n" +
            "\n" +
            "Lisätieto: Kiviperäntie on suljettu liikenteeltä Varisjoen sillan kohdalta sillan korjaustyön vuoksi.\n" +
            "\n" +
            "\n" +
            "Ajankohta: 20.04. klo 07:00 - 29.05.2020 klo 19:00.\n" +
            "\n" +
            "\uFEFFLiikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/ \n" +
            "\n" +
            "Tieliikennekeskus Oulu - ITM Finland\n" +
            "Puh: 0206373331\n" +
            "Sähköposti: oulu.liikennekeskus@tmfg.fi\n" +
            "http://extranet.liikennevirasto.fi/webloik/kartta/liikennetilanne/50368927</value>\n" +
            "              </values>\n" +
            "            </comment>\n" +
            "          </generalPublicComment>\n" +
            "          <groupOfLocations xsi:type=\"Linear\">\n" +
            "            <alertCLinear xsi:type=\"AlertCMethod4Linear\">\n" +
            "              <alertCLocationCountryCode>6</alertCLocationCountryCode>\n" +
            "              <alertCLocationTableNumber>17</alertCLocationTableNumber>\n" +
            "              <alertCLocationTableVersion>1.11.37</alertCLocationTableVersion>\n" +
            "              <alertCDirection>\n" +
            "                <alertCDirectionCoded>unknown</alertCDirectionCoded>\n" +
            "              </alertCDirection>\n" +
            "              <alertCMethod4PrimaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>35257</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "                <offsetDistance>\n" +
            "                  <offsetDistance>5011</offsetDistance>\n" +
            "                </offsetDistance>\n" +
            "              </alertCMethod4PrimaryPointLocation>\n" +
            "              <alertCMethod4SecondaryPointLocation>\n" +
            "                <alertCLocation>\n" +
            "                  <specificLocation>38090</specificLocation>\n" +
            "                </alertCLocation>\n" +
            "                <offsetDistance>\n" +
            "                  <offsetDistance>2611</offsetDistance>\n" +
            "                </offsetDistance>\n" +
            "              </alertCMethod4SecondaryPointLocation>\n" +
            "            </alertCLinear>\n" +
            "          </groupOfLocations>\n" +
            "          <complianceOption>mandatory</complianceOption>\n" +
            "          <roadOrCarriagewayOrLaneManagementType>roadClosed</roadOrCarriagewayOrLaneManagementType>\n" +
            "        </situationRecord>\n" +
            "      </situation>\n" +
            "    </payloadPublication>\n" +
            "  </d2LogicalModel>";

    private final static String LONG_XML2 =
            "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header/><soapenv:Body><ns4:PortsResponse xmlns:ns4=\"IBNet_Baltice_Schema\"><ports><ns1:dataValidTime xmlns:ns1=\"IBNet_Baltice_Ports\">2020-05-15T12:21:59.977+00:00</ns1:dataValidTime><ns1:dataQueryTime xmlns:ns1=\"IBNet_Baltice_Ports\">2020-05-15T12:21:59.977+00:00</ns1:dataQueryTime><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R971</portId><portSource>IBNET</portSource><name>KIEL</name><locode>DEKIE</locode><nationality>DE</nationality><lat>54.3167</lat><lon>10.0833</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>160</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R913</portId><portSource>IBNET</portSource><name>FREDRIKSHAMN</name><locode>DKFRE</locode><nationality>DK</nationality><lat>57.4333</lat><lon>10.5333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>151</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R909</portId><portSource>IBNET</portSource><name>AALBORG</name><locode>DKAAL</locode><nationality>DK</nationality><lat>57.0667</lat><lon>9.93333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>152</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R919</portId><portSource>IBNET</portSource><name>GREN��</name><locode>DKGRE</locode><nationality>DK</nationality><lat>56.4167</lat><lon>10.9333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>153</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R939</portId><portSource>IBNET</portSource><name>HELSING��R</name><locode>DKHSR</locode><nationality>DK</nationality><lat>56.0333</lat><lon>12.6</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>154</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R987</portId><portSource>IBNET</portSource><name>K��PENHAMN</name><locode>DKKPH</locode><nationality>DK</nationality><lat>55.6833</lat><lon>12.6</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>155</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R985</portId><portSource>IBNET</portSource><name>K��GE</name><locode>DKK��G</locode><nationality>DK</nationality><lat>55.45</lat><lon>12.1833</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>156</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R4086319</portId><portSource>IBNET</portSource><name>P��RNU</name><locode>EEPNU</locode><nationality>EE</nationality><lat>58.3833</lat><lon>24.55</lon><seaArea>Baltic Sea</seaArea><displayOrder>111</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R4086322</portId><portSource>IBNET</portSource><name>HAAPSALU</name><locode>EEHAA</locode><nationality>EE</nationality><lat>58.9167</lat><lon>23.5</lon><seaArea>Baltic Sea</seaArea><displayOrder>112</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R3108577</portId><portSource>IBNET</portSource><name>PALDISKI</name><locode>EEPAL</locode><nationality>EE</nationality><lat>59.3333</lat><lon>24.0833</lon><seaArea>Baltic Sea</seaArea><displayOrder>113</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1071</portId><portSource>IBNET</portSource><name>TALLINN</name><locode>EETAL</locode><nationality>EE</nationality><lat>59.4</lat><lon>24.75</lon><seaArea>Baltic Sea</seaArea><displayOrder>114</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R3108068</portId><portSource>IBNET</portSource><name>MUUGA</name><locode>EEMUU</locode><nationality>EE</nationality><lat>59.4833</lat><lon>24.95</lon><seaArea>Baltic Sea</seaArea><displayOrder>115</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R3108065</portId><portSource>IBNET</portSource><name>LOKSA</name><locode>EELOK</locode><nationality>EE</nationality><lat>59.5833</lat><lon>25.7</lon><seaArea>Baltic Sea</seaArea><displayOrder>116</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R3108071</portId><portSource>IBNET</portSource><name>KUNDA</name><locode>EEKND</locode><nationality>EE</nationality><lat>59.5167</lat><lon>26.5333</lon><seaArea>Baltic Sea</seaArea><displayOrder>117</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R4086325</portId><portSource>IBNET</portSource><name>SILLAM��E</name><locode>EESIL</locode><nationality>EE</nationality><lat>59.4</lat><lon>27.7333</lon><seaArea>Baltic Sea</seaArea><displayOrder>118</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1003</portId><portSource>IBNET</portSource><name lang=\"fi\">Suomi (tuntematon satama)</name><locode>FI888</locode><nationality>FI</nationality><lat>59.5</lat><lon>10.0</lon><seaArea>Outside Baltic Sea</seaArea><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1073</portId><portSource>IBNET</portSource><name lang=\"fi\">TORNIO</name><locode>FITOR</locode><nationality>FI</nationality><lat>65.7667</lat><lon>24.15</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>1</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo><restrictions><restriction><isCurrent>true</isCurrent><portRestricted>true</portRestricted><portClosed>false</portClosed><issueTime>2020-05-15T05:33:20.350+00:00</issueTime><timeStamp>2020-05-15T05:33:20.350+00:00</timeStamp><validFrom>2020-05-15</validFrom><rawText>II 2000</rawText><formattedText>II 2000</formattedText></restriction></restrictions></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R969</portId><portSource>IBNET</portSource><name lang=\"fi\">KEMI</name><locode>FIKEM</locode><nationality>FI</nationality><lat>65.7333</lat><lon>24.5667</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>2</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo><restrictions><restriction><isCurrent>true</isCurrent><portRestricted>true</portRestricted><portClosed>false</portClosed><issueTime>2020-05-15T05:33:20.367+00:00</issueTime><timeStamp>2020-05-15T05:33:20.367+00:00</timeStamp><validFrom>2020-05-15</validFrom><rawText>II 2000</rawText><formattedText>II 2000</formattedText></restriction></restrictions></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1027</portId><portSource>IBNET</portSource><name lang=\"fi\">OULU</name><locode>FIOUL</locode><nationality>FI</nationality><lat>65.0167</lat><lon>25.4667</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>3</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo><restrictions><restriction><isCurrent>true</isCurrent><portRestricted>true</portRestricted><portClosed>false</portClosed><issueTime>2020-05-11T07:56:35.050+00:00</issueTime><timeStamp>2020-05-11T07:56:35.050+00:00</timeStamp><validFrom>2020-05-11</validFrom><rawText>II 2000</rawText><formattedText>II 2000</formattedText></restriction></restrictions></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1033</portId><portSource>IBNET</portSource><name lang=\"fi\">RAAHE</name><locode>FIRAA</locode><nationality>FI</nationality><lat>64.6833</lat><lon>24.4833</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>4</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1035</portId><portSource>IBNET</portSource><name lang=\"fi\">KALAJOKI</name><locode>FIKJI</locode><nationality>FI</nationality><lat>64.1833</lat><lon>23.8333</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>5</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R975</portId><portSource>IBNET</portSource><name lang=\"fi\">KOKKOLA</name><locode>FIKOK</locode><nationality>FI</nationality><lat>63.85</lat><lon>23.1333</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>6</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1031</portId><portSource>IBNET</portSource><name lang=\"fi\">PIETARSAARI</name><locode>FIPIE</locode><nationality>FI</nationality><lat>63.6833</lat><lon>22.7</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>7</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1085</portId><portSource>IBNET</portSource><name lang=\"fi\">VAASA</name><locode>FIVAA</locode><nationality>FI</nationality><lat>63.0833</lat><lon>21.6167</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>8</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R967</portId><portSource>IBNET</portSource><name lang=\"fi\">KASKINEN</name><locode>FIKAS</locode><nationality>FI</nationality><lat>62.3833</lat><lon>21.2167</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>9</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R981</portId><portSource>IBNET</portSource><name lang=\"fi\">KRISTIINANKAUPUNKI</name><locode>FIKRI</locode><nationality>FI</nationality><lat>62.2667</lat><lon>21.3833</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>10</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1011</portId><portSource>IBNET</portSource><name lang=\"fi\">PORI</name><locode>FIPOR</locode><nationality>FI</nationality><lat>61.6</lat><lon>21.4833</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>11</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1037</portId><portSource>IBNET</portSource><name lang=\"fi\">RAUMA</name><locode>FIRAU</locode><nationality>FI</nationality><lat>61.1333</lat><lon>21.5</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>12</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1083</portId><portSource>IBNET</portSource><name lang=\"fi\">UUSIKAUPUNKI</name><locode>FIUUS</locode><nationality>FI</nationality><lat>60.8</lat><lon>21.4</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>13</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1013</portId><portSource>IBNET</portSource><name lang=\"fi\">NAANTALI</name><locode>FINAA</locode><nationality>FI</nationality><lat>60.4667</lat><lon>22.0167</lon><seaArea>Baltic Sea</seaArea><displayOrder>14</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1079</portId><portSource>IBNET</portSource><name lang=\"fi\">TURKU</name><locode>FITUR</locode><nationality>FI</nationality><lat>60.45</lat><lon>22.25</lon><seaArea>Baltic Sea</seaArea><displayOrder>15</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R13261160</portId><portSource>IBNET</portSource><name lang=\"fi\">ECKER��</name><locode>FIEK��</locode><nationality>FI</nationality><lat>60.2283</lat><lon>19.5417</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>16</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1007</portId><portSource>IBNET</portSource><name lang=\"fi\">MAARIANHAMINA</name><locode>FIMAN</locode><nationality>FI</nationality><lat>60.1</lat><lon>19.9333</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>17</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R15833300</portId><portSource>IBNET</portSource><name lang=\"fi\">L��NGN��S</name><locode>FIL��N</locode><nationality>FI</nationality><lat>60.1129</lat><lon>20.2977</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>18</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R14230440</portId><portSource>IBNET</portSource><name lang=\"fi\">TAALINTEHDAS</name><locode>FITTS</locode><nationality>FI</nationality><lat>60.0167</lat><lon>22.5083</lon><seaArea>Baltic Sea</seaArea><displayOrder>19</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R13261194</portId><portSource>IBNET</portSource><name lang=\"fi\">F��RBY</name><locode>FIFBY</locode><nationality>FI</nationality><lat>60.1</lat><lon>22.8533</lon><seaArea>Baltic Sea</seaArea><displayOrder>20</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R933</portId><portSource>IBNET</portSource><name lang=\"fi\">HANKO</name><locode>FIHAN</locode><nationality>FI</nationality><lat>59.8167</lat><lon>22.9667</lon><seaArea>Baltic Sea</seaArea><displayOrder>21</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R979</portId><portSource>IBNET</portSource><name lang=\"fi\">KOVERHAR</name><locode>FIKOV</locode><nationality>FI</nationality><lat>59.8833</lat><lon>23.2167</lon><seaArea>Baltic Sea</seaArea><displayOrder>22</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>94R9</portId><portSource>IBNET</portSource><name lang=\"fi\">LAPPOHJA</name><locode>FILAP</locode><nationality>FI</nationality><lat>59.89</lat><lon>23.285</lon><seaArea>Baltic Sea</seaArea><displayOrder>23</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R951</portId><portSource>IBNET</portSource><name lang=\"fi\">INKOO</name><locode>FIINK</locode><nationality>FI</nationality><lat>60.0167</lat><lon>23.9167</lon><seaArea>Baltic Sea</seaArea><displayOrder>24</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R955</portId><portSource>IBNET</portSource><name lang=\"fi\">KANTVIK</name><locode>FIKAN</locode><nationality>FI</nationality><lat>60.0833</lat><lon>24.4</lon><seaArea>Baltic Sea</seaArea><displayOrder>25</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R941</portId><portSource>IBNET</portSource><name lang=\"fi\">HELSINKI</name><locode>FIHEL</locode><nationality>FI</nationality><lat>60.15</lat><lon>24.95</lon><seaArea>Baltic Sea</seaArea><displayOrder>26</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1051</portId><portSource>IBNET</portSource><name lang=\"fi\">SK��LDVIK</name><locode>FISK��</locode><nationality>FI</nationality><lat>60.3</lat><lon>25.5667</lon><seaArea>Baltic Sea</seaArea><displayOrder>27</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R997</portId><portSource>IBNET</portSource><name lang=\"fi\">LOVIISA</name><locode>FILOV</locode><nationality>FI</nationality><lat>60.45</lat><lon>26.25</lon><seaArea>Baltic Sea</seaArea><displayOrder>28</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>94R35</portId><portSource>IBNET</portSource><name lang=\"fi\">MUSSALO</name><locode>FIMUS</locode><nationality>FI</nationality><lat>60.44</lat><lon>26.75</lon><seaArea>Baltic Sea</seaArea><displayOrder>29</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R977</portId><portSource>IBNET</portSource><name lang=\"fi\">KOTKA</name><locode>FIKOT</locode><nationality>FI</nationality><lat>60.4667</lat><lon>26.95</lon><seaArea>Baltic Sea</seaArea><displayOrder>30</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R931</portId><portSource>IBNET</portSource><name lang=\"fi\">HAMINA</name><locode>FIHAM</locode><nationality>FI</nationality><lat>60.5667</lat><lon>27.1833</lon><seaArea>Baltic Sea</seaArea><displayOrder>31</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1043</portId><portSource>IBNET</portSource><name lang=\"fi\">SAIMAAN KANAVA</name><locode>FISAI</locode><nationality>FI</nationality><lat>60.9667</lat><lon>28.5333</lon><seaArea>Baltic Sea</seaArea><displayOrder>32</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546455</portId><portSource>IBNET</portSource><name lang=\"fi\">LAPPEENRANTA</name><locode>FILPR</locode><nationality>FI</nationality><lat>61.05</lat><lon>28.25</lon><seaArea>Baltic Sea</seaArea><displayOrder>33</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546458</portId><portSource>IBNET</portSource><name lang=\"fi\">JOUTSENO</name><locode>FIJTS</locode><nationality>FI</nationality><lat>61.1167</lat><lon>28.4667</lon><seaArea>Baltic Sea</seaArea><displayOrder>34</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546461</portId><portSource>IBNET</portSource><name lang=\"fi\">IMATRA</name><locode>FIIMT</locode><nationality>FI</nationality><lat>61.2167</lat><lon>28.8333</lon><seaArea>Baltic Sea</seaArea><displayOrder>35</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546464</portId><portSource>IBNET</portSource><name lang=\"fi\">RISTIINA</name><locode>FIRIS</locode><nationality>FI</nationality><lat>61.4667</lat><lon>27.2833</lon><seaArea>Baltic Sea</seaArea><displayOrder>36</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546470</portId><portSource>IBNET</portSource><name lang=\"fi\">SAVONLINNA</name><locode>FISLN</locode><nationality>FI</nationality><lat>61.8667</lat><lon>28.8667</lon><seaArea>Baltic Sea</seaArea><displayOrder>37</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546473</portId><portSource>IBNET</portSource><name lang=\"fi\">VARKAUS</name><locode>FIVRK</locode><nationality>FI</nationality><lat>62.3167</lat><lon>27.9167</lon><seaArea>Baltic Sea</seaArea><displayOrder>38</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546482</portId><portSource>IBNET</portSource><name lang=\"fi\">KUOPIO</name><locode>FIKUO</locode><nationality>FI</nationality><lat>62.8833</lat><lon>27.6833</lon><seaArea>Baltic Sea</seaArea><displayOrder>39</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546485</portId><portSource>IBNET</portSource><name lang=\"fi\">SIILINJ��RVI</name><locode>FISII</locode><nationality>FI</nationality><lat>63.1</lat><lon>27.75</lon><seaArea>Baltic Sea</seaArea><displayOrder>40</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546476</portId><portSource>IBNET</portSource><name lang=\"fi\">PUHOS</name><locode>FIPUH</locode><nationality>FI</nationality><lat>62.1</lat><lon>29.9167</lon><seaArea>Baltic Sea</seaArea><displayOrder>41</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546479</portId><portSource>IBNET</portSource><name lang=\"fi\">JOENSUU</name><locode>FIJNS</locode><nationality>FI</nationality><lat>62.5833</lat><lon>29.75</lon><seaArea>Baltic Sea</seaArea><displayOrder>42</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>94R22</portId><portSource>IBNET</portSource><name lang=\"fi\">Olkiluoto</name><locode>FIOLK</locode><nationality>FI</nationality><lat>61.233333</lat><lon>21.483333</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>501</displayOrder></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>47R7546452</portId><portSource>IBNET</portSource><name lang=\"fi\">PARAINEN</name><locode>FIPAR</locode><nationality>FI</nationality><lat>60.3</lat><lon>22.3</lon><seaArea>Baltic Sea</seaArea><displayOrder>502</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>94R36</portId><portSource>IBNET</portSource><name lang=\"fi\">Merikarvia</name><locode>FIMER2</locode><nationality>FI</nationality><lat>61.85</lat><lon>21.5</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>532</displayOrder></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>94R37</portId><portSource>IBNET</portSource><name lang=\"fi\">Kalkkiranta</name><locode>FIKAL2</locode><nationality>FI</nationality><lat>60.25</lat><lon>25.383333</lon><seaArea>Baltic Sea</seaArea><displayOrder>533</displayOrder></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R973</portId><portSource>IBNET</portSource><name>KLAIPEDA</name><locode>LTKDA</locode><nationality>LT</nationality><lat>55.7167</lat><lon>21.1333</lon><seaArea>Baltic Sea</seaArea><displayOrder>131</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1039</portId><portSource>IBNET</portSource><name>RIGA</name><locode>LVRIG</locode><nationality>LV</nationality><lat>56.95</lat><lon>24.0833</lon><seaArea>Baltic Sea</seaArea><displayOrder>121</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1089</portId><portSource>IBNET</portSource><name>VENTSPILS</name><locode>LVVEN</locode><nationality>LV</nationality><lat>57.4</lat><lon>21.5167</lon><seaArea>Baltic Sea</seaArea><displayOrder>122</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R4086328</portId><portSource>IBNET</portSource><name>LIEBAJA</name><locode>LVLJA</locode><nationality>LV</nationality><lat>56.5</lat><lon>20.9833</lon><seaArea>Baltic Sea</seaArea><displayOrder>123</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1023</portId><portSource>IBNET</portSource><name>OSLO</name><locode>NOOSL</locode><nationality>NO</nationality><lat>59.9</lat><lon>10.7833</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>141</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R915</portId><portSource>IBNET</portSource><name>GDANSK</name><locode>PLGDA</locode><nationality>PL</nationality><lat>54.35</lat><lon>18.65</lon><seaArea>Baltic Sea</seaArea><displayOrder>132</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R917</portId><portSource>IBNET</portSource><name>GDYNIA</name><locode>PLGDY</locode><nationality>PL</nationality><lat>54.5333</lat><lon>18.55</lon><seaArea>Baltic Sea</seaArea><displayOrder>133</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1093</portId><portSource>IBNET</portSource><name>VYBORG</name><locode>RUVYB</locode><nationality>RU</nationality><lat>60.7167</lat><lon>28.7833</lon><seaArea>Baltic Sea</seaArea><displayOrder>101</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R3108059</portId><portSource>IBNET</portSource><name>VYSOTSKI</name><locode>RUVYS</locode><nationality>RU</nationality><lat>60.6167</lat><lon>28.5667</lon><seaArea>Baltic Sea</seaArea><displayOrder>102</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R2766007</portId><portSource>IBNET</portSource><name>PRIMORSK</name><locode>RUPRI</locode><nationality>RU</nationality><lat>60.3333</lat><lon>28.6667</lon><seaArea>Baltic Sea</seaArea><displayOrder>103</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R3108062</portId><portSource>IBNET</portSource><name>UST-LUGA</name><locode>RUULU</locode><nationality>RU</nationality><lat>59.6833</lat><lon>28.4</lon><seaArea>Baltic Sea</seaArea><displayOrder>104</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1055</portId><portSource>IBNET</portSource><name>ST PETERSBURG</name><locode>RUSTP</locode><nationality>RU</nationality><lat>59.9333</lat><lon>30.3</lon><seaArea>Baltic Sea</seaArea><displayOrder>105</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>1R938048</portId><portSource>IBNET</portSource><name lang=\"se\">Gotskatest</name><locode>SEGOX</locode><nationality>SE</nationality><lat>58.0</lat><lon>19.0</lon><seaArea>Baltic Sea</seaArea></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R959</portId><portSource>IBNET</portSource><name lang=\"se\">KARLSBORG</name><locode>SEKAR</locode><nationality>SE</nationality><lat>65.8</lat><lon>23.2833</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>1</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R999</portId><portSource>IBNET</portSource><name lang=\"se\">LULE��</name><locode>SELUL</locode><nationality>SE</nationality><lat>65.5833</lat><lon>22.1667</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>2</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R935</portId><portSource>IBNET</portSource><name lang=\"se\">HARAHOLMEN</name><locode>SEHAR</locode><nationality>SE</nationality><lat>65.2333</lat><lon>21.6333</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>3</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1047</portId><portSource>IBNET</portSource><name lang=\"se\">SKELLEFTEHAMN</name><locode>SESKE</locode><nationality>SE</nationality><lat>64.6833</lat><lon>21.25</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>4</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R943</portId><portSource>IBNET</portSource><name lang=\"se\">HOLMSUND</name><locode>SEHOL</locode><nationality>SE</nationality><lat>63.6667</lat><lon>20.3333</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>5</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1041</portId><portSource>IBNET</portSource><name lang=\"se\">RUNDVIK</name><locode>SERUN</locode><nationality>SE</nationality><lat>63.5333</lat><lon>19.45</lon><seaArea>Bay of Bothnia</seaArea><displayOrder>6</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R947</portId><portSource>IBNET</portSource><name lang=\"se\">HUSUM</name><locode>SEHUS</locode><nationality>SE</nationality><lat>63.3333</lat><lon>19.15</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>7</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1109</portId><portSource>IBNET</portSource><name lang=\"se\">��RNSK��LDSVIK</name><locode>SE��RN</locode><nationality>SE</nationality><lat>63.2833</lat><lon>18.7167</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>8</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R2716906</portId><portSource>IBNET</portSource><name lang=\"se\">��NGERMAN��LVEN</name><locode>SE��NG</locode><nationality>SE</nationality><lat>62.9333</lat><lon>17.7833</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>9</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R949</portId><portSource>IBNET</portSource><name lang=\"se\">H��RN��SAND</name><locode>SEH��R</locode><nationality>SE</nationality><lat>62.6333</lat><lon>17.9333</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>10</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R4081188</portId><portSource>IBNET</portSource><name lang=\"se\">S��R��KER</name><locode>SES��R</locode><nationality>SE</nationality><lat>62.5</lat><lon>17.5</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>11</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1061</portId><portSource>IBNET</portSource><name lang=\"se\">SUNDSVALL</name><locode>SESUN</locode><nationality>SE</nationality><lat>62.4167</lat><lon>17.3333</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>12</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R4081186</portId><portSource>IBNET</portSource><name lang=\"se\">STOCKA</name><locode>SESTC</locode><nationality>SE</nationality><lat>61.9</lat><lon>17.35</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>13</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R945</portId><portSource>IBNET</portSource><name lang=\"se\">HUDIKSVALL</name><locode>SEHUD</locode><nationality>SE</nationality><lat>61.7333</lat><lon>17.1167</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>14</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R3012222</portId><portSource>IBNET</portSource><name lang=\"se\">IGGESUND</name><locode>SEIGG</locode><nationality>SE</nationality><lat>61.6333</lat><lon>17.1</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>15</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1065</portId><portSource>IBNET</portSource><name lang=\"se\">S��DERHAMN</name><locode>SES��D</locode><nationality>SE</nationality><lat>61.3</lat><lon>17.0833</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>16</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R4081177</portId><portSource>IBNET</portSource><name lang=\"se\">ORRSK��R</name><locode>SEORR</locode><nationality>SE</nationality><lat>61.22</lat><lon>17.1717</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>17</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1017</portId><portSource>IBNET</portSource><name lang=\"se\">NORRSUNDET</name><locode>SENOR</locode><nationality>SE</nationality><lat>60.9333</lat><lon>17.1667</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>18</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R923</portId><portSource>IBNET</portSource><name lang=\"se\">G��VLE</name><locode>SEG��V</locode><nationality>SE</nationality><lat>60.6833</lat><lon>17.1833</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>19</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R4081153</portId><portSource>IBNET</portSource><name lang=\"se\">SKUTSK��R</name><locode>SESKU</locode><nationality>SE</nationality><lat>60.65</lat><lon>17.4167</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>20</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R2808940</portId><portSource>IBNET</portSource><name lang=\"se\">V��NERN</name><locode>SEV��N</locode><nationality>SE</nationality><lat>58.7667</lat><lon>13.2167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>20</displayOrder></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R10559823</portId><portSource>IBNET</portSource><name lang=\"se\">��REGRUND</name><locode>SE��RG</locode><nationality>SE</nationality><lat>60.3333</lat><lon>18.45</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>21</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R10560147</portId><portSource>IBNET</portSource><name lang=\"se\">HARGSHAMN</name><locode>SEHHA</locode><nationality>SE</nationality><lat>60.1667</lat><lon>18.4833</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>22</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R927</portId><portSource>IBNET</portSource><name lang=\"se\">HALLSTAVIK</name><locode>SEHAL</locode><nationality>SE</nationality><lat>60.0667</lat><lon>18.6</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>23</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R10559054</portId><portSource>IBNET</portSource><name lang=\"se\">GRISSLEHAMN</name><locode>SEGRN</locode><nationality>SE</nationality><lat>60.1</lat><lon>18.8167</lon><seaArea>Sea of Bothnia</seaArea><displayOrder>24</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R957</portId><portSource>IBNET</portSource><name lang=\"se\">KAPPELSK��R</name><locode>SEKAP</locode><nationality>SE</nationality><lat>59.7167</lat><lon>19.0667</lon><seaArea>Baltic Sea</seaArea><displayOrder>25</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1057</portId><portSource>IBNET</portSource><name lang=\"se\">STOCKHOLM</name><locode>SESTO</locode><nationality>SE</nationality><lat>59.3167</lat><lon>18.05</lon><seaArea>Baltic Sea</seaArea><displayOrder>26</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1019</portId><portSource>IBNET</portSource><name lang=\"se\">NYN��SHAMN</name><locode>SENYN</locode><nationality>SE</nationality><lat>58.9</lat><lon>17.95</lon><seaArea>Baltic Sea</seaArea><displayOrder>27</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1067</portId><portSource>IBNET</portSource><name lang=\"se\">S��DERT��LJE</name><locode>SES��T</locode><nationality>SE</nationality><lat>59.2</lat><lon>17.6333</lon><seaArea>Baltic Sea</seaArea><displayOrder>28</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R989</portId><portSource>IBNET</portSource><name lang=\"se\">K��PING</name><locode>SEK��I</locode><nationality>SE</nationality><lat>59.5</lat><lon>16.0167</lon><seaArea>Baltic Sea</seaArea><displayOrder>29</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1099</portId><portSource>IBNET</portSource><name lang=\"se\">V��STER��S</name><locode>SEV��S</locode><nationality>SE</nationality><lat>59.6</lat><lon>16.55</lon><seaArea>Baltic Sea</seaArea><displayOrder>30</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616488</portId><portSource>IBNET</portSource><name lang=\"se\">B��LSTA</name><locode>SEB��A</locode><nationality>SE</nationality><lat>59.5833</lat><lon>17.5</lon><seaArea>Baltic Sea</seaArea><displayOrder>31</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1029</portId><portSource>IBNET</portSource><name lang=\"se\">OXEL��SUND</name><locode>SEOXE</locode><nationality>SE</nationality><lat>58.6667</lat><lon>17.1</lon><seaArea>Baltic Sea</seaArea><displayOrder>32</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1015</portId><portSource>IBNET</portSource><name lang=\"se\">NORRK��PING</name><locode>SENOK</locode><nationality>SE</nationality><lat>58.6</lat><lon>16.2</lon><seaArea>Baltic Sea</seaArea><displayOrder>33</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1097</portId><portSource>IBNET</portSource><name lang=\"se\">V��STERVIK</name><locode>SEV��S</locode><nationality>SE</nationality><lat>57.75</lat><lon>16.5833</lon><seaArea>Baltic Sea</seaArea><displayOrder>34</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1091</portId><portSource>IBNET</portSource><name lang=\"se\">VISBY</name><locode>SEVIS</locode><nationality>SE</nationality><lat>57.6333</lat><lon>18.2833</lon><seaArea>Baltic Sea</seaArea><displayOrder>35</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1059</portId><portSource>IBNET</portSource><name lang=\"se\">STORUGNS</name><locode>SESUS</locode><nationality>SE</nationality><lat>57.8333</lat><lon>18.8</lon><seaArea>Baltic Sea</seaArea><displayOrder>36</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616690</portId><portSource>IBNET</portSource><name lang=\"se\">STR��</name><locode>SESR��</locode><nationality>SE</nationality><lat>57.8833</lat><lon>19.0167</lon><seaArea>Baltic Sea</seaArea><displayOrder>37</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1053</portId><portSource>IBNET</portSource><name lang=\"se\">SLITE</name><locode>SESLI</locode><nationality>SE</nationality><lat>57.7167</lat><lon>18.8167</lon><seaArea>Baltic Sea</seaArea><displayOrder>38</displayOrder><nameDisplayOffset>SW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R10560126</portId><portSource>IBNET</portSource><name lang=\"se\">RONEHAMN</name><locode>SERHM</locode><nationality>SE</nationality><lat>57.175</lat><lon>18.5</lon><seaArea>Baltic Sea</seaArea><displayOrder>39</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616642</portId><portSource>IBNET</portSource><name lang=\"se\">KLINTEHAMN</name><locode>SEKLI</locode><nationality>SE</nationality><lat>57.3833</lat><lon>18.1833</lon><seaArea>Baltic Sea</seaArea><displayOrder>40</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1021</portId><portSource>IBNET</portSource><name lang=\"se\">OSKARSHAMN</name><locode>SEOSK</locode><nationality>SE</nationality><lat>57.25</lat><lon>16.45</lon><seaArea>Baltic Sea</seaArea><displayOrder>41</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R6586930</portId><portSource>IBNET</portSource><name lang=\"se\">M��NSTER��S</name><locode>SEM��S</locode><nationality>SE</nationality><lat>57.1</lat><lon>16.5667</lon><seaArea>Baltic Sea</seaArea><displayOrder>42</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R953</portId><portSource>IBNET</portSource><name lang=\"se\">KALMAR</name><locode>SEKMR</locode><nationality>SE</nationality><lat>56.6667</lat><lon>16.3667</lon><seaArea>Baltic Sea</seaArea><displayOrder>43</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616570</portId><portSource>IBNET</portSource><name lang=\"se\">DEGERHAMN</name><locode>SEDEG</locode><nationality>SE</nationality><lat>56.35</lat><lon>16.4167</lon><seaArea>Baltic Sea</seaArea><displayOrder>44</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616576</portId><portSource>IBNET</portSource><name lang=\"se\">BERGKVARA</name><locode>SEBER</locode><nationality>SE</nationality><lat>56.3833</lat><lon>16.0833</lon><seaArea>Baltic Sea</seaArea><displayOrder>45</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R963</portId><portSource>IBNET</portSource><name lang=\"se\">KARLSKRONA</name><locode>SEKKR</locode><nationality>SE</nationality><lat>56.1667</lat><lon>15.6</lon><seaArea>Baltic Sea</seaArea><displayOrder>46</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R961</portId><portSource>IBNET</portSource><name lang=\"se\">KARLSHAMN</name><locode>SEKAH</locode><nationality>SE</nationality><lat>56.1667</lat><lon>14.8667</lon><seaArea>Baltic Sea</seaArea><displayOrder>47</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1069</portId><portSource>IBNET</portSource><name lang=\"se\">S��LVESBORG</name><locode>SES��L</locode><nationality>SE</nationality><lat>56.05</lat><lon>14.5833</lon><seaArea>Baltic Sea</seaArea><displayOrder>48</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1105</portId><portSource>IBNET</portSource><name lang=\"se\">��HUS</name><locode>SE��HS</locode><nationality>SE</nationality><lat>55.9333</lat><lon>14.3167</lon><seaArea>Baltic Sea</seaArea><displayOrder>49</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1045</portId><portSource>IBNET</portSource><name lang=\"se\">SIMRISHAMN</name><locode>SESIM</locode><nationality>SE</nationality><lat>55.5667</lat><lon>14.3667</lon><seaArea>Baltic Sea</seaArea><displayOrder>50</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1103</portId><portSource>IBNET</portSource><name lang=\"se\">YSTAD</name><locode>SEYST</locode><nationality>SE</nationality><lat>55.4333</lat><lon>13.8333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>51</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1075</portId><portSource>IBNET</portSource><name lang=\"se\">TRELLEBORG</name><locode>SETRE</locode><nationality>SE</nationality><lat>55.3667</lat><lon>13.1667</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>52</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1005</portId><portSource>IBNET</portSource><name lang=\"se\">MALM��</name><locode>SEMM��</locode><nationality>SE</nationality><lat>55.6167</lat><lon>13.0</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>53</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R991</portId><portSource>IBNET</portSource><name lang=\"se\">LANDSKRONA</name><locode>SELKR</locode><nationality>SE</nationality><lat>55.8667</lat><lon>12.8333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>54</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R937</portId><portSource>IBNET</portSource><name lang=\"se\">HELSINGBORG</name><locode>SEHBG</locode><nationality>SE</nationality><lat>56.05</lat><lon>12.7</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>55</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616614</portId><portSource>IBNET</portSource><name lang=\"se\">H��GAN��S</name><locode>SEH��A</locode><nationality>SE</nationality><lat>56.2</lat><lon>12.55</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>56</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R929</portId><portSource>IBNET</portSource><name lang=\"se\">HALMSTAD</name><locode>SEHST</locode><nationality>SE</nationality><lat>56.6667</lat><lon>12.8667</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>57</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R911</portId><portSource>IBNET</portSource><name lang=\"se\">FALKENBERG</name><locode>SEFBG</locode><nationality>SE</nationality><lat>56.9167</lat><lon>12.5</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>58</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1087</portId><portSource>IBNET</portSource><name lang=\"se\">VARBERG</name><locode>SEVAR</locode><nationality>SE</nationality><lat>57.1</lat><lon>12.25</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>59</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R925</portId><portSource>IBNET</portSource><name lang=\"se\">G��TEBORG</name><locode>SEG��T</locode><nationality>SE</nationality><lat>57.7167</lat><lon>11.8833</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>60</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616715</portId><portSource>IBNET</portSource><name lang=\"se\">WALLHAMN</name><locode>SEWAL</locode><nationality>SE</nationality><lat>58.0167</lat><lon>11.7</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>61</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616678</portId><portSource>IBNET</portSource><name lang=\"se\">STENUNGSUND</name><locode>SESTE</locode><nationality>SE</nationality><lat>58.0833</lat><lon>11.8167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>62</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1081</portId><portSource>IBNET</portSource><name lang=\"se\">UDDEVALLA</name><locode>SEUDD</locode><nationality>SE</nationality><lat>58.35</lat><lon>11.9333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>63</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1001</portId><portSource>IBNET</portSource><name lang=\"se\">LYSEKIL</name><locode>SELYS</locode><nationality>SE</nationality><lat>58.2667</lat><lon>11.4333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>64</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616525</portId><portSource>IBNET</portSource><name lang=\"se\">BROFJORDEN</name><locode>SEBRO</locode><nationality>SE</nationality><lat>58.35</lat><lon>11.4167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>65</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R8616665</portId><portSource>IBNET</portSource><name lang=\"se\">STR��MSTAD</name><locode>SESTR</locode><nationality>SE</nationality><lat>58.9333</lat><lon>11.1667</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>66</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R2994033</portId><portSource>IBNET</portSource><name lang=\"se\">AGNESBERG</name><locode>SEABG</locode><nationality>SE</nationality><lat>57.7854</lat><lon>12.0075</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>67</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R2994047</portId><portSource>IBNET</portSource><name lang=\"se\">SURTE</name><locode>SESUE</locode><nationality>SE</nationality><lat>57.8333</lat><lon>12.0167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>68</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R10560131</portId><portSource>IBNET</portSource><name lang=\"se\">BOHUS</name><locode>SEBOH</locode><nationality>SE</nationality><lat>57.85</lat><lon>12.0167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>69</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R2994035</portId><portSource>IBNET</portSource><name lang=\"se\">NOL</name><locode>SENOL</locode><nationality>SE</nationality><lat>57.9167</lat><lon>12.0667</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>70</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R2994057</portId><portSource>IBNET</portSource><name lang=\"se\">L��D��SE</name><locode>SEL��D</locode><nationality>SE</nationality><lat>58.0333</lat><lon>12.15</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>71</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R995</portId><portSource>IBNET</portSource><name lang=\"se\">LILLA EDET</name><locode>SELIE</locode><nationality>SE</nationality><lat>58.1333</lat><lon>12.1167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>72</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1077</portId><portSource>IBNET</portSource><name lang=\"se\">TROLLH��TTAN</name><locode>SETRO</locode><nationality>SE</nationality><lat>58.2833</lat><lon>12.2833</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>73</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R2993948</portId><portSource>IBNET</portSource><name lang=\"se\">VARG��N</name><locode>SEV��N</locode><nationality>SE</nationality><lat>58.3333</lat><lon>12.3667</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>74</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1095</portId><portSource>IBNET</portSource><name lang=\"se\">V��NERSBORG</name><locode>SEVBG</locode><nationality>SE</nationality><lat>58.3833</lat><lon>12.3167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>75</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1107</portId><portSource>IBNET</portSource><name lang=\"se\">��M��L</name><locode>SE��M��</locode><nationality>SE</nationality><lat>59.05</lat><lon>12.7167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>76</displayOrder><nameDisplayOffset>W</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R921</portId><portSource>IBNET</portSource><name lang=\"se\">GRUV��N</name><locode>SEGR��</locode><nationality>SE</nationality><lat>59.3333</lat><lon>13.1167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>77</displayOrder><nameDisplayOffset>NW</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1049</portId><portSource>IBNET</portSource><name lang=\"se\">SKOGHALL</name><locode>SESKO</locode><nationality>SE</nationality><lat>59.3167</lat><lon>13.4333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>78</displayOrder><nameDisplayOffset>N</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R965</portId><portSource>IBNET</portSource><name lang=\"se\">KARLSTAD</name><locode>SEKSD</locode><nationality>SE</nationality><lat>59.3667</lat><lon>13.5333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>79</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R983</portId><portSource>IBNET</portSource><name lang=\"se\">KRISTINEHAMN</name><locode>SEKRN</locode><nationality>SE</nationality><lat>59.3167</lat><lon>14.1</lon><seaArea>Baltic Sea</seaArea><displayOrder>80</displayOrder><nameDisplayOffset>NE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1025</portId><portSource>IBNET</portSource><name lang=\"se\">OTTERB��CKEN</name><locode>SEOTT</locode><nationality>SE</nationality><lat>58.95</lat><lon>14.0333</lon><seaArea>Baltic Sea</seaArea><displayOrder>81</displayOrder><nameDisplayOffset>E</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R1009</portId><portSource>IBNET</portSource><name lang=\"se\">MARIESTAD</name><locode>SEMAD</locode><nationality>SE</nationality><lat>58.7167</lat><lon>13.8167</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>82</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R3200539</portId><portSource>IBNET</portSource><name lang=\"se\">H��NS��TER</name><locode>SEH��S</locode><nationality>SE</nationality><lat>58.6333</lat><lon>13.4333</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>83</displayOrder><nameDisplayOffset>SE</nameDisplayOffset></portInfo></port><port xmlns=\"IBNet_Baltice_Ports\"><portInfo><portId>2R993</portId><portSource>IBNET</portSource><name lang=\"se\">LIDK��PING</name><locode>SELID</locode><nationality>SE</nationality><lat>58.5167</lat><lon>13.1833</lon><seaArea>Outside Baltic Sea</seaArea><displayOrder>84</displayOrder><nameDisplayOffset>S</nameDisplayOffset></portInfo></port></ports></ns4:PortsResponse></soapenv:Body></soapenv:Envelope>";
}