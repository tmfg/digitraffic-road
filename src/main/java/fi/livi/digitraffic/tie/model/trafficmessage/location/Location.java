package fi.livi.digitraffic.tie.model.trafficmessage.location;

import java.math.BigDecimal;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@DynamicUpdate
@IdClass(LocationKey.class)
public class Location {
    @Id
    private Integer locationCode;
    @Id
    private String version;

    private String subtypeCode;
    private String roadJunction;
    private String roadName;
    private String firstName;
    private String secondName;

    private Integer areaRef;
    private Integer linearRef;

    private Integer negOffset;
    private Integer posOffset;

    private Boolean urban;

    @Column(name = "wgs84_lat")
    private BigDecimal wgs84Lat;
    @Column(name = "wgs84_long")
    private BigDecimal wgs84Long;

    @Column(name = "etrs_tm35fin_x")
    private BigDecimal etrsTm35FinX;
    @Column(name = "etrs_tm35fin_y")
    private BigDecimal etrsTm35FixY;

    private String negDirection;
    private String posDirection;

    private String geocode;
    private Integer orderOfPoint;

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(final String roadName) {
        this.roadName = roadName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(final String secondName) {
        this.secondName = secondName;
    }

    public Integer getNegOffset() {
        return negOffset;
    }

    public void setNegOffset(final Integer negOffset) {
        this.negOffset = negOffset;
    }

    public Integer getPosOffset() {
        return posOffset;
    }

    public void setPosOffset(final Integer posOffset) {
        this.posOffset = posOffset;
    }

    public Boolean getUrban() {
        return urban;
    }

    public void setUrban(final Boolean urban) {
        this.urban = urban;
    }

    public BigDecimal getWgs84Lat() {
        return wgs84Lat;
    }

    public void setWgs84Lat(final BigDecimal wgs84Lat) {
        this.wgs84Lat = wgs84Lat;
    }

    public BigDecimal getWgs84Long() {
        return wgs84Long;
    }

    public void setWgs84Long(final BigDecimal wgs84Long) {
        this.wgs84Long = wgs84Long;
    }

    public String getRoadJunction() {
        return roadJunction;
    }

    public void setRoadJunction(final String roadJunction) {
        this.roadJunction = roadJunction;
    }

    public String getNegDirection() {
        return negDirection;
    }

    public void setNegDirection(final String negDirection) {
        this.negDirection = negDirection;
    }

    public String getPosDirection() {
        return posDirection;
    }

    public void setPosDirection(final String posDirection) {
        this.posDirection = posDirection;
    }

    public String getGeocode() {
        return geocode;
    }

    public void setGeocode(final String geocode) {
        this.geocode = geocode;
    }

    public Integer getOrderOfPoint() {
        return orderOfPoint;
    }

    public void setOrderOfPoint(final Integer orderOfPoint) {
        this.orderOfPoint = orderOfPoint;
    }

    public BigDecimal getEtrsTm35FinX() {
        return etrsTm35FinX;
    }

    public void setEtrsTm35FinX(final BigDecimal etrsTm35FinX) {
        this.etrsTm35FinX = etrsTm35FinX;
    }

    public BigDecimal getEtrsTm35FixY() {
        return etrsTm35FixY;
    }

    public void setEtrsTm35FixY(final BigDecimal etrsTm35FixY) {
        this.etrsTm35FixY = etrsTm35FixY;
    }

    public Integer getLocationCode() {
        return locationCode;
    }

    public String getSubtypeCode() {
        return subtypeCode;
    }

    public void setSubtypeCode(final String subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    public Integer getAreaRef() {
        return areaRef;
    }

    public void setAreaRef(final Integer areaRef) {
        this.areaRef = areaRef;
    }

    public Integer getLinearRef() {
        return linearRef;
    }

    public void setLinearRef(final Integer linearRef) {
        this.linearRef = linearRef;
    }

    public void setLocationCode(final Integer locationCode) {
        this.locationCode = locationCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }
}
