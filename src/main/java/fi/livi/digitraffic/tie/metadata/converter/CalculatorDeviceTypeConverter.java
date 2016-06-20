package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.metadata.model.CalculatorDeviceType;

@Converter
public class CalculatorDeviceTypeConverter implements AttributeConverter<CalculatorDeviceType, String> {

    @Override
    public String convertToDatabaseColumn(final CalculatorDeviceType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public CalculatorDeviceType convertToEntityAttribute(final String type) {
        return type != null ? CalculatorDeviceType.valueOf(type) : null;
    }
}
