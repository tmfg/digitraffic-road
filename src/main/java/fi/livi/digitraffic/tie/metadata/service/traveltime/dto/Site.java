package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class Site {

    public final int number;

    @JacksonXmlElementWrapper(useWrapping = false)
    public final List<Name> name;

    public final int roadNumber;

    public final String roadRegisterAddress;

    public final CO1 coordinates_kkj3;

    public final WGS84 coordinates;

    public Site(@JsonProperty("number") final int number,
                @JsonProperty("name") final List<Name> name,
                @JsonProperty("RNO") final int roadNumber,
                @JsonProperty("TRO") final String roadRegisterAddress,
                @JsonProperty("CO1") final CO1 coordinates_kkj3,
                @JsonProperty("WGS84") final WGS84 coordinates) {
        this.number = number;
        this.name = name;
        this.roadNumber = roadNumber;
        this.roadRegisterAddress = roadRegisterAddress;
        this.coordinates_kkj3 = coordinates_kkj3;
        this.coordinates = coordinates;
    }
}
