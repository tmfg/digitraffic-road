package fi.livi.digitraffic.tie.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
public class RoadStation {
    @Id
    @SequenceGenerator(name = "RS_SEQ", sequenceName = "SEQ_ROAD_STATION")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RS_SEQ")
    private long id;

    private long naturalId;

    private String name;

    private int type;

    private boolean obsolete;

    private LocalDate obsoleteDate;

    private String nameFi, nameSe, nameEn;

    private BigDecimal latitude, longitude, elevation;

    private int roadNumber, roadPart, distance;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(final boolean obsolete) {
        this.obsolete = obsolete;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(final String nameFi) {
        this.nameFi = nameFi;
    }

    public String getNameSe() {
        return nameSe;
    }

    public void setNameSe(final String nameSe) {
        this.nameSe = nameSe;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(final String nameEn) {
        this.nameEn = nameEn;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(final BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(final BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getElevation() {
        return elevation;
    }

    public void setElevation(final BigDecimal elevation) {
        this.elevation = elevation;
    }

    public void obsolete() {
        obsoleteDate = LocalDate.now();
        obsolete = true;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(final int roadNumber) {
        this.roadNumber = roadNumber;
    }

    public int getRoadPart() {
        return roadPart;
    }

    public void setRoadPart(final int roadPart) {
        this.roadPart = roadPart;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(final int distance) {
        this.distance = distance;
    }
}
