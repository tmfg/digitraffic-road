package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.metadata.model.RoadStationState;

@Converter
public class RoadStationStateConverter implements AttributeConverter<RoadStationState, String> {

    @Override
    public String convertToDatabaseColumn(final RoadStationState type) {
        return type != null ? type.name() : null;
    }

    @Override
    public RoadStationState convertToEntityAttribute(final String type) {
        return type != null ? RoadStationState.valueOf(type) : null;
    }
}
