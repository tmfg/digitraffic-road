package fi.livi.digitraffic.tie.data.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;import org.springframework.core.io.Resource;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithouLocalStack;
import fi.livi.digitraffic.tie.service.ImageManipulationService;

public class ImageManipulationServiceTest extends AbstractDaemonTestWithouLocalStack {

    @Test
    public void imageExifRemovalSuccess() throws IOException, ImagingException {
        final byte[] imageWithExif = FileUtils.readFileToByteArray(loadResource("classpath:/lotju/kuva/exif.jpg").getFile());
        final byte[] imageWithoutExif = FileUtils.readFileToByteArray(loadResource("classpath:/lotju/kuva/noExif.jpg").getFile());

        final byte[] imageExifRemoved = ImageManipulationService.removeJpgExifMetadata(imageWithExif);

        assertThat(imageWithExif, not(equalTo(imageWithoutExif)));
        assertThat(imageWithExif, not(equalTo(imageExifRemoved)));
        assertArrayEquals(imageWithoutExif, imageExifRemoved);
    }

    @Test
    public void imageWithoutExifShouldStaySame() throws IOException, ImagingException {
        final Resource imgResource = loadResource("classpath:/lotju/kuva/noExif.jpg");
        final byte[] withExif = FileUtils.readFileToByteArray(imgResource.getFile());
        final byte[] withoutExif = ImageManipulationService.removeJpgExifMetadata(withExif);
        assertArrayEquals(withExif, withoutExif);
    }
}
