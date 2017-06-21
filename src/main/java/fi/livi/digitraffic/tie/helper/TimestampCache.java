package fi.livi.digitraffic.tie.helper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TimestampCache {
    private final Map<Long, Timestamp> cache = new HashMap<>();

    public Timestamp get(final Long millis) {
        Timestamp ts = cache.get(millis);

        if(ts == null) {
            final LocalDateTime ldt = DateHelper.toLocalDateTime(millis);
            ts = Timestamp.valueOf(ldt);

            cache.put(millis, ts);
        }

        return ts;
    }
}
