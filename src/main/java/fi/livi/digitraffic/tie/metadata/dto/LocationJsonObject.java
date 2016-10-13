package fi.livi.digitraffic.tie.metadata.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationJsonObject {
    public final int locationCode;

    public final String subtypeCode;

    public final String roadName;
    public final String firstName;
    public final String secondName;

    public final Integer areaRef;

    public final Integer linearRef;

    public final Integer negOffset;
    public final Integer posOffset;

    public final Boolean urban;

    public final BigDecimal wsg84Lat;
    public final BigDecimal wsg84Long;

    public LocationJsonObject(int locationCode, String subtypeCode, String roadName, String firstName, String secondName, Integer areaRef,
                              Integer linearRef,
                              Integer negOffset, Integer posOffset, Boolean urban, BigDecimal wsg84Lat, BigDecimal wsg84Long) {
        this.locationCode = locationCode;
        this.subtypeCode = subtypeCode;
        this.roadName = roadName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.areaRef = areaRef;
        this.linearRef = linearRef;
        this.negOffset = negOffset;
        this.posOffset = posOffset;
        this.urban = urban;
        this.wsg84Lat = wsg84Lat;
        this.wsg84Long = wsg84Long;
    }
}
