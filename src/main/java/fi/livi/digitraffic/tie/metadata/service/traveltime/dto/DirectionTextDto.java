package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class DirectionTextDto {

    @JacksonXmlElementWrapper(useWrapping = false, localName = "texts")
    @JacksonXmlProperty(localName = "text")
    public final List<TextDto> texts;

    public final int directionIndex;

    public final String roadDirection;

    public DirectionTextDto(@JsonProperty("texts") final List<TextDto> texts,
                            @JsonProperty("dirindex") final int directionIndex,
                            @JsonProperty("RDI") final String roadDirection) {
        this.texts = texts;
        this.directionIndex = directionIndex;
        this.roadDirection = roadDirection;
    }
}
