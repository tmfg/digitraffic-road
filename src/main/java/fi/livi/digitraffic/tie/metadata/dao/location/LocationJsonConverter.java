package fi.livi.digitraffic.tie.metadata.dao.location;

import java.math.BigDecimal;

import fi.livi.digitraffic.tie.metadata.dto.LocationJsonObject;

public final class LocationJsonConverter {
    private LocationJsonConverter() {
    }

    public static LocationJsonObject convert(final Object o[]) {
        return new LocationJsonObject(i(o[0]), (String)o[1], (String)o[2], (String)o[3], (String)o[4], (String)o[5], i(o[6]), i(o[7]), i(o[8]), i(o[9]), b(o[10]), (BigDecimal)o[11], (BigDecimal)o[12], (String)o[13], (String)o[14]);
    }

    private static Boolean b(final Object o) {
        return o == null ? null : ((BigDecimal)o).intValue() == 1;
    }

    private static Integer i(final Object o) {
        return o == null ? null : ((BigDecimal)o).intValue();
    }
}
