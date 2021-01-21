
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Sender's contact information", value = "ContactV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "phone",
    "fax",
    "email"
})
public class Contact extends JsonAdditionalProperties {

    @ApiModelProperty("Phone number")
    public String phone;

    @ApiModelProperty("Fax number")
    public String fax;

    @ApiModelProperty("Email")
    public String email;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Contact() {
    }

    public Contact(String phone, String fax, String email) {
        super();
        this.phone = phone;
        this.fax = fax;
        this.email = email;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
