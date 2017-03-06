package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Name {

    @JacksonXmlText
    public final String text;

    public final String language;

    @JsonCreator
    public Name(@JacksonInject final String text,
                @JsonProperty("language") final String language) {
        this.text = text;
        this.language = language;
    }
}
