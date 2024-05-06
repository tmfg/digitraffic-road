package fi.livi.digitraffic.tie.service.weather.forecast;

import static fi.livi.digitraffic.tie.TestUtils.loadResource;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.zip.GZIPOutputStream;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.helper.DateHelper;
import org.springframework.web.reactive.function.client.WebClient;

@ConditionalOnExpression("'${config.test}' == 'true'")
@Component
public class ForecastSectionTestHelper {
    private final String baseUrl;
    private final String suid;
    private final String user;
    private final String pass;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ForecastSectionTestHelper(final WebClient webClient,
                                     final ObjectMapper objectMapper,
                                     @Value("${roadConditions.baseUrl}") final String baseUrl,
                                     @Value("${roadConditions.suid}") final String suid,
                                     @Value("${roadConditions.user}") final String user,
                                     @Value("${roadConditions.pass}") final String pass) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.suid = suid;
        this.user = user;
        this.pass = pass;
    }

    public ForecastSectionClient createForecastSectionClient(final MockWebServer server) {
        return new ForecastSectionClient(webClient, objectMapper, server.url("").toString(), suid, user, pass);
    }

    public void serveGzippedMetadata(final MockWebServer server, final int version) throws IOException {
        serveGzippedResponse(server, version, true);
    }

    public void serveGzippedData(final MockWebServer server, final int version) throws IOException {
        serveGzippedResponse(server, version, false);
    }

    public static final Instant NOW = DateHelper.getNowWithoutMillis();
    public final static String[] TIMES = new String[] { NOW.toString(),
                                                        NOW.plus(2, HOURS).toString(),
                                                        NOW.plus(4, HOURS).toString(),
                                                        NOW.plus(6, HOURS).toString(),
                                                        NOW.plus(12, HOURS).toString() };
    private final static String[] PLACEHOLDERS = new String[] { "TIME_0",
                                                                "TIME_2",
                                                                "TIME_4",
                                                                "TIME_6",
                                                                "TIME_12" };

    private void serveGzippedResponse(final MockWebServer server, final int version, final boolean metadata) throws IOException {
        final String resourcePattern = getResourcePattern(version, metadata);
        final byte[] data = gzippedBytes(resourcePattern);

        // DO NOT set encoding as gzip! Because the real service does not
        server.enqueue(new MockResponse()
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
            .setBody(new Buffer().write(data))
        );
    }

    private String getResourcePattern(final int version, final boolean metadata) {
        final String prefix = "classpath:forecastsection/";
        if (metadata) {
            if (version == 1) {
                return prefix + "roadsV1.json";
            } else if (version == 2) {
                return prefix + "roadsV2.json";
            } else if (version == 3) {
                return prefix + "roadsV3.json.gz";
            }
        } else {
            if (version == 1) {
                return prefix + "keliennuste-v1-template.json";
            } else if (version == 2) {
                return prefix + "keliennuste-v2-template.json";
            } else if (version == 3) {
                return prefix + "keliennuste-v3.json.gz";
            }
        }
        throw new IllegalArgumentException("Unsuported version " + version);
    }

    public String getUrl(final int version, final boolean metadata) {
        if (metadata) {
            if (version == 1) {
                return baseUrl + "roadsV1.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            } else if (version == 2) {
                return baseUrl + "roadsV2.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            } else if (version == 3) {
                return baseUrl + "roadsV3.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            }
        } else {
            if (version == 1) {
                return baseUrl + "json/keliennuste.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            } else if (version == 2) {
                return baseUrl + "keliennuste-v2.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            } else if (version == 3) {
                return baseUrl + "keliennuste-v3.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            }
        }
        throw new IllegalArgumentException("Unsuported version " + version);
    }

    private byte[] gzippedBytes(final String resourcePattern) throws IOException {
        final Resource data = loadResource(resourcePattern);
        // Resource already gzipped
        if (resourcePattern.contains(".gz")) {
            return data.getContentAsByteArray();
        }

        // Manipulate and gzip resource
        final ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream(); // does not need closing
        try(final GZIPOutputStream gzipOs = new GZIPOutputStream(byteArrayOs);
            final InputStream dis = data.getInputStream();
            final Reader decoder = new InputStreamReader(dis, StandardCharsets.UTF_8);
            final BufferedReader buffered = new BufferedReader(decoder)) {

            while(buffered.ready()) {
                final String line = buffered.readLine();
                final String resultLine = StringUtils.replaceEach(line, PLACEHOLDERS, TIMES);
                gzipOs.write(resultLine.getBytes(StandardCharsets.UTF_8));
            }

            // must be closed to flush content to byte array
            gzipOs.close();

            return byteArrayOs.toByteArray();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }


    }
}


