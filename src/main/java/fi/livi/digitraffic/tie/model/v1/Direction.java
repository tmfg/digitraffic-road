package fi.livi.digitraffic.tie.model.v1;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@DynamicUpdate
@JsonPropertyOrder({ "naturalId", "nameFi", "nameSv", "nameEn", "rdi" })
public class Direction {

    @Id
    @Schema(description = "Direction id")
    private Long naturalId;

    @Schema(description = "Direction name in Finnish")
    private String nameFi;

    @Schema(description = "Direction name in Swedish")
    private String nameSv;

    @Schema(description = "Direction name in English")
    private String nameEn;

    @Schema(description = "The value is P if the direction of travel is from west to east along the ring roads (Kehä Ⅰ and Kehä Ⅲ) or " +
                      "away from Helsinki along roads running  radially (such as Vt1 and Vt4), and the value is N for the opposite direction")
    private String rdi;

    @JsonIgnore
    private Timestamp obsoleteDate;

    public Long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(Long naturalId) {
        this.naturalId = naturalId;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(String nameFi) {
        this.nameFi = nameFi;
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

    public String getRdi() {
        return rdi;
    }

    public void setRdi(String rdi) {
        this.rdi = rdi;
    }

    public Timestamp getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Timestamp obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }
}
