package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Wgs84Dto {

    public final Double lon;

    public final Double lat;

    public Wgs84Dto(@JsonProperty("lon") final String lon,
                    @JsonProperty("lat") final String lat) {

        this.lon = NumberUtils.isParsable(lon) ? Double.valueOf(lon) : null;
        this.lat = NumberUtils.isParsable(lat) ? Double.valueOf(lat) : null;
    }
}
