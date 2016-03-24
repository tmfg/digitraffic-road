package fi.livi.digitraffic.tie.geojson.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import fi.livi.digitraffic.tie.geojson.LngLatAlt;

public class LngLatAltDeserializer extends JsonDeserializer<LngLatAlt> {
	@Override
	public LngLatAlt deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
		if (jp.isExpectedStartArrayToken()) {
			return deserializeArray(jp, ctxt);
		}
		throw ctxt.mappingException(LngLatAlt.class);
	}

	protected LngLatAlt deserializeArray(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
		final LngLatAlt node = new LngLatAlt();
		node.setLongitude(extractDouble(jp, ctxt, false));
		node.setLatitude(extractDouble(jp, ctxt, false));
		node.setAltitude(extractDouble(jp, ctxt, true));
		if (jp.hasCurrentToken() && jp.getCurrentToken() != JsonToken.END_ARRAY)
			jp.nextToken();
		return node;
	}

	private double extractDouble(final JsonParser jp, final DeserializationContext ctxt, final boolean optional)
			throws IOException {
		final JsonToken token = jp.nextToken();
		if (token == null) {
			if (optional)
				return Double.NaN;
			else
				throw ctxt.mappingException("Unexpected end-of-input when binding data into LngLatAlt");
		}
		else {
			switch (token) {
				case END_ARRAY:
					if (optional)
						return Double.NaN;
					else
						throw ctxt.mappingException("Unexpected end-of-input when binding data into LngLatAlt");
				case VALUE_NUMBER_FLOAT:
					return jp.getDoubleValue();
				case VALUE_NUMBER_INT:
					return jp.getLongValue();
				default:
					throw ctxt.mappingException("Unexpected token (" + token.name()
							+ ") when binding data into LngLatAlt ");
			}
		}
	}
}
