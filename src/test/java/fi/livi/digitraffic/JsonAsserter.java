package fi.livi.digitraffic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public void run() throws UnsupportedEncodingException, JsonProcessingException {
        super.run();

        final var jsonNode = new ObjectMapper().readValue(response.getContentAsString(), JsonNode.class);

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
            throws JsonProcessingException, UnsupportedEncodingException {
        this.run();

        function.accept(new ObjectMapper().readValue(response.getContentAsString(), JsonNode.class));
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
