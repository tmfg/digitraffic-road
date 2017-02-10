package fi.livi.digitraffic.tie.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;

public class CameraHelper {

    private static final Pattern cameraPresetIdPattern = Pattern.compile("^C[0-9]{7}$");

    private CameraHelper() {}

    public static String resolvePresetId(final Kuva kuva) {
        String presetId = kuva.getNimi().substring(0, 8);
        System.out.println(presetId);
        return presetId;
    }

    public static String convertVanhaIdToKameraId(final Integer vanhaId) {
        final String vanha = vanhaId.toString();
        return StringUtils.leftPad(vanha, 6, "C00000");
    }

    public static String convertCameraIdToPresetId(final String cameraId, final String suunta) {
        return cameraId + StringUtils.leftPad(suunta, 2, "00");
    }

    public static String convertPresetIdToCameraId(final String presetId) {
        return presetId.substring(0, 6);
    }

    public static long convertPresetIdToVanhaId(final String presetId) {
        String cameraId = convertPresetIdToCameraId(presetId);
        cameraId = StringUtils.removeStart(cameraId, "C0");
        return Long.parseLong(StringUtils.removeStart(cameraId, "C"));
    }

    public static boolean validatePresetId(String presetId) {
        Matcher m = cameraPresetIdPattern.matcher(presetId);
        return m.matches();
    }
}
