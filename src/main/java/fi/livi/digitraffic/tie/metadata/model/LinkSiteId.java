package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class LinkSiteId implements Serializable {

    @ManyToOne(cascade = CascadeType.ALL)
    private Link link;

    @ManyToOne(cascade = CascadeType.ALL)
    private Site site;

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        LinkSiteId that = (LinkSiteId) o;

        return new EqualsBuilder()
            .append(link, that.link)
            .append(site, that.site)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(link)
            .append(site)
            .toHashCode();
    }
}
