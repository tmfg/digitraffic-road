package fi.livi.digitraffic.tie.helper;

import fi.livi.digitraffic.tie.service.BadRequestException;

public final class EnumConverter {
    private EnumConverter() {}

    public static <E extends Enum<E>> E parseState(final Class<E> clazz, final String string) {
        try {
            return E.valueOf(clazz, string.toUpperCase());
        } catch(final IllegalArgumentException iae) {
            throw new BadRequestException(String.format("Illegal value %s for enum %s", string, clazz.getSimpleName()));
        }
    }

}
