
package fi.livi.digitraffic.tie.data.model.maintenance;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sender",
    "messageIdentifier",
    "sendingTime"
})
public class Caption implements Serializable
{

    @JsonProperty(required = true)
    private Sender sender;

    @JsonProperty(required = true)
    private Integer messageIdentifier;

    @JsonProperty(required = true)
    private ZonedDateTime sendingTime;

    @JsonCreator
    public Caption(Sender sender, Integer messageIdentifier, ZonedDateTime sendingTime) {
        this.sender = sender;
        this.messageIdentifier = messageIdentifier;
        this.sendingTime = sendingTime;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Integer getMessageIdentifier() {
        return messageIdentifier;
    }

    public void setMessageIdentifier(Integer messageIdentifier) {
        this.messageIdentifier = messageIdentifier;
    }

    public ZonedDateTime getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(ZonedDateTime sendingTime) {
        this.sendingTime = sendingTime;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Caption caption = (Caption) o;

        return new EqualsBuilder()
            .append(sender, caption.sender)
            .append(messageIdentifier, caption.messageIdentifier)
            .append(sendingTime, caption.sendingTime)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(sender)
            .append(messageIdentifier)
            .append(sendingTime)
            .toHashCode();
    }
}
