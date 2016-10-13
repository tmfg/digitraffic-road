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
public class LocationSubtype {
    @Id
    private String subtypeCodeFi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @JsonIgnore
    private LocationClass locationClass;

    private int typeCode;

    private int subtypeCode;

    private String descriptionEn;

    private String descriptionFi;

    public int getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public int getSubtypeCode() {
        return subtypeCode;
    }

    public void setSubtypeCode(int subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    public String getSubtypeCodeFi() {
        return subtypeCodeFi;
    }

    public void setSubtypeCodeFi(String subtypeCodeFi) {
        this.subtypeCodeFi = subtypeCodeFi;
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

    public LocationClass getLocationClass() {
        return locationClass;
    }

    public void setLocationClass(LocationClass locationClass) {
        this.locationClass = locationClass;
    }
}
