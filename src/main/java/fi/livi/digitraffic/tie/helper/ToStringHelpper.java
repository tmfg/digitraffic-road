package fi.livi.digitraffic.tie.helper;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.ToStringBuilder;

import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.TiesaaAsemaVO;

/**
 * Provides helpper functions to stringify objects for logging
 */
public class ToStringHelpper {

    private static final String LOTJU_ID = "lotjuId";
    private static final String VANHA_ID = "vanhaId";
    private static final String NIMI = "nimi";
    private static final String NAME = "name";
    private static final String NATURAL_ID = "naturalId";
    private static final String ID = "id";
    private final StringBuffer sb;
    private boolean toStringCalled;

    public static final String ISO_8601_UTC_TIMESTAMP_EXAMPLE = "timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)";
    public static final String ISO_8601_OFFSET_TIMESTAMP_EXAMPLE = "timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)";

    public ToStringHelpper(final Object object) {
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

    public static String toString(final TiesaaAsemaVO tsa) {
        final StringBuffer sb = createStartSb(tsa);
        JSON_STYLE.append(sb, LOTJU_ID, tsa.getId());
        JSON_STYLE.append(sb, VANHA_ID, tsa.getVanhaId(), true);
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
        if (sb.length() > 0 && sb.lastIndexOf(",") == (sb.length() - 1)) {
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

    public enum TimestampFormat {
        ISO_8601_UTC,
        ISO_8601_WITH_ZONE_OFFSET
    }

    public static String toString(final ZonedDateTime zonedDateTime, final TimestampFormat timestampFormat) {
        if (zonedDateTime == null) {
            return null;
        }
        if (TimestampFormat.ISO_8601_UTC == timestampFormat) {
            return ZonedDateTime.ofInstant(zonedDateTime.toInstant(), ZoneOffset.UTC).toString();
        } else if (TimestampFormat.ISO_8601_WITH_ZONE_OFFSET == timestampFormat) {
            return zonedDateTime.toOffsetDateTime().toString();
        }
        throw new NotImplementedException("ToString for " + ZonedDateTime.class.getSimpleName() +
                " for " + timestampFormat + " not implemented");
    }

    public static String toString(final LocalDateTime localDateTime, final TimestampFormat timestampFormat) {
        if (localDateTime == null) {
            return null;
        }
        final ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        if (TimestampFormat.ISO_8601_UTC == timestampFormat) {
            return ZonedDateTime.ofInstant(zonedDateTime.toInstant(), ZoneOffset.UTC).toString();
        } else if (TimestampFormat.ISO_8601_WITH_ZONE_OFFSET == timestampFormat) {
            return zonedDateTime.toOffsetDateTime().toString();
        }
        throw new NotImplementedException("ToString for " + localDateTime.getClass().getSimpleName() +
                " for " + timestampFormat + " not implemented");
    }
}
