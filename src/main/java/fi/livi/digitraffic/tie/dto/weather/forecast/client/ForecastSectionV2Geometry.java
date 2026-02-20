package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import java.math.BigDecimal;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

public class ForecastSectionV2Geometry {

    private final static ObjectWriter ow = JsonMapper.builder().build().writer().withDefaultPrettyPrinter();

    private String type;

    private List<List<List<BigDecimal>>> coordinates;

    public ForecastSectionV2Geometry() {
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public List<List<List<BigDecimal>>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(final List<List<List<BigDecimal>>> coordinates) {
        this.coordinates = coordinates;
    }

    public String toJsonString() throws JacksonException {
        return ow.writeValueAsString(this);
    }
}
