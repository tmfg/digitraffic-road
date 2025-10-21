package fi.livi.digitraffic;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ResponseAsserter {
    protected final MockHttpServletResponse response;
    private final HttpStatus expectedStatus;

    private String expectedContentType;

    public ResponseAsserter(final MockHttpServletResponse response, final HttpStatus expectedStatus) {
        this.response = response;
        this.expectedStatus = expectedStatus;
    }

    public static ResponseAsserter ok(final MockHttpServletResponse response) {
        return new ResponseAsserter(response, HttpStatus.OK);
    }

    public static ResponseAsserter notFound(final MockHttpServletResponse response) {
        return new ResponseAsserter(response, HttpStatus.NOT_FOUND);
    }

    public void run() throws UnsupportedEncodingException, JsonProcessingException {
        Assertions.assertEquals(expectedStatus.value(), response.getStatus());

        if(expectedContentType != null) {
            Assertions.assertEquals(expectedContentType, response.getContentType());
        }
    }

    public ResponseAsserter expectJson() {
        this.expectedContentType = "application/json;charset=UTF-8";

        return this;
    }

    public ResponseAsserter expectXml() {
        this.expectedContentType = "application/xml;charset=UTF-8";

        return this;
    }
}
