package fi.livi.digitraffic.tie.metadata.model;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@Entity
@DynamicUpdate
@JsonPropertyOrder({ "naturalId", "nameFi", "nameSv", "nameEn", "roadSection", "roadSectionBeginDistance",
                     "xCoordKkj3", "yCoordKkj3", "longitudeWgs84", "latitudeWgs84" })
public class Site {

    @Id
    @JsonProperty(value = "id")
    @ApiModelProperty("Site id")
    private Long naturalId;

    @ApiModelProperty("Site name in Finnish")
    private String nameFi;
    @ApiModelProperty("Site name in Swedish")
    private String nameSv;
    @ApiModelProperty("Site name in English")
    private String nameEn;

    @OneToMany(mappedBy = "primaryKey.site", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LinkSite> linkSites;

    @ManyToOne
    @JoinColumn(name = "ROAD_SECTION_ID", referencedColumnName = "ID")
    private RoadSection roadSection;

    @ApiModelProperty("Distance in meters from the beginning of the road section")
    private Long roadSectionBeginDistance;

    @ApiModelProperty("Site X coordinate in Finnish National Coordinate System (KKJ3)")
    private Integer xCoordKkj3;
    @ApiModelProperty("Site Y coordinate in Finnish National Coordinate System (KKJ3)")
    private Integer yCoordKkj3;

    @ApiModelProperty("Site longitude in WGS84")
    private Double longitudeWgs84;
    @ApiModelProperty("Site latitude in WGS84")
    private Double latitudeWgs84;

    @JsonIgnore
    private Timestamp obsoleteDate;

    public Long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(Long naturalId) {
        this.naturalId = naturalId;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(String nameFi) {
        this.nameFi = nameFi;
    }

    public String getNameSv() {
        return nameSv;
    }

    public void setNameSv(String nameSv) {
        this.nameSv = nameSv;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public List<LinkSite> getLinkSites() {
        return linkSites;
    }

    public RoadSection getRoadSection() {
        return roadSection;
    }

    public void setRoadSection(RoadSection roadSection) {
        this.roadSection = roadSection;
    }

    public Long getRoadSectionBeginDistance() {
        return roadSectionBeginDistance;
    }

    public void setRoadSectionBeginDistance(Long roadSectionBeginDistance) {
        this.roadSectionBeginDistance = roadSectionBeginDistance;
    }

    public Integer getxCoordKkj3() {
        return xCoordKkj3;
    }

    public void setxCoordKkj3(Integer xCoordKkj3) {
        this.xCoordKkj3 = xCoordKkj3;
    }

    public Integer getyCoordKkj3() {
        return yCoordKkj3;
    }

    public void setyCoordKkj3(Integer yCoordKkj3) {
        this.yCoordKkj3 = yCoordKkj3;
    }

    public Double getLongitudeWgs84() {
        return longitudeWgs84;
    }

    public void setLongitudeWgs84(Double longitudeWgs84) {
        this.longitudeWgs84 = longitudeWgs84;
    }

    public Double getLatitudeWgs84() {
        return latitudeWgs84;
    }

    public void setLatitudeWgs84(Double latitudeWgs84) {
        this.latitudeWgs84 = latitudeWgs84;
    }

    public Timestamp getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Timestamp obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }
}
