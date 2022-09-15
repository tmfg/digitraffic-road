package fi.livi.digitraffic.tie;

import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER;
import static fi.livi.digitraffic.tie.helper.DateHelperTest.NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fi.livi.digitraffic.tie.controller.DtMediaType;

public abstract class AbstractRestWebTest extends AbstractSpringJUnitTest {

    protected final MediaType DT_JSON_CONTENT_TYPE = DtMediaType.APPLICATION_JSON;

    private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    protected GenericApplicationContext applicationContext;

    protected MockMvc mockMvc;

    @Autowired
    void setConverters(final HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters).filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElseThrow();

        assertNotNull(this.mappingJackson2HttpMessageConverter, "the JSON message converter must not be null");
    }

    @BeforeEach
    public void metadataTestBaseBefore() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    protected void assertTimesFormatMatchesIsoDateTimeWithZ(final String content) {
        assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_CONTAINS_MATCHER.matches(content));
        assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_CONTAINS_MATCHER.matches(content));
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
}
