package fi.livi.digitraffic.tie.geojson.lamstation;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.geojson.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.model.LamStationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Lam station properties", value = "Properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LamStationProperties extends RoadStationProperties {

    private long id;

    // lam aseman naturalId
    @ApiModelProperty(value = "Lam station's natural id")
    private long lamNaturalId;

//    private Long lotjuId;
//    private double summerFreeFlowSpeed1;
//    private double summerFreeFlowSpeed2;
//    private double winterFreeFlowSpeed1;
//    private double winterFreeFlowSpeed2;

    @ApiModelProperty(value = "Direction 1 municipality (1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso.)", required = true, position = 1)
    private String direction1Municipality;

    @ApiModelProperty(value = "Direction 1 municipality code")
    private Integer direction1MunicipalityCode;

    @ApiModelProperty(value = "Direction 2 municipality (2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso.)", required = true, position = 1)
    private String direction2Municipality;

    @ApiModelProperty(value = "Direction 2 municipality code")
    private Integer direction2MunicipalityCode;

    @ApiModelProperty(value = "Type of  lam station")
    private LamStationType lamStationType;


    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getLamNaturalId() {
        return lamNaturalId;
    }

    public void setLamNaturalId(final long lamNaturalId) {
        this.lamNaturalId = lamNaturalId;
    }

    public String getDirection1Municipality() {
        return direction1Municipality;
    }

    public void setDirection1Municipality(final String direction1Municipality) {
        this.direction1Municipality = direction1Municipality;
    }

    public Integer getDirection1MunicipalityCode() {
        return direction1MunicipalityCode;
    }

    public void setDirection1MunicipalityCode(final Integer direction1MunicipalityCode) {
        this.direction1MunicipalityCode = direction1MunicipalityCode;
    }

    public String getDirection2Municipality() {
        return direction2Municipality;
    }

    public void setDirection2Municipality(final String direction2Municipality) {
        this.direction2Municipality = direction2Municipality;
    }

    public Integer getDirection2MunicipalityCode() {
        return direction2MunicipalityCode;
    }

    public void setDirection2MunicipalityCode(final Integer direction2MunicipalityCode) {
        this.direction2MunicipalityCode = direction2MunicipalityCode;
    }

    public LamStationType getLamStationType() {
        return lamStationType;
    }

    public void setLamStationType(final LamStationType lamStationType) {
        this.lamStationType = lamStationType;
    }

}
