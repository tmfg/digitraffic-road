package fi.livi.digitraffic.tie.controller;

import org.springframework.http.MediaType;

public class MediaTypes {

    /* Set also charset=UTF-8 as mobile applications wont always work without it. */
    public static final String MEDIA_TYPE_APPLICATION_VND_GEO_JSON = "application/vnd.geo+json;charset=UTF-8";
    public static final String MEDIA_TYPE_APPLICATION_GEO_JSON = "application/geo+json;charset=UTF-8";
    public static final String MEDIA_TYPE_APPLICATION_JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;

    private MediaTypes() {}
}
