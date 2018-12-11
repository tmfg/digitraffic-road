package fi.livi.digitraffic.tie.helper;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

import java.lang.reflect.Field;
import java.time.Instant;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.ely.lotju.tiesaa.proto.TiesaaProtos;

import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._03._12.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaAsemaVO;

/**
 * Provides helper functions to stringify objects for logging
 */
public class ToStringHelper {
    private static final String LOTJU_ID = "lotjuId";
    private static final String VANHA_ID = "vanhaId";
    private static final String NIMI = "nimi";
    private static final String NAME = "name";
    private static final String NATURAL_ID = "naturalId";
    private static final String ID = "id";
    private final StringBuffer sb;
    private boolean toStringCalled;

    public static final String ISO_8601_OFFSET_TIMESTAMP_EXAMPLE = "timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00 or 2018-11-09T09:41:09Z)";

    public ToStringHelper(final Object object) {
        sb = createStartSb(object);
    }

    public static String toString(final LamAsemaVO la) {
        final StringBuffer sb = createStartSb(la);
        JSON_STYLE.append(sb, LOTJU_ID, la.getId());
        JSON_STYLE.append(sb, VANHA_ID, la.getVanhaId(), true);
        JSON_STYLE.append(sb, NIMI, la.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(final KameraVO kamera) {
        final StringBuffer sb = createStartSb(kamera);
        JSON_STYLE.append(sb, LOTJU_ID, kamera.getId());
        JSON_STYLE.append(sb, VANHA_ID, kamera.getVanhaId(), true);
        JSON_STYLE.append(sb, NIMI, kamera.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(final EsiasentoVO esiasento) {
        final StringBuffer sb = createStartSb(esiasento);
        JSON_STYLE.append(sb, LOTJU_ID, esiasento.getId());
        JSON_STYLE.append(sb, "kameraId", esiasento.getKameraId(), true);
        JSON_STYLE.append(sb, "suunta", esiasento.getSuunta(), true);
        JSON_STYLE.append(sb, "nimiEsitys", esiasento.getNimiEsitys(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(final TiesaaAsemaVO tsa) {
        final StringBuffer sb = createStartSb(tsa);
        JSON_STYLE.append(sb, LOTJU_ID, tsa.getId());
        JSON_STYLE.append(sb, VANHA_ID, tsa.getVanhaId(), true);
        JSON_STYLE.append(sb, NIMI, tsa.getNimi(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toStringFull(final Object object, final String...secretFields) {
        final ReflectionToStringBuilder refBuiler = new ReflectionToStringBuilder(object, JSON_STYLE) {
            @Override
            protected Object getValue(Field field) throws IllegalAccessException {
                for (String excludeFieldName : secretFields) {
                    if (field.getName().equals(excludeFieldName)) {
                        return "*****";
                    }
                }
                return super.getValue(field);
            }
        };
        return object.getClass().getSimpleName() + ": " + refBuiler;
    }

    private static StringBuffer createStartSb(final Object object) {
        return new StringBuffer(object.getClass().getSimpleName() + ": {");
    }

    private static void removeLastFieldSeparatorFromEnd(final StringBuffer sb) {
        if (sb.length() > 0 && sb.lastIndexOf(",") == (sb.length() - 1)) {
            sb.setLength(sb.length()-1); // remove last char","
        }
    }

    public ToStringHelper appendField(final String fieldName, final Object value) {
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

    public static String toString(final TmsStation tmsStation) {
        final StringBuffer sb = createStartSb(tmsStation);
        JSON_STYLE.append(sb, ID, tmsStation.getId());
        JSON_STYLE.append(sb, LOTJU_ID, tmsStation.getLotjuId());
        JSON_STYLE.append(sb, NATURAL_ID, tmsStation.getNaturalId());
        JSON_STYLE.append(sb, NAME, tmsStation.getName(), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String toString(final TiesaaProtos.TiesaaMittatieto tiesaa) {
        final StringBuffer sb = createStartSb(tiesaa);

        JSON_STYLE.append(sb, "asemaId", tiesaa.getAsemaId());

        if (tiesaa.getAika() != 0) {
            JSON_STYLE.append(sb, "aika", Instant.ofEpochMilli(tiesaa.getAika()), true);
        } else {
            JSON_STYLE.append(sb, "aika", "null", true);
        }

        sb.append("anturit: [");

        boolean first = true;

        for (TiesaaProtos.TiesaaMittatieto.Anturi anturi : tiesaa.getAnturiList()) {
            if (!first) {
                sb.append(", ");
            }

            sb.append("{");
            JSON_STYLE.append(sb, "laskennallinenAnturiId", anturi.getLaskennallinenAnturiId(), true);
            JSON_STYLE.append(sb, "arvo", NumberConverter.convertAnturiValueToDouble(anturi.getArvo()));
            sb.append("}");
            first = false;
        }

        sb.append("]");

        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");

        return sb.toString();
    }

    public static String toString(final LAMRealtimeProtos.Lam lam) {
        final StringBuffer sb = createStartSb(lam);
        JSON_STYLE.append(sb, "asemaId", lam.getAsemaId());

        if (lam.getAika() != 0) {
            JSON_STYLE.append(sb, "aika", Instant.ofEpochMilli(lam.getAika()), true);
        } else {
            JSON_STYLE.append(sb, "aika", "null", true);
        }

        sb.append("anturit: [");

        boolean first = true;
        for (final LAMRealtimeProtos.Lam.Anturi anturi : lam.getAnturiList()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("{");
            JSON_STYLE.append(sb, "laskennallinenAnturiId", anturi.getLaskennallinenAnturiId(), true);
            JSON_STYLE.append(sb, "arvo", anturi.getArvo());
            sb.append("}");
            first = false;
        }

        sb.append("]");

        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");

        return sb.toString();
    }

    public static String toString(final KuvaProtos.Kuva kuva) {
        final StringBuffer sb = createStartSb(kuva);

        JSON_STYLE.append(sb, "kuvaId", kuva.getKuvaId(), true);
        JSON_STYLE.append(sb, "nimi", kuva.getNimi(), true);
        JSON_STYLE.append(sb, "asemanNimi", kuva.getAsemanNimi(), true);
        JSON_STYLE.append(sb, "esiasennonNimi", kuva.getEsiasennonNimi(), true);
        JSON_STYLE.append(sb, "esiasentoId", kuva.getEsiasentoId());
        JSON_STYLE.append(sb, "kameraId", kuva.getKameraId());
        JSON_STYLE.append(sb, "aika", kuva.getAikaleima(), true);
        JSON_STYLE.append(sb, "tienumero", kuva.getTienumero(), true);
        JSON_STYLE.append(sb, "tieosa", kuva.getTieosa(), true);

        JSON_STYLE.append(sb, "julkinen", kuva.getJulkinen());
        //JSON_STYLE.append(sb, "url", CameraHelper.createCameraUrl(kuva), true);
        removeLastFieldSeparatorFromEnd(sb);
        sb.append("}");
        return sb.toString();
    }

    public static String nullSafeToString(final Object o) {
        return o != null ? o.toString() : null;
    }

    public enum TimestampFormat {
        ISO_8601_UTC,
        ISO_8601_WITH_ZONE_OFFSET
    }
}
