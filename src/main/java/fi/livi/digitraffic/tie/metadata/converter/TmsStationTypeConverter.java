package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.metadata.model.TmsStationType;

@Converter
public class TmsStationTypeConverter implements AttributeConverter<TmsStationType, String> {

    @Override
    public String convertToDatabaseColumn(final TmsStationType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public TmsStationType convertToEntityAttribute(final String type) {
        return type != null ? TmsStationType.valueOf(type) : null;
    }
}
