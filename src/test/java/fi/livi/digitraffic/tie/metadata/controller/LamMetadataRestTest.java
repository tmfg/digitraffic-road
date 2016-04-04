package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class LamMetadataRestTest extends MetadataTest {

    private final MediaType contentType = MediaType.APPLICATION_JSON_UTF8;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    void setConverters(final HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testLamMetadataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH + MetadataApplicationConfiguration.API_METADATA_PART_PATH + "/lam-stations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.type", is("LamStationFeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("LamStationFeature")))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.crs.type", is("link")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)));
                // coordinates[0]=6675908.0
                // coordinates[1]=382080.0
                // coordinates[2]=0


    }
}
