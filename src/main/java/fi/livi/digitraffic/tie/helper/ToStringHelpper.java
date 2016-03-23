package fi.livi.digitraffic.tie.helper;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Provides helpper functions to stringify objects for logging
 */
public class ToStringHelpper {

    private final StringBuffer sb;
    private boolean toStringCalled;

    public static String toString(LamAsema la) {
        StringBuffer sb = createStartSb(la);
        JSON_STYLE.append(sb, "lotjuId", la.getId());
        JSON_STYLE.append(sb, "vanhaId", la.getVanhaId());
        JSON_STYLE.append(sb, "nimi", la.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(Kamera kamera) {
        StringBuffer sb = createStartSb(kamera);
        JSON_STYLE.append(sb, "lotjuId", kamera.getId());
        JSON_STYLE.append(sb, "vanhaId", kamera.getVanhaId());
        JSON_STYLE.append(sb, "nimi", kamera.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(TiesaaAsema tsa) {
        StringBuffer sb = createStartSb(tsa);
        JSON_STYLE.append(sb, "lotjuId", tsa.getId());
        JSON_STYLE.append(sb, "vanhaId", tsa.getVanhaId());
        JSON_STYLE.append(sb, "nimi", tsa.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toStringFull(Object object) {
        return object.getClass().getSimpleName() + ": " + ToStringBuilder.reflectionToString(object, JSON_STYLE);
    }

    private static StringBuffer createStartSb(Object object) {
        return new StringBuffer(object.getClass().getSimpleName() + ": {");
    }

    private static void removeLastFieldSeparatorFromEnd(StringBuffer sb) {
        if (sb.length() > 0 && sb.lastIndexOf(",") == sb.length()-1) {
            sb.setLength(sb.length()-1); // remove last char","
        }
    }

    public ToStringHelpper(Object object) {
        sb = createStartSb(object);
    }

    public ToStringHelpper appendField(String fieldName, Object value) {
        JSON_STYLE.append(sb, fieldName, value, true);
        return this;
    }

    @Override
    public String toString() {
        if (!toStringCalled) {
            removeLastFieldSeparatorFromEnd(sb);
            sb.append("}");
            toStringCalled = true;
        }
        return sb.toString();
    }
}
