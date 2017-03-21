package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@NamedQueries({ @NamedQuery(name = "Area.findNonObsoleteAreasWithTypeCode",
                            query = "SELECT o FROM Area o WHERE obsolete = false and type = :typeCode ORDER BY name"),
                @NamedQuery(name = "Area.findByIdList",
                            query = "SELECT o FROM Area o WHERE id IN (:idList) ORDER BY name"),
                @NamedQuery(name = "Area.calculateAreaLength",
                            query = "SELECT SUM(l.length) FROM Link l INNER JOIN l.areas a WHERE a.id = :areaId and l.obsolete = 0") })
public class Area implements Serializable {

    private Long id;
    private Long naturalId;
    private String name;
    private Integer typeCode;
    private boolean obsolete; // required
    private Date obsoleteDate;
    private List<Link> links;

    public enum Type {
        CITY_REGION("area.type.cityRegion", 1),
        CONNECTING_ROUTE("area.type.connectingRoute", 2),
        ROAD_NETWORK("area.type.roadNetwork", 3);

        private int code;
        private String displayedName;

        public static Type getType(int code) {
            for (Type type : Type.values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }

        Type(String displayedName, int code) {
            this.displayedName = displayedName;
            this.code = code;
        }

        public String getDisplayedName() {
            return this.displayedName;
        }

        public int getCode() {
            return code;
        }
    }

    public Area() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "AREA_SEQ")
    @SequenceGenerator(name = "AREA_SEQ",
                       sequenceName = "seq_area")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NATURAL_ID")
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

    @Column(name = "TYPE")
    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    @Transient
    public Type getType() {
        return Type.getType(typeCode);
    }

    @Transient
    public void setType(Type type) {
        this.typeCode = type.getCode();
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    @Column(name = "OBSOLETE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "area_link",
               joinColumns = { @JoinColumn(name = "area_id") },
               inverseJoinColumns = @JoinColumn(name = "link_id"))
    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

}
