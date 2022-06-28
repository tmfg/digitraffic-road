package fi.livi.digitraffic.tie.converter;

import org.springframework.core.convert.converter.Converter;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;

@Deprecated
public class Datex2MessagetypeParameterStringToEnumConverter implements Converter<String, Datex2MessageType> {

    @Override
    public Datex2MessageType convert(final String from) {
        final Datex2MessageType type = Datex2MessageType.valueOf(from.replace("-", "_").toUpperCase());

        if (type == null) {
            throw new IllegalArgumentException("Invalid messagetype " + from);
        }

        return type;
    }
}
