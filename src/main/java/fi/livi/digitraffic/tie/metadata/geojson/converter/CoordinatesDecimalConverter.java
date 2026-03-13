package fi.livi.digitraffic.tie.metadata.geojson.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Copy of {@link tools.jackson.databind.ser.impl.IndexedListSerializer}
 */
public final class CoordinatesDecimalConverter
    extends ValueSerializer<List<?>> {

    private static final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.ROOT);

    @Override
    public void serialize(final List<?> value, final JsonGenerator gen, final SerializationContext provider) {
        gen.writeStartArray(value);
        this.serializeContents(value, gen, provider);
        gen.writeEndArray();
    }

    private void serializeContents(final List<?> value, final JsonGenerator gen, final SerializationContext provider) {
        if (isEmpty(provider, value)) {
            return;
        } else {
            for (final Object element : value) {
                if (element instanceof List) {
                    this.serialize((List<?>) element, gen, provider);
                } else {
                    serializeDouble((Double) element, gen, provider);
                }
            }
        }
    }

    synchronized private void serializeDouble(final Double value, final JsonGenerator gen, final SerializationContext provider) {
        gen.writePOJO(BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP));
    }

    private DecimalFormat getNewDecimalFormat() {
        final DecimalFormat df = new DecimalFormat("0.000000", dfs);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df;
    }

    @Override
    public boolean isEmpty(final SerializationContext provider, final List<?> value) {
        return value.isEmpty();
    }
}
