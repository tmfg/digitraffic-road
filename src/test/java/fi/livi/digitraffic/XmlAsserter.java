package fi.livi.digitraffic;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.jgroups.conf.XmlNode;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

public class XmlAsserter extends ResponseAsserter {
    public XmlAsserter(final MockHttpServletResponse response, final HttpStatus expectedStatus) {
        super(response, expectedStatus);

        expectXml();
    }

    public static XmlAsserter ok(final MockHttpServletResponse response) {
        return new XmlAsserter(response, HttpStatus.OK);
    }

    public void run() throws UnsupportedEncodingException, JsonProcessingException {
        super.run();
    }

    public void expectContent(final Consumer<ObjectNode> consumer) throws JsonProcessingException, UnsupportedEncodingException {
        this.run();

        consumer.accept(new XmlMapper().readValue(response.getContentAsString(), ObjectNode.class));
    }
}
