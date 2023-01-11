package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.TestUtils.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MvcResult;

import fi.livi.digitraffic.tie.AbstractRestWebTest;

public class StaticResourcesWebTest extends AbstractRestWebTest {

    @Value("${dt.domain.url}")
    public String dtDomainUrl;

    @Test
    public void testSchemaLocationUpdated() throws Exception {

        final Resource resource = loadResource("classpath:/schemas/datex2/DATEXIIResponseSchema_1_1.xsd");
        final String originalSchema = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        final String originallocation = StringUtils.substringAfter(originalSchema, "schemaLocation=\"");

        final String expectedOriginalLocationValue = "DATEXIISchema_2_2_3_with_definitions_FI.xsd";
        assertEquals(expectedOriginalLocationValue, originallocation.substring(0, expectedOriginalLocationValue.length()));

        final MvcResult result = expectOk(executeGet("/schemas/datex2/DATEXIIResponseSchema_1_1.xsd")).andReturn();
        final String content = result.getResponse().getContentAsString();

        final String location = StringUtils.substringAfter(content, "schemaLocation=\"");
        assertTrue(dtDomainUrl.length() > 0);
        assertEquals(dtDomainUrl, location.substring(0, dtDomainUrl.length()));
    }
}
