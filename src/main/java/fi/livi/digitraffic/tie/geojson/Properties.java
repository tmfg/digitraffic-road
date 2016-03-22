package fi.livi.digitraffic.tie.geojson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.LamStationType;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Lam station properties")
@JsonTypeInfo(property = "type",  use = JsonTypeInfo.Id.NAME)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Properties {

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

    /* RoadStation properties*/
    private Map<String, String> names = new HashMap<>();

    private long naturalId;

    private String name;

    private Integer roadNumber;

    private Integer roadPart;

    private Integer distance;

    private Integer collectionInterval;

    private CollectionStatus collectionStatus;

    private String municipality;

    private String municipalityCode;

    private String province;

    private String provinceCode;

    private String description;

    public void addName(String lang, String name) {
        if (name != null) {
            this.names.put(lang, name);
        }
    }

    public Map<String, String> getNames() {
        return names;
    }

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

//    public Long getLotjuId() {
//        return lotjuId;
//    }
//
//    public void setLotjuId(Long lotjuId) {
//        this.lotjuId = lotjuId;
//    }

//    public double getSummerFreeFlowSpeed1() {
//        return summerFreeFlowSpeed1;
//    }
//
//    public void setSummerFreeFlowSpeed1(double summerFreeFlowSpeed1) {
//        this.summerFreeFlowSpeed1 = summerFreeFlowSpeed1;
//    }
//
//    public double getSummerFreeFlowSpeed2() {
//        return summerFreeFlowSpeed2;
//    }
//
//    public void setSummerFreeFlowSpeed2(double summerFreeFlowSpeed2) {
//        this.summerFreeFlowSpeed2 = summerFreeFlowSpeed2;
//    }
//
//    public double getWinterFreeFlowSpeed1() {
//        return winterFreeFlowSpeed1;
//    }
//
//    public void setWinterFreeFlowSpeed1(double winterFreeFlowSpeed1) {
//        this.winterFreeFlowSpeed1 = winterFreeFlowSpeed1;
//    }
//
//    public double getWinterFreeFlowSpeed2() {
//        return winterFreeFlowSpeed2;
//    }
//
//    public void setWinterFreeFlowSpeed2(double winterFreeFlowSpeed2) {
//        this.winterFreeFlowSpeed2 = winterFreeFlowSpeed2;
//    }

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

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(long naturalId) {
        this.naturalId = naturalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public boolean isObsolete() {
//        return obsolete;
//    }
//
//    public void setObsolete(boolean obsolete) {
//        this.obsolete = obsolete;
//    }
//
//    public LocalDate getObsoleteDate() {
//        return obsoleteDate;
//    }
//
//    public void setObsoleteDate(LocalDate obsoleteDate) {
//        this.obsoleteDate = obsoleteDate;
//    }

    public Integer getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(Integer roadNumber) {
        this.roadNumber = roadNumber;
    }

    public Integer getRoadPart() {
        return roadPart;
    }

    public void setRoadPart(Integer roadPart) {
        this.roadPart = roadPart;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Integer getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(Integer collectionInterval) {
        this.collectionInterval = collectionInterval;
    }

    public CollectionStatus getCollectionStatus() {
        return collectionStatus;
    }

    public void setCollectionStatus(CollectionStatus collectionStatus) {
        this.collectionStatus = collectionStatus;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
