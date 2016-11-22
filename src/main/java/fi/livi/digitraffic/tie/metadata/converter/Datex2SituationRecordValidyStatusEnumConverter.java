package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.data.model.Datex2SituationRecordValidyStatus;

@Converter
public class Datex2SituationRecordValidyStatusEnumConverter implements AttributeConverter<Datex2SituationRecordValidyStatus, String> {

    @Override
    public String convertToDatabaseColumn(final Datex2SituationRecordValidyStatus roadStationType) {
        return roadStationType != null ? roadStationType.name() : null;
    }

    @Override
    public Datex2SituationRecordValidyStatus convertToEntityAttribute(final String type) {
        return type != null ? Datex2SituationRecordValidyStatus.valueOf(type) : null;
    }
}
