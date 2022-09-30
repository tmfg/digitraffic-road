package fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ForecastSectionV2Geometry {

    private final static ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private String type;

    private List<List<List<BigDecimal>>> coordinates;

    public ForecastSectionV2Geometry() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<List<List<BigDecimal>>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<List<BigDecimal>>> coordinates) {
        this.coordinates = coordinates;
    }

    public String toJsonString() throws JsonProcessingException {
        return ow.writeValueAsString(this);
    }
}
