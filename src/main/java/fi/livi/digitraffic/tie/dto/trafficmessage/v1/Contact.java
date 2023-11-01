
package fi.livi.digitraffic.tie.dto.trafficmessage.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.JsonAdditionalProperties;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sender's contact information", name = "ContactV1")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "phone",
    "fax",
    "email"
})
public class Contact extends JsonAdditionalProperties {

    @Schema(description = "Phone number")
    public String phone;

    @Schema(description = "Email")
    public String email;

    public Contact() {
    }

    public Contact(final String phone, final String email) {
        super();
        this.phone = phone;
        this.email = email;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
