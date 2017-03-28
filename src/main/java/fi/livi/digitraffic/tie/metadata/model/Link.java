package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
    @Column(name = "OBSOLETE_DATE")
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "area_link", joinColumns = { @JoinColumn(name = "link_id") }, inverseJoinColumns = @JoinColumn(name = "area_id"))
    private List<Area> areas;

    // TODO: remove if not needed
    @Column(nullable = false)
    private Long direction;
    @OneToMany(targetEntity = LatestJourneytimeMedian.class, mappedBy = "link", fetch = FetchType.LAZY)
    private List<LatestJourneytimeMedian> latestJourneyTimeMedians;
    @OneToOne(fetch = FetchType.LAZY, mappedBy="link")
    private FluencyAlertBlacklist fluencyAlertBlacklist;

    // TODO: remove if not needed
    @Column(nullable = false)
    private Boolean replacement;
    @Column(name = "MAP_TURNING_LAYER")
    private Boolean onTurningLinksLayer;
    @Column(nullable = false)
    private Boolean special;

    /**
     * Helper -method, returns the correct free flow speed based on whether it
     * is summer or winter. Accesses RoadDistrict
     * 
     * @return
     */
    @Transient
    public BigDecimal getFreeFlowSpeed() {
        if (roadDistrict.getSpeedLimitSeason().equals(SpeedLimitSeason.SUMMER.getCode())) {
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

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }

    public Long getDirection() {
        return direction;
    }

    public void setDirection(Long direction) {
        this.direction = direction;
    }

    public List<LatestJourneytimeMedian> getLatestJourneyTimeMedians() {
        return latestJourneyTimeMedians;
    }

    public void setLatestJourneyTimeMedians(List<LatestJourneytimeMedian> latestJourneyTimeMedians) {
        this.latestJourneyTimeMedians = latestJourneyTimeMedians;
    }

    public FluencyAlertBlacklist getFluencyAlertBlacklist() {
        return fluencyAlertBlacklist;
    }

    public void setFluencyAlertBlacklist(FluencyAlertBlacklist fluencyAlertBlacklist) {
        this.fluencyAlertBlacklist = fluencyAlertBlacklist;
    }

    public Boolean getReplacement() {
        return replacement;
    }

    public void setReplacement(Boolean replacement) {
        this.replacement = replacement;
    }

    public Boolean getOnTurningLinksLayer() {
        return onTurningLinksLayer;
    }

    public void setOnTurningLinksLayer(Boolean onTurningLinksLayer) {
        this.onTurningLinksLayer = onTurningLinksLayer;
    }

    public Boolean getSpecial() {
        return special;
    }

    public void setSpecial(Boolean special) {
        this.special = special;
    }
}
