package fi.livi.digitraffic.tie.geojson.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fi.livi.digitraffic.tie.geojson.LngLatAlt;

public class LngLatAltSerializer extends JsonSerializer<LngLatAlt> {
	private static final long[] POW10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

	/**
	 * The following must convert double to String in a much more efficient way then Double.toString()
	 *
	 * @See http://stackoverflow.com/questions/10553710/fast-double-to-string-conversion-with-given-precision
	 * @param val
	 * @param precision
	 * @return
	 */
	private static String fastDoubleToString(double val, final int precision) {
		final StringBuilder sb = new StringBuilder();
		if (val < 0) {
			sb.append('-');
			val = -val;
		}
		final long exp = POW10[precision];
		final long lval = (long)(val * exp + 0.5);
		sb.append(lval / exp).append('.');
		final long fval = lval % exp;
		for (int p = precision - 1; p > 0 && fval < POW10[p] && fval>0; p--) {
			sb.append('0');
		}
		sb.append(fval);
		int i = sb.length()-1;
		while(sb.charAt(i)=='0' && sb.charAt(i-1)!='.')
		{
			sb.deleteCharAt(i);
			i--;
		}
		return sb.toString();
	}

	@Override
	public void serialize(final LngLatAlt value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
		jgen.writeStartArray();

		jgen.writeNumber(fastDoubleToString(value.getLongitude(), 9));
		jgen.writeNumber(fastDoubleToString(value.getLatitude(), 9));

		if (value.hasAltitude()) {
			jgen.writeNumber(value.getAltitude());
		}

		jgen.writeEndArray();
	}
}
