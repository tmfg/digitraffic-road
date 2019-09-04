package fi.livi.digitraffic.tie.data.dto.trafficsigns;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceMetadataSchema {
    @JsonProperty
    public String tunnus;
    @JsonProperty
    public String tyyppi;

    public String tieosoite;
    public Double etrsTm35FinX;
    public Double etrsTm35FixY;

    @JsonProperty("sijainti")
    public void setSijainti(final Map<String, Object> map) {
        this.tieosoite = (String) map.get("tieosoite");
        this.etrsTm35FinX = (Double) map.get("n");
        this.etrsTm35FixY = (Double) map.get("e");
    }
}
