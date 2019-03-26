package fi.livi.digitraffic.tie.helper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class TimestampCache {
    private final Map<Long, OffsetDateTime> cache = new HashMap<>();

    public OffsetDateTime get(final Long millis) {
        OffsetDateTime ts = cache.get(millis);

        if(ts == null) {
            final Instant instant = Instant.ofEpochMilli(millis);
            ts = instant.atOffset(ZoneOffset.UTC);
            cache.put(millis, ts);
        }

        return ts;
    }
}
