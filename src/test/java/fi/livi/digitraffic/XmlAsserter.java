package fi.livi.digitraffic;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;

import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

public class XmlAsserter extends ResponseAsserter {
    private static final XmlMapper XML_MAPPER = XmlMapper.builder()
            .disable(XmlReadFeature.AUTO_DETECT_XSI_TYPE)
            .build();
    public XmlAsserter(final MockHttpServletResponse response, final HttpStatus expectedStatus) {
        super(response, expectedStatus);

        expectXml();
    }

    public static XmlAsserter ok(final MockHttpServletResponse response) {
        return new XmlAsserter(response, HttpStatus.OK);
    }

    public void run() throws UnsupportedEncodingException, JacksonException {
        super.run();
    }

    public void expectContent(final Consumer<ObjectNode> consumer) throws JacksonException, UnsupportedEncodingException {
        this.run();

        consumer.accept(XML_MAPPER.readValue(response.getContentAsString(), ObjectNode.class));
    }
}
