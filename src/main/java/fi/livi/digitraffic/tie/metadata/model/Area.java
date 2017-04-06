package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Area implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AREA_SEQ")
    @SequenceGenerator(name = "AREA_SEQ", sequenceName = "seq_area")
    private Long id;

    private Long naturalId;

    private String name;

    private Integer type;

    private Boolean obsolete;

    @Temporal(TemporalType.TIMESTAMP)
    private Date obsoleteDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "area_link", joinColumns = { @JoinColumn(name = "area_id") }, inverseJoinColumns = @JoinColumn(name = "link_id"))
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

    public Type getType() {
        return Type.getType(type);
    }

    public void setType(Type type) {
        this.type = type.getCode();
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

}
