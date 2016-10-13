package fi.livi.digitraffic.tie.metadata.model.location;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DynamicUpdate
public class LocationType {
    @Id
    private String typeCodeFi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @JsonIgnore
    private LocationClass locationClass;

    private int typeCode;

    private String descriptionEn;

    private String descriptionFi;

    public String getTypeCodeFi() {
        return typeCodeFi;
    }

    public void setTypeCodeFi(String typeCodeFi) {
        this.typeCodeFi = typeCodeFi;
    }

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

    public int getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public LocationClass getLocationClass() {
        return locationClass;
    }

    public void setLocationClass(LocationClass locationClass) {
        this.locationClass = locationClass;
    }
}

