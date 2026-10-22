package fi.livi.digitraffic.tie.controller;

/**
 * Sunset dates and deprecation notes
 * <p>
 * I.e. Sunset on 1.1.2022 -> create two constants, date in format YYYY-MM-DD:
 * public static final String SUNSET_2022_01_01    = "2022-01-01";
 * public static final String API_NOTE_2022_11_01 = SUNSET_TEXT + SUNSET_2022_01_01;
 * <p>
 * And add those values for deprecated APIs:
 * @Deprecated(forRemoval = true)
 * @Sunset(date = SUNSET_2022_01_01) OR @Sunset(tbd = true)
 * @Operation(summary = "Api description plaa plaa." + ApiDeprecations.API_NOTE_2022_01_01)
 */
public final class ApiDeprecations {

    private static final String SUNSET_TEXT = " Will be removed after ";

    public static final String SUNSET_FUTURE = "TBD";
    public static final String API_NOTE_FUTURE = " Will be removed in the future ";

    // Weather V1 forecast data: replaced by Weather V2 forecasts. Sunset is a Tuesday (production release day)
    public static final String SUNSET_2026_11_17 = "2026-11-17";
    public static final String API_NOTE_2026_11_17 = SUNSET_TEXT + SUNSET_2026_11_17 + ".";

    // Weather V1 stations, sensors, history and forecast metadata: replaced by Weather V2 endpoints.
    // Traffic message V1 endpoints and the DATEX II 2.2.3 endpoints: replaced by Traffic message V2 and DATEX II 3.5 and 3.7 endpoints.
    // Sunset is a Tuesday (production release day)
    public static final String SUNSET_2027_04_13 = "2027-04-13";
    public static final String API_NOTE_2027_04_13 = SUNSET_TEXT + SUNSET_2027_04_13 + ".";

    private ApiDeprecations() {}
}
