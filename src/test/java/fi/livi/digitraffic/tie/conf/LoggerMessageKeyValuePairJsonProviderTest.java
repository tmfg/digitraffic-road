package fi.livi.digitraffic.tie.conf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;

@RunWith(SpringRunner.class)
public class LoggerMessageKeyValuePairJsonProviderTest {
    private static final Logger log = LoggerFactory.getLogger(LoggerMessageKeyValuePairJsonProviderTest.class);

    final LoggerMessageKeyValuePairJsonProvider provider = new LoggerMessageKeyValuePairJsonProvider();

    private ByteArrayOutputStream out;
    private JsonGenerator jsonGenerator;

    @Before
    public void init() throws IOException {
        out = new ByteArrayOutputStream();
        final JsonFactory factory = new JsonFactory();
        jsonGenerator = factory.createGenerator(out, JsonEncoding.UTF8);
    }

    @Test
    public void simpleKeyValuePair() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=bar");
        Assert.assertEquals("{\"foo\":\"bar\"}", result);
    }

    @Test
    public void simpleKeyValuePair2() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("  \n  \t    foo=bar     \n   ");
        Assert.assertEquals("{\"foo\":\"bar\"}", result);
    }

    @Test
    public void keyValueChainTakesFirstPair() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo=bar=hello=world and=this");
        Assert.assertEquals("{\"foo\":\"bar\",\"and\":\"this\"}", result);
    }

    @Test
    public void xmlTagsAreStripped() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("a=b " + LONG_XML);
        Assert.assertEquals("{\"a\":\"b\"}", result);
    }

    @Test
    public void nullMessage() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson(null);
        Assert.assertEquals("{}", result);
    }

    @Test
    public void emptyMessage() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("");
        Assert.assertEquals("{}", result);
    }

    @Test
    public void emptyMessage2() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("    ");
        Assert.assertEquals("{}", result);
    }

    @Test
    public void emptyResultWhenSpaces() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson(" a = b ");
        Assert.assertEquals("{}", result);
    }

    @Test
    public void emptyResultWhenSpaces2() throws IOException {
        final String result = sendEventWithFormatedMessageAndReturnResultJson("foo =bar hello= world");
        Assert.assertEquals("{}", result);
    }

    private String sendEventWithFormatedMessageAndReturnResultJson(final String formattedMessage) throws IOException {
        jsonGenerator.writeStartObject();
        provider.writeTo(jsonGenerator, createEvent(formattedMessage));
        jsonGenerator.writeEndObject();
        jsonGenerator.close();
        final String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
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
}
