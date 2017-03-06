package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Text {

    @JacksonXmlText
    public final String text;

    public final String lang;

    @JsonCreator
    public Text(@JacksonInject final String text,
                @JsonProperty("lang") final String lang) {
        this.text = text;
        this.lang = lang;
    }
}