package fi.livi.digitraffic.tie.documentation;

import java.util.Collections;

import fi.livi.digitraffic.tie.service.BuildVersionResolver;
import fi.livi.digitraffic.tie.service.LocalizedMessageSource;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;

public class RoadApiInfo extends ApiInfo {

    private final BuildVersionResolver buildVersionResolver;
    protected LocalizedMessageSource localizedMessageSource;

    public RoadApiInfo(final LocalizedMessageSource localizedMessageSource, final BuildVersionResolver buildVersionResolver) {
        super(null, //title,
              null, //description,
              null, //version,
              null, //termsOfServiceUrl,
              null, //contact,
              null, //license,
              null,
              Collections.emptyList()); //vendorExtensions
        this.localizedMessageSource = localizedMessageSource;
        this.buildVersionResolver = buildVersionResolver;
    }

    @Override
    public String getTitle() {
        return localizedMessageSource.getMessage("apiInfo.title");
    }

    @Override
    public String getDescription() {
        return localizedMessageSource.getMessage("apiInfo.description");
    }

    @Override
    public String getVersion() {
            return buildVersionResolver.getAppFullVersion();
    }

    @Override
    public Contact getContact() {
        return new Contact(
                localizedMessageSource.getMessage("apiInfo.contact.name"),
                localizedMessageSource.getMessage("apiInfo.contact.url"),
                "");
    }

    @Override
    public String getTermsOfServiceUrl() {
        return localizedMessageSource.getMessage("apiInfo.terms.of.service.url");
    }


    @Override
    public String getLicense() {
        return localizedMessageSource.getMessage("apiInfo.licence");
    }

    @Override
    public String getLicenseUrl() {
        return localizedMessageSource.getMessage("apiInfo.licence.url");
    }

}
