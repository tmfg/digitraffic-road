package fi.livi.digitraffic;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

public class JsonAsserter extends ResponseAsserter {
    private String expectedType;
    private Integer expectedFeatureCount;

    public JsonAsserter(final MockHttpServletResponse response, final HttpStatus expectedStatus) {
        super(response, expectedStatus);

        expectJson();
    }

    public static JsonAsserter ok(final MockHttpServletResponse response) {
        return new JsonAsserter(response, HttpStatus.OK);
    }

    public static JsonAsserter bad(final MockHttpServletResponse response) {
        return new JsonAsserter(response, HttpStatus.BAD_REQUEST);
    }

    public void run() throws UnsupportedEncodingException, JacksonException {
        super.run();

        final var jsonNode = JsonMapper.builder().build().readValue(response.getContentAsString(), JsonNode.class);

        if(this.expectedType != null) {
            Assertions.assertNotNull(jsonNode.get("type"));
            Assertions.assertEquals(this.expectedType, jsonNode.get("type").asText());
        }

        if(this.expectedFeatureCount != null) {
            Assertions.assertNotNull(jsonNode.get("features"));
            Assertions.assertEquals(this.expectedFeatureCount, jsonNode.get("features").size());
        }
    }

    public void expectContent(final Consumer<JsonNode> function)
            throws JacksonException, UnsupportedEncodingException {
        this.run();

        function.accept(JsonMapper.builder().build().readValue(response.getContentAsString(), JsonNode.class));
    }

    public JsonAsserter expectType(final String expectedType) {
        this.expectedType = expectedType;

        return this;
    }

    public ResponseAsserter expectFeatureCount(final int expectedCount) {
        this.expectedFeatureCount = expectedCount;

        return this;
    }
}
