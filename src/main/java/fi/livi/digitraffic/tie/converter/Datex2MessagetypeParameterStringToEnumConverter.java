package fi.livi.digitraffic.tie.converter;

import org.springframework.core.convert.converter.Converter;

import fi.livi.digitraffic.tie.data.model.Datex2MessageType;

public class Datex2MessagetypeParameterStringToEnumConverter implements Converter<String, Datex2MessageType> {

    @Override
    public Datex2MessageType convert(final String from) {
        return Datex2MessageType.valueOf(from.replace("-", "_").toUpperCase());
    }
}
