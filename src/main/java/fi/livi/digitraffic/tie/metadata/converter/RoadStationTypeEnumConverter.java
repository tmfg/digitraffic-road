package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Converter
public class RoadStationTypeEnumConverter implements AttributeConverter<RoadStationType, String> {

    @Override
    public String convertToDatabaseColumn(final RoadStationType roadStationType) {
        return roadStationType != null ? roadStationType.name() : null;
    }

    @Override
    public RoadStationType convertToEntityAttribute(final String type) {
        return type != null ? RoadStationType.valueOf(type) : null;
    }
}
