package fi.livi.digitraffic.tie.metadata.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Road {

    @Id
    @GenericGenerator(name = "SEQ_ROAD", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD"))
    @GeneratedValue(generator = "SEQ_ROAD")
    private Long id;

    private String naturalId;

    private boolean obsolete;

    private Date obsoleteDate;

    private String name;

    public Road() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(String naturalId) {
        this.naturalId = naturalId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
