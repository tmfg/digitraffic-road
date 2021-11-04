package fi.livi.digitraffic.tie.controller;

import org.springframework.http.MediaType;

public class MediaTypeValues {

    /* Set also charset=UTF-8 as mobile applications wont always work without it. */
    public static final String APPLICATION_VND_GEO_JSON = "application/vnd.geo+json;charset=UTF-8";
    public static final String APPLICATION_GEO_JSON = "application/geo+json;charset=UTF-8";
    public static final String APPLICATION_JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;
    public static final String APPLICATION_XML = MediaType.APPLICATION_XML_VALUE;

    private MediaTypeValues() {}
}
