package fi.livi.digitraffic.tie.helper;

import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.AbstractTest;

public class GZipUtilsTest extends  AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(GZipUtilsTest.class);

    @Test
    public void testCompression() {
        final String compressedBase64String = GZipUtils.compressToBase64String(FEATURE);
        final String decompressedBase64String = GZipUtils.decompressBase64String(compressedBase64String);
        assertEquals(FEATURE, decompressedBase64String);
        assertEquals(BASE64, compressedBase64String);
    }

    @Test
    public void testCompressionResultIsUnderOneThird() throws IOException {
        final String original = readResourceContent("classpath:tloik/traffic-message/GUID50409331.json");
        final String compressedBase64String = GZipUtils.compressToBase64String(original);
        final String decompressedOriginal = GZipUtils.decompressBase64String(compressedBase64String);
        log.info("Compressed from {} bytes to {} bytes", original.getBytes().length, compressedBase64String.getBytes().length);
        assertTrue((double)compressedBase64String.getBytes().length / original.getBytes().length < 1.0/3 );
        assertEquals(original, decompressedOriginal);
    }

    @Test
    public void testCompressionResultIsAroundOneThird() throws IOException {
        final String original = readResourceContent("classpath:tloik/traffic-message/00018_Etelä-Savo.json");
        final String compressedBase64String = GZipUtils.compressToBase64String(original);
        final String decompressedOriginal = GZipUtils.decompressBase64String(compressedBase64String);
        log.info("Compressed from {} bytes to {} bytes", original.getBytes().length, compressedBase64String.getBytes().length);
        assertTrue((double)compressedBase64String.getBytes().length / original.getBytes().length < 0.35 );
        assertEquals(original, decompressedOriginal);
    }

    final static String FEATURE = "{\n" +
        "  \"type\": \"Feature\",\n" +
        "  \"geometry\": {\n" +
        "    \"type\": \"Point\",\n" +
        "    \"coordinates\": [\n" +
        "      25.694914,\n" +
        "      64.483873\n" +
        "    ]\n" +
        "  },\n" +
        "  \"properties\": {\n" +
        "    \"situationId\": \"GUID50408990\",\n" +
        "    \"situationType\": \"TRAFFIC_ANNOUNCEMENT\",\n" +
        "    \"trafficAnnouncementType\": \"ACCIDENT_REPORT\",\n" +
        "    \"version\": 21,\n" +
        "    \"releaseTime\": \"2023-01-13T07:22:18.767Z\",\n" +
        "    \"versionTime\": \"2023-01-13T09:25:23.542Z\",\n" +
        "    \"announcements\": [\n" +
        "      {\n" +
        "        \"language\": \"FI\",\n" +
        "        \"title\": \"Tie 4, Siikalatva. Liikennetiedote onnettomuudesta. Tilanne muuttunut. \",\n" +
        "        \"location\": {\n" +
        "          \"countryCode\": 6,\n" +
        "          \"locationTableNumber\": 17,\n" +
        "          \"locationTableVersion\": \"1.11.43\",\n" +
        "          \"description\": \"Tie 4 välillä Jyväskylä - Oulu, Siikalatva.\\nTarkempi paikka: Paikasta Hyttikoski 7,7 km, suuntaan Rantsila.\"\n" +
        "        },\n" +
        "        \"locationDetails\": {\n" +
        "          \"roadAddressLocation\": {\n" +
        "            \"primaryPoint\": {\n" +
        "              \"municipality\": \"Siikalatva\",\n" +
        "              \"province\": \"Pohjois-Pohjanmaa\",\n" +
        "              \"country\": \"Suomi\",\n" +
        "              \"roadAddress\": {\n" +
        "                \"road\": 4,\n" +
        "                \"roadSection\": 354,\n" +
        "                \"distance\": 7727\n" +
        "              },\n" +
        "              \"alertCLocation\": {\n" +
        "                \"locationCode\": 1183,\n" +
        "                \"name\": \"Hyttikoski\",\n" +
        "                \"distance\": 7727\n" +
        "              }\n" +
        "            },\n" +
        "            \"secondaryPoint\": {\n" +
        "              \"municipality\": \"Siikalatva\",\n" +
        "              \"province\": \"Pohjois-Pohjanmaa\",\n" +
        "              \"country\": \"Suomi\",\n" +
        "              \"roadAddress\": {\n" +
        "                \"road\": 4,\n" +
        "                \"roadSection\": 354,\n" +
        "                \"distance\": 7727\n" +
        "              },\n" +
        "              \"alertCLocation\": {\n" +
        "                \"locationCode\": 1186,\n" +
        "                \"name\": \"Rantsila\",\n" +
        "                \"distance\": 3232\n" +
        "              }\n" +
        "            },\n" +
        "            \"direction\": \"UNKNOWN\"\n" +
        "          }\n" +
        "        },\n" +
        "        \"features\": [\n" +
        "          {\n" +
        "            \"name\": \"Onnettomuus\"\n" +
        "          },\n" +
        "          {\n" +
        "            \"name\": \"Raskaan ajoneuvon nostotyö\"\n" +
        "          },\n" +
        "          {\n" +
        "            \"name\": \"Tie on suljettu liikenteeltä\"\n" +
        "          }\n" +
        "        ],\n" +
        "        \"roadWorkPhases\": [],\n" +
        "        \"comment\": \"Kiertotie kulkee tien 88 ja 86 kautta\",\n" +
        "        \"timeAndDuration\": {\n" +
        "          \"startTime\": \"2023-01-13T07:20:00Z\"\n" +
        "        },\n" +
        "        \"additionalInformation\": \"Liikenne- ja kelitiedot verkossa: https://liikennetilanne.fintraffic.fi/\",\n" +
        "        \"sender\": \"Fintraffic Tieliikennekeskus Tampere\"\n" +
        "      }\n" +
        "    ],\n" +
        "    \"contact\": {\n" +
        "      \"phone\": \"02002100\",\n" +
        "      \"email\": \"tampere.liikennekeskus@fintraffic.fi\"\n" +
        "    },\n" +
        "    \"dataUpdatedTime\": \"2023-01-13T09:25:26Z\"\n" +
        "  }\n" +
        "}";

    final private static String BASE64 = "H4sIAAAAAAAAAO1Wy27iSBTd5ytKXoPjB2Bg1YgkM0z3QESTaal7ola1fUkqtquseiChiL/JN/QP5Mfm+hlDnIl6Mbthgct1z31X3ePHM0Isvc/AmhLrCqg2EqxevnkHIgUt9yh4xPcW7FowrgsQboZCyIhxqkGh7FuxSYg3tEeTwcQd9KqN0cAejP1x4Bfvt/h/KLxkUmQgNSu0Kz+KaUM1E3wR5e5+u1lcDJ2BM55MnNprA9lUMW3Ws6urxfz7bLlc3Sznl39eLjc1WEu63bJwxrkwPIQUuK7VZvP54gKh39eX16t1o7EDqdA4Ijy32pKQAFWwYWmh6Dme33fcvutvnGDqeVN3bAej4OuJhS74ZOoNp55vDwdeA6et2Np1fKyeCEkovzP0rmzUwuq9SDTTSVkEBmTQI58Zi2lC9Y7a5BOugXPACkdCAxH5WovUmAiURsCGoWEOBHe0Ntxom7RtJyIs6tx0p9oPMVw8HHMR5Z5Hvbas1tnQHwksTfoDJGLc4G3QX029Ldd2XXvgW0dgjDWULKsCKfMku+enhCXJ8xP5Y49rFe/zdZ+sTGKOivA3OpExpBkjGWVxTKfkGp8U8ye/77VmsVAxI0EvIHHaI8pgapRysqbYDCyPbTWxHDpKcwGaskSdVkgKGs2iSIJSn7qLWBx/llK5L2/UqRTlqeEsZBlNmM4vovWS1VGBKltix/AMlVf0/kEw1c+flKe0A151sLBqRMpeI1oZdMRWAVAyONWsRJ8hrLL2h12YiGEHyoCDwAtOAIdX8dAEJ8X8zWIWmLop1cF03bHf4ZnT8lq+dP9V9u/Hd/R+Eq2lIBQ8+r+1/2lrR//S2vryvtNY3/O9X2psxGSTu3Wz/LhcfVlaZ13a7VmxLZm1Pdrz38k0qGNfNUNaHZnuva+6pirOZxd9EBzMTnDChdJC759//qqpfMqivjLJA0ZjSFJQiQZI9PNTd8q3rZTzY/JFyPj6HmmzSLwtDUWac13u5yPD3gskKBKbJAYguORkPCYPlIxHJKZITPSY71KY8ejCyE5qwt5K/QZPO1PH+do9zmkUsdwcTRZ8K2Ra27ZqCu3nAcWA97XgUoIUj6NDIZvca52p6fl50pBtwan2Fq9++e2By/N2Cgp4VLCiddVgkIqhthCDio0iG5ri1xHUAZeFvm2+vJCmwvZssbJ77Hpu1fEcx3Mdp/FpQYoklYt0adM+dvXhKNbSYVUdK6Ka3mT4D9Hb3zOjoqyHs8M/AwBzk1IKAAA=";
}