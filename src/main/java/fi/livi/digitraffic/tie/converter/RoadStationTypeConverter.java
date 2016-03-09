package fi.livi.digitraffic.tie.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.model.RoadStationType;

@Converter
public class RoadStationTypeConverter implements AttributeConverter<RoadStationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RoadStationType attribute) {
        return attribute != null ? attribute.getTypeNumber() : null;
    }

    @Override
    public RoadStationType convertToEntityAttribute(Integer typeNumber) {
        return typeNumber != null ? RoadStationType.fromTypeNumber(typeNumber) : null;
    }
}
