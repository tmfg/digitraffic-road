package fi.livi.digitraffic.tie.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtils {

    private GZipUtils() {
        throw new IllegalStateException("Util class initialization is not allowed");
    }

    public static byte[] compress(final String input) {
        final byte[] originalBytes = input.getBytes(StandardCharsets.UTF_8);
        return compress(originalBytes);
    }

    public static String compressToBase64String(final String input) {
        return toBase64String(compress(input));
    }

    public static String decompressBase64String(final String base64String) {
        final byte[] t = decompress(fromBase64String(base64String));
        return new String(t, StandardCharsets.UTF_8);
    }

    public static byte[] compress(final byte[] originalBytes) {
        try (final ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
             final GZIPOutputStream gzipOs = new GZIPOutputStream(byteArrayOs)) {
            gzipOs.write(originalBytes);
            gzipOs.close();
            byteArrayOs.close();
            return byteArrayOs.toByteArray();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static byte[] decompress(final byte[] originalBytes) {
        try (final ByteArrayInputStream byteArrayIs = new ByteArrayInputStream(originalBytes);
             final GZIPInputStream gzipIs = new GZIPInputStream(byteArrayIs)){
            return gzipIs.readAllBytes();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static String toBase64String(final byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] fromBase64String(final String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

}
