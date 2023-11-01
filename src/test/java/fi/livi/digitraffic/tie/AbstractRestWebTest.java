package fi.livi.digitraffic.tie;

import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER;
import static fi.livi.digitraffic.tie.helper.DateHelperTest.NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fi.livi.digitraffic.tie.controller.DtMediaType;

public abstract class AbstractRestWebTest extends AbstractSpringJUnitTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractRestWebTest.class);

    protected final MediaType DT_JSON_CONTENT_TYPE = DtMediaType.APPLICATION_JSON;

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    protected GenericApplicationContext applicationContext;

    protected MockMvc mockMvc;

    @Autowired
    void setConverters(final HttpMessageConverter<?>[] converters) {

        final HttpMessageConverter<?> mappingJackson2HttpMessageConverter = Arrays.stream(converters).filter(
            hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElseThrow();

        assertNotNull(mappingJackson2HttpMessageConverter, "the JSON message converter must not be null");
    }

    @BeforeEach
    public void metadataTestBaseBefore() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    protected void assertTimesFormatMatchesIsoDateTimeWithZ(final String content) {
        assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(content));
        assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(content));
    }

    protected ResultActions executeGet(final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);
        get.contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(get);
    }

    protected ResultActions expectOk(final ResultActions rs) throws Exception {
        return rs.andExpect(status().isOk());
    }

    protected ResultActions expectOkFeatureCollectionWithSize(final ResultActions rs, final int featuresSize) throws Exception {
        return rs.andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(featuresSize)));
    }

    protected ResultActions expectOkFeature(final ResultActions rs) throws Exception {
        return rs.andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("Feature")));
    }

    protected ResultActions logInfoResponse(final ResultActions result) throws UnsupportedEncodingException {
        return logResponse(result, false);
    }

    protected ResultActions logDebugResponse(final ResultActions result) throws UnsupportedEncodingException {
        return logResponse(result, true);
    }
    private ResultActions logResponse(final ResultActions result, final boolean debug) throws UnsupportedEncodingException {
        final String responseStr = result.andReturn().getResponse().getContentAsString();
        if (debug) {
            log.debug("\n" + responseStr);
        } else {
            log.info("\n" + responseStr);
        }
        return result;
    }

}
