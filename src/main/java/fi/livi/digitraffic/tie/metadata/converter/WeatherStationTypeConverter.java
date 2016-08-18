package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.metadata.model.WeatherStationType;

@Converter
public class WeatherStationTypeConverter implements AttributeConverter<WeatherStationType, String> {

    @Override
    public String convertToDatabaseColumn(final WeatherStationType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public WeatherStationType convertToEntityAttribute(final String type) {
        return type != null ? WeatherStationType.valueOf(type) : null;
    }
}
