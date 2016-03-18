package fi.livi.digitraffic.tie.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.model.LamStationType;

@Converter
public class LamStationTypeConverter implements AttributeConverter<LamStationType, String> {

    @Override
    public String convertToDatabaseColumn(LamStationType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public LamStationType convertToEntityAttribute(String type) {
        return type != null ? LamStationType.valueOf(type) : null;
    }
}
