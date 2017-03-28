package fi.livi.digitraffic.tie.metadata.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

@Entity
@AssociationOverrides({ @AssociationOverride(name = "primaryKey.link", joinColumns = @JoinColumn(name = "LINK_ID")),
                        @AssociationOverride(name = "primaryKey.Site", joinColumns = @JoinColumn(name = "SITE_ID")) })
public class LinkSite {

    @EmbeddedId
    private LinkSiteId primaryKey = new LinkSiteId();

    private Integer orderNumber;

    @Transient
    public Link getLink() {
        return primaryKey.getLink();
    }

    public void setLink(Link link) {
        this.primaryKey.setLink(link);
    }

    @Transient
    public Site getSite() {
        return primaryKey.getSite();
    }

    public void setSite(Site site) {
        this.primaryKey.setSite(site);
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
}
