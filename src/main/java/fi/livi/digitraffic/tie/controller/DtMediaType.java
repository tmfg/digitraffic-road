package fi.livi.digitraffic.tie.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;

/** Overides for mediatypes with charset=UTF-8 as mobile applications sometimes needs it **/
public class DtMediaType {

    private static final String UTF_8_VALUE_SUFFIX = ";charset=UTF-8";

    /* Set also charset=UTF-8 as mobile applications won't always work without it. */
    public static final String APPLICATION_VND_GEO_JSON_VALUE = "application/vnd.geo+json" + UTF_8_VALUE_SUFFIX;
    public static final String APPLICATION_GEO_JSON_VALUE = "application/geo+json" + UTF_8_VALUE_SUFFIX;
    @SuppressWarnings("deprecation")
    public static final String APPLICATION_JSON_VALUE = MediaType.APPLICATION_JSON_UTF8_VALUE;
    public static final String APPLICATION_XML_VALUE = MediaType.APPLICATION_XML_VALUE + UTF_8_VALUE_SUFFIX;

    @SuppressWarnings("deprecation")
    public static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON_UTF8;
    public static final MediaType APPLICATION_XML = new MediaType("application", "xml", StandardCharsets.UTF_8);

    private DtMediaType() {}
}
