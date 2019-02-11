package fi.livi.digitraffic.tie.metadata.model.forecastsection;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class LinkId {

    @EmbeddedId
    private LinkIdPK linkIdPK;

    private Long linkId;

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
