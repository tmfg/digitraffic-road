package fi.livi.digitraffic.tie.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.model.RoadWeatherStationType;

@Converter
public class RoadWeatherStationTypeConverter implements AttributeConverter<RoadWeatherStationType, String> {

    @Override
    public String convertToDatabaseColumn(final RoadWeatherStationType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public RoadWeatherStationType convertToEntityAttribute(final String type) {
        return type != null ? RoadWeatherStationType.valueOf(type) : null;
    }
}
