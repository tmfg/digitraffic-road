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

    private static final String LOTJU_ID = "lotjuId";
    public static final String VANHA_ID = "vanhaId";
    public static final String NIMI = "nimi";
    public static final String NAME = "name";
    public static final String NATURAL_ID = "naturalId";
    public static final String ID = "id";
    private final StringBuffer sb;
    private boolean toStringCalled;

    public ToStringHelpper(final Object object) {
        sb = createStartSb(object);
    }

    public static String toString(final LamAsema la) {
        final StringBuffer sb = createStartSb(la);
        JSON_STYLE.append(sb, LOTJU_ID, la.getId());
        JSON_STYLE.append(sb, VANHA_ID, la.getVanhaId());
        JSON_STYLE.append(sb, NIMI, la.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(final Kamera kamera) {
        final StringBuffer sb = createStartSb(kamera);
        JSON_STYLE.append(sb, LOTJU_ID, kamera.getId());
        JSON_STYLE.append(sb, VANHA_ID, kamera.getVanhaId());
        JSON_STYLE.append(sb, NIMI, kamera.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(final TiesaaAsema tsa) {
        final StringBuffer sb = createStartSb(tsa);
        JSON_STYLE.append(sb, LOTJU_ID, tsa.getId());
        JSON_STYLE.append(sb, VANHA_ID, tsa.getVanhaId());
        JSON_STYLE.append(sb, NIMI, tsa.getNimi(), true);
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
        JSON_STYLE.append(sb, ID, lamStation.getId());
        JSON_STYLE.append(sb, LOTJU_ID, lamStation.getLotjuId());
        JSON_STYLE.append(sb, NATURAL_ID, lamStation.getNaturalId());
        JSON_STYLE.append(sb, NAME, lamStation.getName(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }
}
