package fi.livi.digitraffic.tie.controller;

import org.springframework.http.MediaType;

/** Overides for mediatypes with charset=UTF-8 as mobile applications sometimes needs it **/
public class DtMediaType {

    /* Set also charset=UTF-8 as mobile applications wont always work without it. */
    public static final String APPLICATION_VND_GEO_JSON_VALUE = "application/vnd.geo+json;charset=UTF-8";
    public static final String APPLICATION_GEO_JSON_VALUE = "application/geo+json;charset=UTF-8";
    @SuppressWarnings("deprecation")
    public static final String APPLICATION_JSON_VALUE = MediaType.APPLICATION_JSON_UTF8_VALUE;
    public static final String APPLICATION_XML_VALUE = MediaType.APPLICATION_XML_VALUE;

    @SuppressWarnings("deprecation")
    public static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON_UTF8;
    public static final MediaType APPLICATION_XML = MediaType.APPLICATION_XML;

    private DtMediaType() {}
}
