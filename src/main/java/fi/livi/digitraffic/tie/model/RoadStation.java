package fi.livi.digitraffic.tie.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import fi.livi.digitraffic.tie.converter.RoadStationTypeConverter;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
public class RoadStation implements Stringifiable {
    @Id
    @SequenceGenerator(name = "RS_SEQ", sequenceName = "SEQ_ROAD_STATION")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RS_SEQ")
    private Long id;

    private long naturalId;

    private String name;

    @Convert(converter = RoadStationTypeConverter.class)
    private RoadStationType type;

    private boolean obsolete;

    private LocalDate obsoleteDate;

    private String nameFi, nameSv, nameEn;

    private BigDecimal latitude, longitude, altitude;

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

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
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

    public RoadStationType getType() {
        return type;
    }

    public void setType(final RoadStationType type) {
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

    public String getNameSv() {
        return nameSv;
    }

    public void setNameSv(final String nameSv) {
        this.nameSv = nameSv;
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

    public BigDecimal getAltitude() {
        return altitude;
    }

    public void setAltitude(final BigDecimal altitude) {
        this.altitude = altitude;
    }

    /**
     * @return true if state changed
     */
    public boolean obsolete() {
        if (obsoleteDate == null || !obsolete) {
            obsoleteDate = LocalDate.now();
            obsolete = true;
        }
        return false;
    }

    public Integer getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(final Integer roadNumber) {
        this.roadNumber = roadNumber;
    }

    public Integer getRoadPart() {
        return roadPart;
    }

    public void setRoadPart(final Integer roadPart) {
        this.roadPart = roadPart;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(final Integer distance) {
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

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("id", getId())
                .appendField("naturalId", getNaturalId())
                .appendField("name", getName())
                .toString();
    }

}