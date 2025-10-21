package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.TestUtils.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MvcResult;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.AbstractRestWebTest;

public class StaticResourcesWebTest extends AbstractRestWebTest {

    private static final Logger log = LoggerFactory.getLogger(StaticResourcesWebTest.class);

    @Value("${dt.domain.url}")
    public String dtDomainUrl;

    @Test
    public void testSchemaLocationUpdated() throws Exception {
        // Load resource to compare http://localhost:9002/schemas/datex2/tms/3_5/xml/DATEXII_3_RoadTrafficData.xsd
        final Resource resource = loadResource("classpath:/schemas/datex2/tms/3_5/xml/DATEXII_3_RoadTrafficData.xsd");
        final String originalSchema = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        final String originalFirstLocationLocation = StringUtils.substringBefore(
                StringUtils.substringAfter(originalSchema, "schemaLocation=\""), "/>").trim();

        final String expectedOriginalLocationValue = "DATEXII_3_LocationReferencing.xsd";
        assertEquals(expectedOriginalLocationValue,
                     originalFirstLocationLocation.substring(0, expectedOriginalLocationValue.length()));

        // Get resource from server
        final MvcResult result = expectOk(executeGet("/schemas/datex2/tms/3_5/xml/DATEXII_3_RoadTrafficData.xsd")).andReturn();
        final String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

        final String actualLocation = StringUtils.substringAfter(content, "schemaLocation=\"");
        assertFalse(dtDomainUrl.isBlank());
        assertEquals(dtDomainUrl, actualLocation.substring(0, dtDomainUrl.length()));
        final String expectedLocation =
                URI.create(StringUtil.format("{}/{}/{}", dtDomainUrl, "/schemas/datex2/tms/3_5/xml/",
                                expectedOriginalLocationValue))
                        .normalize().toString();
        log.info("expectedLocation: {}", expectedLocation);
        log.info("actualLocation:   {}", actualLocation.substring(0, expectedLocation.length()));
        assertEquals(expectedLocation, actualLocation.substring(0, expectedLocation.length()));
    }
}
