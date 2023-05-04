package fi.livi.digitraffic.tie.documentation;

import fi.livi.digitraffic.tie.service.BuildVersionService;
import fi.livi.digitraffic.tie.service.LocalizedMessageSource;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

public class RoadApiInfo {

    private final BuildVersionService buildVersionResolver;
    protected LocalizedMessageSource localizedMessageSource;

    public RoadApiInfo(final LocalizedMessageSource localizedMessageSource, final BuildVersionService buildVersionResolver) {
        this.localizedMessageSource = localizedMessageSource;
        this.buildVersionResolver = buildVersionResolver;
    }

    public String getTitle() {
        return localizedMessageSource.getMessage("apiInfo.title");
    }

    public String getDescription() {
        return localizedMessageSource.getMessage("apiInfo.description");
    }

    public String getVersion() {
            return buildVersionResolver.getAppFullVersion();
    }

    public Contact getContact() {
        return new Contact()
            .name(localizedMessageSource.getMessage("apiInfo.contact.name"))
            .url(localizedMessageSource.getMessage("apiInfo.contact.url"));
    }

    public String getTermsOfServiceUrl() {
        return localizedMessageSource.getMessage("apiInfo.terms.of.service.url");
    }

    public License getLicense() {
        return new License()
            .name(localizedMessageSource.getMessage("apiInfo.licence"))
            .url(localizedMessageSource.getMessage("apiInfo.licence.url"));
    }

}
