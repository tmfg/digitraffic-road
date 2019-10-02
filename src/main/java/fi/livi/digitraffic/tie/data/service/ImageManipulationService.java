package fi.livi.digitraffic.tie.data.service;

import java.io.ByteArrayOutputStream;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;

@ConditionalOnNotWebApplication
public class ImageManipulationService {

    private static final ExifRewriter exifRewriter = new ExifRewriter();

    /**
     * Removes JPG image Exif metadata losslessly
     * @param in image file contents
     * @return image file contents without Exif metadata
     *
     * @throws ImagingException when removing Exif metadata fails
     */
    public static byte[] removeJpgExifMetadata(final byte[] in) throws ImagingException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            exifRewriter.removeExifMetadata(in, os);
        } catch (final ImagingException ie) {
            throw ie;
        } catch (Exception e) {
            throw new ImagingException("Failed to remove Exif metadata from image", e);
        }
        return os.toByteArray();
    }
}