package fi.livi.digitraffic.tie.dto.trafficmessage.v2;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

/**
 * Date-time format constants and Jackson serializer/deserializer for V2 traffic message API.
 * Always serializes Instant as "yyyy-MM-dd'T'HH:mm:ss.SSSZ" (3 digit millis, Z suffix).
 * Deserializes any ISO 8601 instant with or without fractional seconds.
 */
public final class V2DateTimeFormat {

    private static final DateTimeFormatter SERIALIZER_FORMAT = new DateTimeFormatterBuilder()
            .appendInstant(3)
            .toFormatter();

    private static final DateTimeFormatter DESERIALIZER_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .appendLiteral('Z')
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    private V2DateTimeFormat() {
    }

    public static class Serializer extends ValueSerializer<Instant> {
        @Override
        public void serialize(final Instant value, final JsonGenerator gen, final SerializationContext ctxt) {
            gen.writeString(SERIALIZER_FORMAT.format(value));
        }
    }

    public static class Deserializer extends ValueDeserializer<Instant> {
        @Override
        public Instant deserialize(final JsonParser p, final DeserializationContext ctxt) {
            return DESERIALIZER_FORMAT.parse(p.getString(), Instant::from);
        }
    }
}

