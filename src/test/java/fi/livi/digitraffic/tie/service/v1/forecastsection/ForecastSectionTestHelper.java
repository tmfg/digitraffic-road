package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static fi.livi.digitraffic.tie.TestUtils.loadResource;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.service.RestTemplateGzipService;

@ConditionalOnNotWebApplication
@Component
public class ForecastSectionTestHelper {
    private static final Logger log = LoggerFactory.getLogger(ForecastSectionTestHelper.class);

    private final String baseUrl;
    private final String suid;
    private final String user;
    private final String pass;
    private final RestTemplateGzipService restTemplateGzipService;
    private final ObjectMapper objectMapper;

    public ForecastSectionTestHelper(final RestTemplateGzipService restTemplateGzipService,
                                     final ObjectMapper objectMapper,
                                     @Value("${roadConditions.baseUrl}") final String baseUrl,
                                     @Value("${roadConditions.suid}") final String suid,
                                     @Value("${roadConditions.user}") final String user,
                                     @Value("${roadConditions.pass}") final String pass) {
        this.restTemplateGzipService = restTemplateGzipService;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.suid = suid;
        this.user = user;
        this.pass = pass;
    }

    public ForecastSectionClient createForecastSectionClient() {
        return new ForecastSectionClient(restTemplateGzipService, objectMapper, baseUrl, suid, user, pass);
    }

    public void serverExpectMetadata(final MockRestServiceServer server, final int version) {
        serverExpect(server, version, true);
    }

    public void serverExpectData(final MockRestServiceServer server, final int version) {
        serverExpect(server, version, false);
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

    private void serverExpect(final MockRestServiceServer server, final int version, final boolean metadata) {
        final String url = getUrl(version, metadata);
        final String resourcePattern = getResourcePattern(version, metadata);
        final Resource data = loadForecastResourceAsGzippedResource(resourcePattern);



        log.info("serverExpect {}", url);
        log.info("serverReturn {}", resourcePattern);
        server.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
              .andRespond(MockRestResponseCreators.withSuccess(data, MediaType.APPLICATION_OCTET_STREAM));
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

    private Resource loadForecastResourceAsGzippedResource(final String resourcePattern) {
        final Resource data = loadResource(resourcePattern);
        // Resource already gzipped
        if (resourcePattern.contains(".gz")) {
            return data;
        }

        // Manipulate and gzip resource
        try {
            final ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
            final GZIPOutputStream gzipOs = new GZIPOutputStream(byteArrayOs);
            final Reader decoder = new InputStreamReader(data.getInputStream(), StandardCharsets.UTF_8);
            final BufferedReader buffered = new BufferedReader(decoder);
            String line = buffered.readLine();
            while (line != null) {
                final String resultLine = StringUtils.replaceEach(line, PLACEHOLDERS, TIMES);
                gzipOs.write(resultLine.getBytes(StandardCharsets.UTF_8));
                line = buffered.readLine();
            }
            gzipOs.close();
            byteArrayOs.close();
            return new ByteArrayResource(byteArrayOs.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }
}


