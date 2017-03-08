package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Site {

    public final int number;

    @JacksonXmlElementWrapper(useWrapping = false, localName = "names")
    @JacksonXmlProperty(localName = "name")
    public final List<Name> names;

    public final int roadNumber;

    public final String roadRegisterAddress;

    public final CO1 coordinatesKkj3;

    public final WGS84 coordinatesWgs84;

    public Site(@JsonProperty("number") final int number,
                @JsonProperty("names") final List<Name> names,
                @JsonProperty("RNO") final int roadNumber,
                @JsonProperty("TRO") final String roadRegisterAddress,
                @JsonProperty("CO1") final CO1 coordinatesKkj3,
                @JsonProperty("WGS84") final WGS84 coordinatesWgs84) {
        this.number = number;
        this.names = names;
        this.roadNumber = roadNumber;
        this.roadRegisterAddress = roadRegisterAddress;
        this.coordinatesKkj3 = coordinatesKkj3;
        this.coordinatesWgs84 = coordinatesWgs84;
    }
}
