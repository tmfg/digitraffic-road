package fi.livi.digitraffic.tie.documentation;

import fi.livi.digitraffic.tie.service.BuildVersionService;
import fi.livi.digitraffic.tie.service.MessageService;
import springfox.documentation.service.ApiInfo;

public class MetadataApiInfo extends ApiInfo {

    private BuildVersionService buildVersionService;
    protected MessageService messageService;

    public MetadataApiInfo(String title, String description, String version, String termsOfServiceUrl, String contact, String license, String licenseUrl) {
        super(title, description, version, termsOfServiceUrl, contact, license, licenseUrl);
    }

    public MetadataApiInfo(MessageService messageService, BuildVersionService buildVersionService) {
        super(null, //title,
              null, //description,
              null, //version,
              null, //termsOfServiceUrl,
              null, //contact,
              null, //license,
              null); //licenseUrl)
        this.messageService = messageService;
        this.buildVersionService = buildVersionService;
    }

    @Override
    public String getTitle() {
        return messageService.getMessage("apiInfo.title");
    }

    @Override
    public String getDescription() {
        return messageService.getMessage("apiInfo.description");
    }

    @Override
    public String getVersion() {
            return buildVersionService.getAppFullVersion();
    }

    @Override
    public String getContact() {
        return messageService.getMessage("apiInfo.contact");
    }

    @Override
    public String getTermsOfServiceUrl() {
        return messageService.getMessage("apiInfo.terms.of.service");
    }


    @Override
    public String getLicense() {
        return messageService.getMessage("apiInfo.licence");
    }

    @Override
    public String getLicenseUrl() {
        return messageService.getMessage("apiInfo.licence.url");
    }

}
