package fi.livi.digitraffic.tie;

import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER;
import static fi.livi.digitraffic.tie.helper.DateHelperTest.NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.context.WebApplicationContext;

import fi.livi.digitraffic.tie.controller.DtMediaType;

@SpringBootTest(classes = RoadApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
public abstract class AbstractRestWebTest extends AbstractSpringJUnitTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractRestWebTest.class);

    protected final MediaType DT_JSON_CONTENT_TYPE = DtMediaType.APPLICATION_JSON;

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    protected WebTestClient webTestClient;

    protected void assertTimesFormatMatchesIsoDateTimeWithZ(final String content) {
        assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(content));
        assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(content));
    }

    protected void assertBodyTimesFormatMatchesIsoDateTimeWithZ(final EntityExchangeResult<byte[]> result) {
        assertTimesFormatMatchesIsoDateTimeWithZ(new String(result.getResponseBody(), StandardCharsets.UTF_8));
    }

    protected WebTestClient.ResponseSpec executeGet(final String url) throws Exception {
        return executeGet(url, MediaType.APPLICATION_JSON);
    }

    protected WebTestClient.ResponseSpec executeGetXml(final String url) throws Exception {
        return executeGet(url, MediaType.APPLICATION_XML);
    }

    private WebTestClient.ResponseSpec executeGet(final String url, final MediaType mediaType) throws Exception {
        return webTestClient
                .get()
                .uri(url)
                .accept(mediaType)
                .header("Content-Type", mediaType.toString())
                .header("Accept-Charset", StandardCharsets.UTF_8.name())
                .exchange();
    }

    /** Reads the whole response to a MockHttpServletResponse for the response asserters. */
    protected MockHttpServletResponse toMockResponse(final WebTestClient.ResponseSpec rs) throws Exception {
        final EntityExchangeResult<byte[]> result = rs.expectBody().returnResult();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(result.getStatus().value());
        result.getResponseHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
        if (result.getResponseBody() != null) {
            response.getOutputStream().write(result.getResponseBody());
        }
        return response;
    }

    protected WebTestClient.ResponseSpec expectOk(final WebTestClient.ResponseSpec rs) {
        return rs.expectStatus().isOk();
    }

    protected WebTestClient.BodyContentSpec expectOkFeatureCollectionWithSize(final WebTestClient.ResponseSpec rs, final int featuresSize) {
        return rs.expectStatus().isOk()
                .expectBody()
                .jsonPath("type").value(equalTo("FeatureCollection"))
                .jsonPath("features").value(hasSize(featuresSize));
    }

    protected WebTestClient.BodyContentSpec expectOkFeature(final WebTestClient.ResponseSpec rs) {
        return rs.expectStatus().isOk()
                .expectBody()
                .jsonPath("type").value(equalTo("Feature"));
    }

    /** Logs the response body and returns the body spec for further assertions. Do status and header checks before calling. */
    protected WebTestClient.BodyContentSpec logInfoResponse(final WebTestClient.ResponseSpec rs) {
        return logResponse(rs, false);
    }

    /** Logs the response body and returns the body spec for further assertions. Do status and header checks before calling. */
    protected WebTestClient.BodyContentSpec logDebugResponse(final WebTestClient.ResponseSpec rs) {
        return logResponse(rs, true);
    }

    private WebTestClient.BodyContentSpec logResponse(final WebTestClient.ResponseSpec rs, final boolean debug) {
        return rs.expectBody().consumeWith(result -> {
            final String responseStr = new String(result.getResponseBody() != null ? result.getResponseBody() : new byte[0], StandardCharsets.UTF_8);
            if (debug) {
                log.debug("\n" + responseStr);
            } else {
                log.info("\n" + responseStr);
            }
        });
    }

}
