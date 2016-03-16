package fi.livi.digitraffic.tie.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.model.CameraType;

@Converter
public class CameraTypeConverter implements AttributeConverter<CameraType, String> {

    @Override
    public String convertToDatabaseColumn(CameraType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public CameraType convertToEntityAttribute(String type) {
        return type != null ? CameraType.valueOf(type) : null;
    }
}
