package fi.livi.digitraffic.tie.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fi.ely.lotju.kamera.proto.KuvaProtos;

public class CameraHelper {

    private static final Pattern cameraPresetIdPattern = Pattern.compile("^C[0-9]{7}$");

    private CameraHelper() {}

    public static String convertVanhaIdToKameraId(final Integer vanhaId) {
        return StringUtils.leftPad(vanhaId.toString(), 6, "C00000");
    }

    public static String convertNaturalIdToCameraId(final Long naturalId) {
        return convertVanhaIdToKameraId(naturalId.intValue());
    }

    public static String convertCameraIdToPresetId(final String cameraId, final String suunta) {
        return cameraId + leftPadDirection(suunta);
    }

    public static String leftPadDirection(final String direction) {
        return StringUtils.leftPad(direction, 2, "00");
    }

    public static String convertPresetIdToCameraId(final String presetId) {
        return presetId.substring(0, 6);
    }

    public static long convertPresetIdToVanhaId(final String presetId) {
        final String cameraId = convertPresetIdToCameraId(presetId);
        return Long.parseLong(StringUtils.removeStart(cameraId, "C"));
    }

    public static boolean validatePresetId(final String presetId) {
        Matcher m = cameraPresetIdPattern.matcher(presetId);
        return m.matches();
    }

    public static String getDirectionFromPresetId(final String presetId) {
        return StringUtils.substring(presetId, -2);
    }

    public static String resolvePresetId(final KuvaProtos.Kuva kuva) {
        return kuva.getNimi().substring(0, 8);
    }
}
