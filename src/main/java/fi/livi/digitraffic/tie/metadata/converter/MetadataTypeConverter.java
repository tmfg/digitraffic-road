package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;

@Converter
public class MetadataTypeConverter implements AttributeConverter<MetadataType, String> {

    @Override
    public String convertToDatabaseColumn(final MetadataType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public MetadataType convertToEntityAttribute(final String type) {
        return type != null ? MetadataType.valueOf(type) : null;
    }
}
