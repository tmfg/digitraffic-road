package fi.livi.digitraffic;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import tools.jackson.core.JacksonException;

public class ResponseAsserter<T extends ResponseAsserter<T>> {
    protected final MockHttpServletResponse response;
    private final HttpStatus expectedStatus;
    private String expectedContentType;
    private boolean expectLastModifiedHeaderPresent = false;

    public ResponseAsserter(final MockHttpServletResponse response, final HttpStatus expectedStatus) {
        this.response = response;
        this.expectedStatus = expectedStatus;
    }

    public static ResponseAsserter<?> ok(final MockHttpServletResponse response) {
        return new ResponseAsserter<>(response, HttpStatus.OK);
    }

    public static ResponseAsserter<?> notFound(final MockHttpServletResponse response) {
        return new ResponseAsserter<>(response, HttpStatus.NOT_FOUND);
    }

    public void run() throws UnsupportedEncodingException, JacksonException {
        Assertions.assertEquals(expectedStatus.value(), response.getStatus());

        if(expectedContentType != null) {
            Assertions.assertEquals(expectedContentType, response.getContentType());
        }

        if (this.expectLastModifiedHeaderPresent) {
            final String lastModified = response.getHeader("Last-Modified");
            Assertions.assertNotNull(lastModified, "Last-Modified header should be present");
            Assertions.assertFalse(lastModified.isEmpty(), "Last-Modified header should not be empty");
        }
    }

    public void expectJson() {
        this.expectedContentType = "application/json;charset=UTF-8";
    }

    public void expectXml() {
        this.expectedContentType = "application/xml;charset=UTF-8";
    }

    @SuppressWarnings("unchecked")
    public T expectLastModifiedHeaderPresent() {
        this.expectLastModifiedHeaderPresent = true;
        return (T) this;
    }
}
