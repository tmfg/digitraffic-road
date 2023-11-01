package fi.livi.digitraffic.tie.metadata.geojson.converter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Copy of {@link com.fasterxml.jackson.databind.ser.impl.IndexedListSerializer}
 */
public final class CoordinatesDecimalConverter
    extends JsonSerializer<List<?>> {

    private static final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.ROOT);

    @Override
    public void serialize(final List<?> value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        gen.writeStartArray(value);
        this.serializeContents(value, gen, provider);
        gen.writeEndArray();
    }

    private void serializeContents(final List<?> value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
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

    synchronized private void serializeDouble(final Double value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        gen.writeObject(BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP));
    }

    private DecimalFormat getNewDecimalFormat() {
        final DecimalFormat df = new DecimalFormat("0.000000", dfs);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df;
    }

    @Override
    public boolean isEmpty(final SerializerProvider provider, final List<?> value) {
        return value.isEmpty();
    }
}
