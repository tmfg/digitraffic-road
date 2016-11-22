package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.data.model.Datex2SituationRecordType;

@Converter
public class Datex2SituationRecordTypeConverter implements AttributeConverter<Datex2SituationRecordType, String> {

    @Override
    public String convertToDatabaseColumn(final Datex2SituationRecordType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public Datex2SituationRecordType convertToEntityAttribute(final String type) {
        return type != null ? Datex2SituationRecordType.valueOf(type) : null;
    }
}
