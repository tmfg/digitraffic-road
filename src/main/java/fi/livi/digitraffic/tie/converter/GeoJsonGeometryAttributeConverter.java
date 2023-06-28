package fi.livi.digitraffic.tie.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

@Converter(autoApply = true)
public class GeoJsonGeometryAttributeConverter implements AttributeConverter<Geometry<?>, String> {
    private static final Logger log = LoggerFactory.getLogger(GeoJsonGeometryAttributeConverter.class);

    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;

    public GeoJsonGeometryAttributeConverter(final ObjectMapper objectMapper) {
        this.jsonWriter = objectMapper.writerFor(Geometry.class);
        this.jsonReader = objectMapper.readerFor(Geometry.class);
    }

    @Override
    public String convertToDatabaseColumn(final Geometry<?> geometry) {
        if (geometry == null) {
            return null;
        }
        try {
            return jsonWriter.writeValueAsString(geometry);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert geometry to json: {}", ToStringHelper.toStringFull(geometry));
            throw new RuntimeException(e);
        }
    }

    @Override
    public Geometry<?> convertToEntityAttribute(final String json) {
        if (json == null) {
            return null;
        }
        try {
            return jsonReader.readValue(json);
        } catch (final JsonProcessingException e) {
            log.error("Failed to convert json to geometry: {}", json);
            throw new RuntimeException(e);
        }

    }
}
