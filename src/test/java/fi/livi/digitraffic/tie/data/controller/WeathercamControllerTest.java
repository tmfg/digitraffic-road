package fi.livi.digitraffic.tie.data.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.net.URI;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class WeathercamControllerTest extends AbstractRestWebTest {

    private static final Logger log = LoggerFactory.getLogger(WeathercamControllerTest.class);

    @MockBean
    private CameraPresetHistoryService cameraPresetHistoryService;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String s3WeathercamBucketName;
    @Value("${dt.amazon.s3.weathercam.region}")
    private String s3WeathercamRegion;

    private final String imageName = "C7777701.jpg";
    private final String versionId = "qwerty";

    @Test
    public void getPublicImage() throws Exception {

        Mockito.when(cameraPresetHistoryService.findHistory(eq(getPresetId(imageName)), eq(versionId)))
            .thenReturn(createHistory(imageName, versionId, true, ZonedDateTime.now()));

        Mockito.when(cameraPresetHistoryService.resolveHistoryStatus(eq(imageName), eq(versionId)))
            .thenReturn(CameraPresetHistoryService.HistoryStatus.PUBLIC);

        MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.FOUND, null);
        //assertResponse(response, HttpStatus.FOUND, getVersionedRedirectUrl(imageName, versionId));
    }

    @Test
    public void getSecretImage() throws Exception {

        Mockito.when(cameraPresetHistoryService.findHistory(eq(getPresetId(imageName)), eq(versionId)))
            .thenReturn(createHistory(imageName, versionId, false, ZonedDateTime.now()));

        Mockito.when(cameraPresetHistoryService.resolveHistoryStatus(eq(imageName), eq(versionId)))
            .thenReturn(CameraPresetHistoryService.HistoryStatus.SECRET);

        MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.NOT_FOUND, null);
    }

    @Test
    public void getTooOldImage() throws Exception {

        Mockito.when(cameraPresetHistoryService.findHistory(eq(getPresetId(imageName)), eq(versionId)))
            .thenReturn(createHistory(imageName, versionId, true, ZonedDateTime.now().minusHours(25)));

        Mockito.when(cameraPresetHistoryService.resolveHistoryStatus(eq(imageName), eq(versionId)))
            .thenReturn(CameraPresetHistoryService.HistoryStatus.TOO_OLD);

        MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.NOT_FOUND, null);
    }

    @Test
    public void getNotExistingImage() throws Exception {

        Mockito.when(cameraPresetHistoryService.findHistory(anyString(), anyString()))
            .thenReturn(null);

        Mockito.when(cameraPresetHistoryService.resolveHistoryStatus(eq(imageName), eq(versionId)))
            .thenReturn(CameraPresetHistoryService.HistoryStatus.NOT_FOUND);

        MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.NOT_FOUND, null);
    }

    private void assertResponse(MockHttpServletResponse response, final HttpStatus httpStatus, final String redirectUrl) {
        log.info("HTTP response: {} and redirect: {}", response.getStatus(), response.getRedirectedUrl());
        Assert.assertEquals(httpStatus.value(), response.getStatus());
        Assert.assertEquals(redirectUrl, response.getRedirectedUrl());
    }

    private String getVersionedRedirectUrl(final String imageName, final String versionId) {
        return String.format("http://%s.s3-%s.amazonaws.com/%s-versions.jpg?versionId=%s", s3WeathercamBucketName, s3WeathercamRegion, getPresetId(imageName), versionId);
    }

    private CameraPresetHistory createHistory(final String imageName, final String versionId, final boolean publishable, final ZonedDateTime lastModified) {

        return new CameraPresetHistory(getPresetId(imageName), versionId, -1, lastModified, publishable, 10, ZonedDateTime.now());
    }

    private MockHttpServletResponse requestImage(final String imageName, final String versionId) throws Exception {

        final URI uri = URI.create(RoadWebApplicationConfiguration.WEATHERCAM_PATH + "/" + imageName + "?versionId=" + versionId);
        log.info("Request uri: {}", uri);
        final MockHttpServletRequestBuilder get = get(uri);

        return mockMvc.perform(get).andReturn().getResponse();
    }

    private static String getPresetId(String imageName) {
        return imageName.substring(0,8);
    }
}
