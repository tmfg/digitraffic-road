package fi.livi.digitraffic.tie.metadata.model.location;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
public class LocationSubtype {
    @Id
    private String subtypeCode;

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

    public String getSubtypeCode() {
        return subtypeCode;
    }

    public void setSubtypeCode(String subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    public boolean validate() {
        return isNotEmpty(subtypeCode) && isNotEmpty(descriptionEn) && isNotEmpty(descriptionFi);
    }
}
