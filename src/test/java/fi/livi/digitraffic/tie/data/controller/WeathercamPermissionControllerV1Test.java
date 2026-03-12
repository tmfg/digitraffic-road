package fi.livi.digitraffic.tie.data.controller;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.controller.weathercam.WeathercamPermissionControllerV1;
import fi.livi.digitraffic.tie.model.weathercam.CameraPresetHistory;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageThumbnailService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryDataService.HistoryStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class WeathercamPermissionControllerV1Test extends AbstractRestWebTest {

    private static final Logger log = LoggerFactory.getLogger(WeathercamPermissionControllerV1Test.class);

    @Autowired
    private WeathercamS3Properties weathercamS3Properties;

    @Autowired
    private ResourceLoader resourceLoader;

    @MockitoBean
    protected CameraPresetHistoryDataService cameraPresetHistoryDataServiceMock;

    @MockitoBean
    protected CameraImageThumbnailService cameraImageThumbnailServiceMock;

    private final String imageName = "C7777701.jpg";
    private final String versionId = "qwerty";

    @Test
    public void getPublicImage() throws Exception {

        Mockito.when(cameraPresetHistoryDataServiceMock.findHistoryVersionInclSecretInternal(eq(getPresetId(imageName)),
                        eq(versionId)))
                .thenReturn(createHistory(imageName, versionId, true, Instant.now()));

        Mockito.when(cameraPresetHistoryDataServiceMock.resolveHistoryStatusForVersion(eq(imageName), eq(versionId)))
                .thenReturn(HistoryStatus.PUBLIC);

        final MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.FOUND, getVersionedRedirectUrl(imageName, versionId));
    }

    @Test
    public void getSecretImageReturnsImageNotAvailable() throws Exception {

        Mockito.when(cameraPresetHistoryDataServiceMock.findHistoryVersionInclSecretInternal(eq(getPresetId(imageName)),
                        eq(versionId)))
                .thenReturn(createHistory(imageName, versionId, false, Instant.now()));

        Mockito.when(cameraPresetHistoryDataServiceMock.resolveHistoryStatusForVersion(eq(imageName), eq(versionId)))
                .thenReturn(HistoryStatus.SECRET);

        final MockHttpServletResponse response = requestImage(imageName, versionId);
        assertImageNotAvailableResponse(response);
    }

    @Test
    public void getTooOldImageReturnsImageNotAvailable() throws Exception {

        Mockito.when(cameraPresetHistoryDataServiceMock.findHistoryVersionInclSecretInternal(eq(getPresetId(imageName)),
                        eq(versionId)))
                .thenReturn(createHistory(imageName, versionId, true, Instant.now().minus(25, ChronoUnit.HOURS)));

        Mockito.when(cameraPresetHistoryDataServiceMock.resolveHistoryStatusForVersion(eq(imageName), eq(versionId)))
                .thenReturn(HistoryStatus.TOO_OLD);

        final MockHttpServletResponse response = requestImage(imageName, versionId);
        assertImageNotAvailableResponse(response);
    }

    @Test
    public void getNotExistingImageReturnsImageNotAvailable() throws Exception {

        Mockito.when(cameraPresetHistoryDataServiceMock.findHistoryVersionInclSecretInternal(anyString(), anyString()))
                .thenReturn(null);

        Mockito.when(cameraPresetHistoryDataServiceMock.resolveHistoryStatusForVersion(eq(imageName), eq(versionId)))
                .thenReturn(HistoryStatus.NOT_FOUND);

        final MockHttpServletResponse response = requestImage(imageName, versionId);
        assertImageNotAvailableResponse(response);
    }

    @Test
    public void getThumbnailOfMissingCurrentImageReturnsImageNotAvailable() throws Exception {
        // When requesting a thumbnail of a current image that has been deleted from S3 (non-public camera),
        // the CameraImageThumbnailService will fail with NoSuchKeyException.
        // The controller should return the "image not available" placeholder.
        Mockito.when(cameraImageThumbnailServiceMock.generateCameraImageThumbnailAsync(eq(imageName), isNull()))
                .thenReturn(CompletableFuture.failedFuture(
                        NoSuchKeyException.builder().message("The specified key does not exist.").build()));

        final MockHttpServletResponse response = requestThumbnail(imageName);
        assertImageNotAvailableResponse(response);
    }

    @Test
    public void getThumbnailOfPublicCurrentImageReturnsThumbnail() throws Exception {
        final byte[] fakeThumbnailBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, 0x01, 0x02, 0x03};

        Mockito.when(cameraImageThumbnailServiceMock.generateCameraImageThumbnailAsync(eq(imageName), isNull()))
                .thenReturn(CompletableFuture.completedFuture(fakeThumbnailBytes));

        final MockHttpServletResponse response = requestThumbnail(imageName);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.IMAGE_JPEG_VALUE, response.getContentType());
        assertArrayEquals(fakeThumbnailBytes, response.getContentAsByteArray());
    }

    private void assertResponse(final MockHttpServletResponse response, final HttpStatus httpStatus,
                                final String redirectUrl) {
        log.info("HTTP response: {} and redirect: {}", response.getStatus(), response.getRedirectedUrl());
        assertEquals(httpStatus.value(), response.getStatus());
        assertEquals(redirectUrl, response.getRedirectedUrl());
    }

    private void assertImageNotAvailableResponse(final MockHttpServletResponse response) throws IOException {
        log.info("HTTP response: {} contentType: {} contentLength: {}",
                response.getStatus(), response.getContentType(), response.getContentLength());
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.IMAGE_JPEG_VALUE, response.getContentType());

        // Verify Cache-Control: no-cache header
        final String cacheControl = response.getHeader(HttpHeaders.CACHE_CONTROL);
        assertNotNull(cacheControl, "Cache-Control header should be present");
        assertTrue(cacheControl.contains("no-cache"), "Cache-Control should contain no-cache, was: " + cacheControl);

        // Verify the response body matches the static placeholder image
        final byte[] expectedImage = readImageNotAvailableResource();
        assertArrayEquals(expectedImage, response.getContentAsByteArray(),
                "Response body should be the image_not_available.jpg placeholder");
    }

    private byte[] readImageNotAvailableResource() throws IOException {
        final Resource resource = resourceLoader.getResource("classpath:img/image_not_available.jpg");
        try (final InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        }
    }

    private String getVersionedRedirectUrl(final String imageName, final String versionId) {
        return weathercamS3Properties.getS3UriForVersion(imageName, versionId).toString();
    }

    private CameraPresetHistory createHistory(final String imageName, final String versionId, final boolean publishable,
                                              final Instant lastModified) {
        return new CameraPresetHistory(getPresetId(imageName), versionId, -1, lastModified, publishable, 10, true);
    }

    private MockHttpServletResponse requestImage(final String imageName, final String versionId) throws Exception {
        final URI uri = URI.create(
                WeathercamPermissionControllerV1.WEATHERCAM_PATH + "/" + imageName + "?versionId=" + versionId);
        log.info("Request uri: {}", uri);

        final MvcResult mvcResult = mockMvc.perform(get(uri))
                .andReturn();
        return mockMvc.perform(asyncDispatch(mvcResult))
                .andReturn().getResponse();
    }

    private MockHttpServletResponse requestThumbnail(final String imageName) throws Exception {
        final URI uri = URI.create(
                WeathercamPermissionControllerV1.WEATHERCAM_PATH + "/" + imageName + "?thumbnail=true");
        log.info("Request uri: {}", uri);

        final MvcResult mvcResult = mockMvc.perform(get(uri))
                .andReturn();
        return mockMvc.perform(asyncDispatch(mvcResult))
                .andReturn().getResponse();
    }

    private static String getPresetId(final String imageName) {
        return imageName.substring(0, 8);
    }
}
