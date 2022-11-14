package fi.livi.digitraffic.tie.controller;

/**
 * Sunset dates and deprecation notes
 *
 * I.e. Sunset on 1.1.2022 -> create two constants, date in format YYYY-MM-DD:
 * SUNSET_2022_01_01    = "2022-01-01"
 * API_NOTE_2022_11_01 = SUNSET_TEXT + SUNSET_2022_01_01;
 *
 * And add those values for deprecated APIs:
 * @Deprecated(forRemoval = true)
 * @Sunset(date = SUNSET_2022_01_01) OR @Sunset(tbd = true)
 * @Operation(summary = "Api description plaa plaa. " + ApiDeprecations.API_NOTE_2022_01_01)
 */
public final class ApiDeprecations {

    private static final String SUNSET_TEXT = "Will be removed after ";

    public static final String SUNSET_FUTURE = "TBD";
    public static final String SUNSET_2022_11_01 = "2022-11-01";
    public static final String SUNSET_2023_01_01 = "2023-01-01";
    public static final String API_NOTE_2022_11_01 = SUNSET_TEXT + SUNSET_2022_11_01;
    public static final String API_NOTE_2023_01_01 = SUNSET_TEXT + SUNSET_2023_01_01;
    public static final String API_NOTE_FUTURE = "Will be removed in the future";

    private ApiDeprecations() {}

    public static String willBeDeprecated(final String sinceDate) {
        return "Will be deprecated from " + sinceDate;
    }
}