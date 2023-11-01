package fi.livi.digitraffic.tie.service.weathercam;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.conf.properties.LotjuMetadataProperties;
import fi.livi.digitraffic.tie.service.lotju.HostWithHealthCheck;
import fi.livi.digitraffic.tie.service.lotju.MultiDestinationProvider;

@Component
@ConditionalOnNotWebApplication
public class CameraImageReader {

    private final int connectTimeout;
    private final int readTimeout;
    private final MultiDestinationProvider destinationProvider;

    CameraImageReader(
        @Value("${camera-image-uploader.http.connectTimeout}")
        final int connectTimeout,
        @Value("${camera-image-uploader.http.readTimeout}")
        final int readTimeout,
        final LotjuMetadataProperties lotjuMetadataProperties) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        destinationProvider = new MultiDestinationProvider(HostWithHealthCheck.createHostsWithHealthCheck(lotjuMetadataProperties, lotjuMetadataProperties.getPath().image));
    }

    public byte[] readImage(final long kuvaId, final ImageUpdateInfo info) throws IOException {
        final StopWatch start = StopWatch.createStarted();
        final URI destination = destinationProvider.getDestination();
        final String imageDownloadUrl = getCameraDownloadUrl(destination, kuvaId);
        info.setDownloadUrl(imageDownloadUrl);

        try {
            final URL url = new URL(imageDownloadUrl);
            final URLConnection con = url.openConnection();
            con.setConnectTimeout(connectTimeout);
            con.setReadTimeout(readTimeout);
            try (final InputStream is = con.getInputStream()) {
                final byte[] data = IOUtils.toByteArray(is);
                info.setSizeBytes(data.length);
                info.updateReadStatusSuccess();
                info.setReadDurationMs(start.getTime());
                return data;
            }
        } catch (final Exception e) {
            destinationProvider.setHostNotHealthy(destination);
            throw e;
        }
    }

    private String getCameraDownloadUrl(final URI destination, final long kuvaId) {
        return StringUtils.appendIfMissing(destination.toString(), "/") + kuvaId;
    }
}
