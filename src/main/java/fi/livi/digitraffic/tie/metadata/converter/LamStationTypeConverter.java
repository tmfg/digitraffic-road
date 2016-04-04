package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.metadata.model.LamStationType;

@Converter
public class LamStationTypeConverter implements AttributeConverter<LamStationType, String> {

    @Override
    public String convertToDatabaseColumn(final LamStationType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public LamStationType convertToEntityAttribute(final String type) {
        return type != null ? LamStationType.valueOf(type) : null;
    }
}
