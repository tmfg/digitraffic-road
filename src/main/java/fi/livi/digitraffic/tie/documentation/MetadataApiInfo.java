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
              null, //this.description,
              null, //this.version,
              null, //this.termsOfServiceUrl,
              null, //this.contact,
              null, //this.license,
              null); //this.licenseUrl)
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

    /*
    .title("Springfox petstore API")
    .description("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum " +
                         "has been the industry's standard dummy text ever since the 1500s, when an unknown printer "
                         + "took a " +
                         "galley of type and scrambled it to make a type specimen book. It has survived not only five " +
                         "centuries, but also the leap into electronic typesetting, remaining essentially unchanged. " +
                         "It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum " +
                         "passages, and more recently with desktop publishing software like Aldus PageMaker including " +
                         "versions of Lorem Ipsum.")
    .termsOfServiceUrl("http://springfox.io")
    .contact("springfox")
    .license("Apache License Version 2.0")
    .licenseUrl("https://github.com/springfox/springfox/blob/master/LICENSE")
    .version("2.0");
*/
}
