package fi.livi.digitraffic.tie.metadata.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.converter.RoadStationStateConverter;
import fi.livi.digitraffic.tie.metadata.converter.RoadStationTypeConverter;
import fi.livi.digitraffic.tie.metadata.converter.RoadStationTypeEnumConverter;

@Entity
@DynamicUpdate
public class RoadStation {

    @Id
    @GenericGenerator(name = "SEQ_ROAD_STATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_STATION"))
    @GeneratedValue(generator = "SEQ_ROAD_STATION")
    private Long id;

    @NotNull
    private Long naturalId;

    private Long lotjuId;

    private String name;

    @Convert(converter = RoadStationTypeConverter.class)
    private RoadStationType type;

    /**
     * This is used only in db queries
     */
    @Convert(converter = RoadStationTypeEnumConverter.class)
    private RoadStationType roadStationType;

    @Convert(converter = RoadStationStateConverter.class)
    private RoadStationState state;

    private boolean obsolete;

    private LocalDate obsoleteDate;

    @Column(name="IS_PUBLIC")
    private boolean isPublic;

    private String nameFi, nameSv, nameEn;

    private BigDecimal latitude, longitude, altitude;

    private Integer collectionInterval;

    @Enumerated(EnumType.STRING)
    private CollectionStatus collectionStatus;

    private String municipality;

    private String municipalityCode;

    private String province;

    private String provinceCode;

    private String location;
    private LocalDateTime startDate;
    private String country;
    private String liviId;
    private LocalDateTime repairMaintenanceDate;
    private LocalDateTime annualMaintenanceDate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="ROAD_ADDRESS_ID", unique=true)
    @Fetch(FetchMode.JOIN)
    private RoadAddress roadAddress;


    @ManyToMany
    @JoinTable(name = "ROAD_STATION_SENSORS",
               joinColumns = @JoinColumn(name = "ROAD_STATION_ID", referencedColumnName = "ID"),
               inverseJoinColumns = @JoinColumn(name = "ROAD_STATION_SENSOR_ID", referencedColumnName = "ID"))
    List<RoadStationSensor> roadStationSensors;

    protected RoadStation() {
    }

    public RoadStation(final RoadStationType type) {
        setType(type);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final Long naturalId) {
        this.naturalId = naturalId;
    }


    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
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
        if (this.type != null && !this.type.equals(type)) {
            throw new IllegalArgumentException("RoadStationType can not be changed once set. (" + this.type + " -> " + type + " )");
        }
        this.type = type;
        this.roadStationType = type;
    }

    public void setPublic(final boolean aPublic) {
        this.isPublic = aPublic;
    }

    public boolean isPublic() {
        return isPublic;
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
            return true;
        }
        return false;
    }

    public Integer getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(final Integer collectionInterval) {
        this.collectionInterval = collectionInterval;
    }

    public CollectionStatus getCollectionStatus() {
        return collectionStatus;
    }

    public void setCollectionStatus(final CollectionStatus collectionStatus) {
        this.collectionStatus = collectionStatus;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(final String municipality) {
        this.municipality = municipality;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(final String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(final String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public List<RoadStationSensor> getRoadStationSensors() {
        return roadStationSensors;
    }

    public void setRoadStationSensors(final List<RoadStationSensor> roadStationSensors) {
        this.roadStationSensors = roadStationSensors;
    }

    public void setRepairMaintenanceDate(final LocalDateTime repairMaintenanceDate) {
        this.repairMaintenanceDate = repairMaintenanceDate;
    }

    public LocalDateTime getRepairMaintenanceDate() {
        return repairMaintenanceDate;
    }

    public void setAnnualMaintenanceDate(final LocalDateTime annualMaintenanceDate) {
        this.annualMaintenanceDate = annualMaintenanceDate;
    }

    public LocalDateTime getAnnualMaintenanceDate() {
        return annualMaintenanceDate;
    }

    public RoadAddress getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(final RoadAddress roadAddress) {
        this.roadAddress = roadAddress;
    }

    public RoadStationState getState() {
        return state;
    }

    public void setState(final RoadStationState state) {
        this.state = state;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setStartDate(final LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setLiviId(final String liviId) {
        this.liviId = liviId;
    }

    public String getLiviId() {
        return liviId;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("id", getId())
                .appendField("naturalId", getNaturalId())
                .appendField("name", getName())
                .appendField("type", getType())
                .toString();
    }
}
