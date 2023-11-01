package fi.livi.digitraffic.tie.converter;

import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoadStationTypeIntegerConverter implements AttributeConverter<RoadStationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final RoadStationType attribute) {
        return attribute != null ? attribute.getTypeNumber() : null;
    }

    @Override
    public RoadStationType convertToEntityAttribute(final Integer typeNumber) {
        return typeNumber != null ? RoadStationType.fromTypeNumber(typeNumber) : null;
    }
}
