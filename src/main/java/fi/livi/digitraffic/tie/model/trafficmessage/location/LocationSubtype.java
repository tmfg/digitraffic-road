package fi.livi.digitraffic.tie.model.trafficmessage.location;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
@DynamicUpdate
public class LocationSubtype {
    @EmbeddedId
    private LocationSubtypeKey id;

    private String descriptionEn;

    private String descriptionFi;

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionFi() {
        return descriptionFi;
    }

    public void setDescriptionFi(String descriptionFi) {
        this.descriptionFi = descriptionFi;
    }

    public boolean validate() {
        return id != null && id.validate() && isNotEmpty(descriptionEn) && isNotEmpty(descriptionFi);
    }

    public LocationSubtypeKey getId() {
        return id;
    }

    public void setId(LocationSubtypeKey id) {
        this.id = id;
    }

    public String getSubtypeCode() {
        return id.getSubtypeCode();
    }
}
