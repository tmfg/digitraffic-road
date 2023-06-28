package fi.livi.digitraffic.tie.model.v1.forecastsection;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class LinkId {

    @JsonIgnore
    @EmbeddedId
    private LinkIdPK linkIdPK;

    private Long linkId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    public LinkId() {
    }

    public LinkId(LinkIdPK linkIdPK, Long linkId, ForecastSection forecastSection) {
        this.linkIdPK = linkIdPK;
        this.linkId = linkId;
        this.forecastSection = forecastSection;
    }

    public LinkIdPK getLinkIdPK() {
        return linkIdPK;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(final Long linkId) {
        this.linkId = linkId;
    }

    public ForecastSection getForecastSection() {
        return forecastSection;
    }
}
