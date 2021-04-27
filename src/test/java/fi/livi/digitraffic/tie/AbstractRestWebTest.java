package fi.livi.digitraffic.tie;

import java.util.Arrays;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@TestPropertySource(properties = { "spring.localstack.enabled=false" })
public abstract class AbstractRestWebTest extends AbstractSpringJUnitTest {
    protected final MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;

    private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;

    protected static final Matcher<String> ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER = Matchers.not(Matchers.matchesRegex("([0-9]{4})-(1[0-2]|0[1-9])-([0-3][0-9])T([0-2][1-9]):([0-6][1-9]):([0-6][1-9])(\\.[0-9]{3})?Z"));
    protected static final Matcher<String> NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER = Matchers.not(Matchers.matchesRegex("([0-9]{4})-(1[0-2]|0[1-9])-([0-3][0-9])T([0-2][1-9]):([0-6][1-9])(:([0-6][1-9])){0,1}(\\.[0-9]{0,3}){0,1}[+|-]"));
    protected static final Matcher<String> ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_MATCHER = Matchers.allOf(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER, NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER);
    protected static final ResultMatcher ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_RESULT_MATCHER = MockMvcResultMatchers.content().string(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_MATCHER);

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    protected GenericApplicationContext applicationContext;

    protected MockMvc mockMvc;

    @Autowired
    void setConverters(final HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @BeforeEach
    public void metadataTestBaseBefore() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    protected void assertTimesFormatMatches(final String content) {
        assertTrue(NO_ISO_DATE_TIME_WITH_OFFSET_MATCHER.matches(content));
        assertTrue(ISO_DATE_TIME_WITH_Z_OFFSET_MATCHER.matches(content));
    }
}
