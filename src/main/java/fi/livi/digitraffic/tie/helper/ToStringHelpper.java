package fi.livi.digitraffic.tie.helper;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

import org.apache.commons.lang3.builder.ToStringBuilder;

import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;

/**
 * Provides helpper functions to stringify objects for logging
 */
public class ToStringHelpper {

    private final StringBuffer sb;
    private boolean toStringCalled;

    public static String toString(final LamAsema la) {
        final StringBuffer sb = createStartSb(la);
        JSON_STYLE.append(sb, "lotjuId", la.getId());
        JSON_STYLE.append(sb, "vanhaId", la.getVanhaId());
        JSON_STYLE.append(sb, "nimi", la.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(final Kamera kamera) {
        final StringBuffer sb = createStartSb(kamera);
        JSON_STYLE.append(sb, "lotjuId", kamera.getId());
        JSON_STYLE.append(sb, "vanhaId", kamera.getVanhaId());
        JSON_STYLE.append(sb, "nimi", kamera.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(final TiesaaAsema tsa) {
        final StringBuffer sb = createStartSb(tsa);
        JSON_STYLE.append(sb, "lotjuId", tsa.getId());
        JSON_STYLE.append(sb, "vanhaId", tsa.getVanhaId());
        JSON_STYLE.append(sb, "nimi", tsa.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toStringFull(final Object object) {
        return object.getClass().getSimpleName() + ": " + ToStringBuilder.reflectionToString(object, JSON_STYLE);
    }

    private static StringBuffer createStartSb(final Object object) {
        return new StringBuffer(object.getClass().getSimpleName() + ": {");
    }

    private static void removeLastFieldSeparatorFromEnd(final StringBuffer sb) {
        if (sb.length() > 0 && sb.lastIndexOf(",") == sb.length()-1) {
            sb.setLength(sb.length()-1); // remove last char","
        }
    }

    public ToStringHelpper(final Object object) {
        sb = createStartSb(object);
    }

    public ToStringHelpper appendField(final String fieldName, final Object value) {
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

    public static String toString(final LamStation lamStation) {
        final StringBuffer sb = createStartSb(lamStation);
        JSON_STYLE.append(sb, "id", lamStation.getId());
        JSON_STYLE.append(sb, "lotjuId", lamStation.getLotjuId());
        JSON_STYLE.append(sb, "naturalId", lamStation.getNaturalId());
        JSON_STYLE.append(sb, "name", lamStation.getName(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }
}
