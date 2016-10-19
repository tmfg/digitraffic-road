package fi.livi.digitraffic.tie.metadata.model.location;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
public class LocationType {
    @Id
    private String typeCode;

    private String descriptionEn;

    private String descriptionFi;

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(final String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionFi() {
        return descriptionFi;
    }

    public void setDescriptionFi(final String descriptionFi) {
        this.descriptionFi = descriptionFi;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(final String typeCode) {
        this.typeCode = typeCode;
    }

    public boolean validate() {
        return isNotEmpty(typeCode) && isNotEmpty(descriptionEn) && isNotEmpty(descriptionFi);
    }
}

