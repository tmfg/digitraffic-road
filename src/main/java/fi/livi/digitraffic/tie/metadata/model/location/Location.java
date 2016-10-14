package fi.livi.digitraffic.tie.metadata.model.location;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DynamicUpdate
public class Location {
    @Id
    private int locationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtype_code", nullable = false)
    @JsonIgnore
    private LocationSubtype locationSubtype;

    private String roadName;
    private String firstName;
    private String secondName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_ref")
    private Location areaRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linear_ref")
    private Location linearRef;

    private Integer negOffset;
    private Integer posOffset;

    private Boolean urban;

    @Column(name = "wsg84_lat")
    private BigDecimal wsg84Lat;

    @Column(name = "wsg84_long")
    private BigDecimal wsg84Long;

    public int getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(int locationCode) {
        this.locationCode = locationCode;
    }

    public LocationSubtype getLocationSubtype() {
        return locationSubtype;
    }

    public void setLocationSubtype(LocationSubtype locationSubtype) {
        this.locationSubtype = locationSubtype;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Location getAreaRef() {
        return areaRef;
    }

    public void setAreaRef(Location areaRef) {
        this.areaRef = areaRef;
    }

    public Location getLinearRef() {
        return linearRef;
    }

    public void setLinearRef(Location linearRef) {
        this.linearRef = linearRef;
    }

    public Integer getNegOffset() {
        return negOffset;
    }

    public void setNegOffset(Integer negOffset) {
        this.negOffset = negOffset;
    }

    public Integer getPosOffset() {
        return posOffset;
    }

    public void setPosOffset(Integer posOffset) {
        this.posOffset = posOffset;
    }

    public Boolean getUrban() {
        return urban;
    }

    public void setUrban(Boolean urban) {
        this.urban = urban;
    }

    public BigDecimal getWsg84Lat() {
        return wsg84Lat;
    }

    public void setWsg84Lat(BigDecimal wsg84Lat) {
        this.wsg84Lat = wsg84Lat;
    }

    public BigDecimal getWsg84Long() {
        return wsg84Long;
    }

    public void setWsg84Long(BigDecimal wsg84Long) {
        this.wsg84Long = wsg84Long;
    }
}
