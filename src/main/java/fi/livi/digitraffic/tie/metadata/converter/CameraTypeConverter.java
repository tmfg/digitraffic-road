package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.metadata.model.CameraType;

@Converter
public class CameraTypeConverter implements AttributeConverter<CameraType, String> {

    @Override
    public String convertToDatabaseColumn(final CameraType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public CameraType convertToEntityAttribute(final String type) {
        return type != null ? CameraType.valueOf(type) : null;
    }
}
