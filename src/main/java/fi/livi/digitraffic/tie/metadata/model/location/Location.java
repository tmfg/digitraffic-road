package fi.livi.digitraffic.tie.metadata.model.location;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
public class Location {
    @Id
    private int locationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtype_code", nullable = false)
    private LocationSubtype locationSubtype;

    private String roadJunction;
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

    @Column(name = "wgs84_lat")
    private BigDecimal wgs84Lat;

    @Column(name = "wgs84_long")
    private BigDecimal wgs84Long;

    private String negDirection;
    private String posDirection;

    private String geocode;
    private Integer orderOfPoint;

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

    public BigDecimal getWgs84Lat() {
        return wgs84Lat;
    }

    public void setWgs84Lat(BigDecimal wgs84Lat) {
        this.wgs84Lat = wgs84Lat;
    }

    public BigDecimal getWgs84Long() {
        return wgs84Long;
    }

    public void setWgs84Long(BigDecimal wgs84Long) {
        this.wgs84Long = wgs84Long;
    }

    public String getRoadJunction() {
        return roadJunction;
    }

    public void setRoadJunction(String roadJunction) {
        this.roadJunction = roadJunction;
    }

    public String getNegDirection() {
        return negDirection;
    }

    public void setNegDirection(String negDirection) {
        this.negDirection = negDirection;
    }

    public String getPosDirection() {
        return posDirection;
    }

    public void setPosDirection(String posDirection) {
        this.posDirection = posDirection;
    }

    public String getGeocode() {
        return geocode;
    }

    public void setGeocode(String geocode) {
        this.geocode = geocode;
    }

    public Integer getOrderOfPoint() {
        return orderOfPoint;
    }

    public void setOrderOfPoint(Integer orderOfPoint) {
        this.orderOfPoint = orderOfPoint;
    }
}
