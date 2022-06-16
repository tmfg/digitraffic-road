
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sender's contact information", name = "Contact_OldV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "phone",
    "fax",
    "email"
})
public class Contact extends JsonAdditionalProperties {

    @Schema(description = "Phone number")
    public String phone;

    @Schema(description = "Fax number")
    public String fax;

    @Schema(description = "Email")
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
