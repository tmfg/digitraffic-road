package fi.livi.digitraffic.tie.geojson.lamstation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.livi.digitraffic.tie.geojson.RoadStationProperties;
import fi.livi.digitraffic.tie.model.LamStationType;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Lam station properties")
@JsonTypeInfo(property = "type",  use = JsonTypeInfo.Id.NAME)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LamStationProperties extends RoadStationProperties {

    private long id;

    // lam aseman naturalId
    private long lamNaturalId;

//    private Long lotjuId;
//    private double summerFreeFlowSpeed1;
//    private double summerFreeFlowSpeed2;
//    private double winterFreeFlowSpeed1;
//    private double winterFreeFlowSpeed2;
    private String direction1Municipality;
    private Integer direction1MunicipalityCode;
    private String direction2Municipality;
    private Integer direction2MunicipalityCode;
    private LamStationType lamStationType;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLamNaturalId() {
        return lamNaturalId;
    }

    public void setLamNaturalId(long lamNaturalId) {
        this.lamNaturalId = lamNaturalId;
    }

    public String getDirection1Municipality() {
        return direction1Municipality;
    }

    public void setDirection1Municipality(String direction1Municipality) {
        this.direction1Municipality = direction1Municipality;
    }

    public Integer getDirection1MunicipalityCode() {
        return direction1MunicipalityCode;
    }

    public void setDirection1MunicipalityCode(Integer direction1MunicipalityCode) {
        this.direction1MunicipalityCode = direction1MunicipalityCode;
    }

    public String getDirection2Municipality() {
        return direction2Municipality;
    }

    public void setDirection2Municipality(String direction2Municipality) {
        this.direction2Municipality = direction2Municipality;
    }

    public Integer getDirection2MunicipalityCode() {
        return direction2MunicipalityCode;
    }

    public void setDirection2MunicipalityCode(Integer direction2MunicipalityCode) {
        this.direction2MunicipalityCode = direction2MunicipalityCode;
    }

    public LamStationType getLamStationType() {
        return lamStationType;
    }

    public void setLamStationType(LamStationType lamStationType) {
        this.lamStationType = lamStationType;
    }

}
