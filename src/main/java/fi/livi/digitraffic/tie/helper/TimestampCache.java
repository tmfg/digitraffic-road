package fi.livi.digitraffic.tie.helper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TimestampCache {
    private final Map<Long, Timestamp> cache = new HashMap<>();

    public Timestamp get(final Long millis) {
        Timestamp ts = cache.get(millis);

        if(ts == null) {
            final Instant instant = Instant.ofEpochMilli(millis);
            ts = Timestamp.from(instant);

            cache.put(millis, ts);
        }

        return ts;
    }
}
