package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@DynamicUpdate
public class Link implements Serializable {

    @Id
    @GenericGenerator(name = "SEQ_LINK", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_LINK"))
    @GeneratedValue(generator = "SEQ_LINK")
    private Long id;

    @Column(name = "NATURAL_ID", nullable = false)
    private Long naturalId;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String nameSv;
    @Column(nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private Long length;

    @Column(nullable = false)
    private Boolean obsolete;

    private Timestamp obsoleteDate;

    @Column(name = "START_ROAD_ADDRESS_DISTANCE", nullable = false)
    private Long startRoadAddressDistance;
    @Column(name = "END_ROAD_ADDRESS_DISTANCE", nullable = false)
    private Long endRoadAddressDistance;

    @Column(name = "SUMMER_FREE_FLOW_SPEED", nullable = false)
    private BigDecimal summerFreeFlowSpeed;
    @Column(name = "WINTER_FREE_FLOW_SPEED", nullable = false)
    private BigDecimal winterFreeFlowSpeed;

    @ManyToOne
    @JoinColumn(name = "START_ROAD_SECTION_ID", referencedColumnName = "ID")
    private RoadSection startRoadSection;
    @ManyToOne
    @JoinColumn(name = "END_ROAD_SECTION_ID", referencedColumnName = "ID")
    private RoadSection endRoadSection;
    @ManyToOne
    @JoinColumn(name = "ROAD_DISTRICT_ID", referencedColumnName = "ID")
    private RoadDistrict roadDistrict;

    @OneToMany(mappedBy = "primaryKey.link", cascade = CascadeType.ALL)
    private List<LinkSite> linkSites;

    @ManyToOne
    @JoinTable(name = "LINK_DIRECTION", joinColumns = { @JoinColumn(name = "LINK_ID") }, inverseJoinColumns = @JoinColumn(name = "DIRECTION_ID"))
    private Direction linkDirection;

    @Transient
    public BigDecimal getFreeFlowSpeed() {
        if (roadDistrict.getSpeedLimitSeasonCode().equals(SpeedLimitSeason.SUMMER.getCode())) {
            return summerFreeFlowSpeed;
        } else {
            return winterFreeFlowSpeed;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(Long naturalId) {
        this.naturalId = naturalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Boolean getObsolete() {
        return obsolete;
    }

    public void setObsolete(Boolean obsolete) {
        this.obsolete = obsolete;
    }

    public Timestamp getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Timestamp obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public Long getStartRoadAddressDistance() {
        return startRoadAddressDistance;
    }

    public void setStartRoadAddressDistance(Long startRoadAddressDistance) {
        this.startRoadAddressDistance = startRoadAddressDistance;
    }

    public Long getEndRoadAddressDistance() {
        return endRoadAddressDistance;
    }

    public void setEndRoadAddressDistance(Long endRoadAddressDistance) {
        this.endRoadAddressDistance = endRoadAddressDistance;
    }

    public BigDecimal getSummerFreeFlowSpeed() {
        return summerFreeFlowSpeed;
    }

    public void setSummerFreeFlowSpeed(BigDecimal summerFreeFlowSpeed) {
        this.summerFreeFlowSpeed = summerFreeFlowSpeed;
    }

    public BigDecimal getWinterFreeFlowSpeed() {
        return winterFreeFlowSpeed;
    }

    public void setWinterFreeFlowSpeed(BigDecimal winterFreeFlowSpeed) {
        this.winterFreeFlowSpeed = winterFreeFlowSpeed;
    }

    public RoadSection getStartRoadSection() {
        return startRoadSection;
    }

    public void setStartRoadSection(RoadSection startRoadSection) {
        this.startRoadSection = startRoadSection;
    }

    public RoadSection getEndRoadSection() {
        return endRoadSection;
    }

    public void setEndRoadSection(RoadSection endRoadSection) {
        this.endRoadSection = endRoadSection;
    }

    public RoadDistrict getRoadDistrict() {
        return roadDistrict;
    }

    public void setRoadDistrict(RoadDistrict roadDistrict) {
        this.roadDistrict = roadDistrict;
    }

    public List<LinkSite> getLinkSites() {
        return linkSites;
    }

    public Direction getLinkDirection() {
        return linkDirection;
    }
}
