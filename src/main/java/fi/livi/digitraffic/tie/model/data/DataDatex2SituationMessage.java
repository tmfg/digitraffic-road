package fi.livi.digitraffic.tie.model.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "data_datex2_situation_message")
public class DataDatex2SituationMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "datex2_id")
    private DataDatex2Situation situation;

    private String messageVersion;
    private String messageType;
    private String message;

    public DataDatex2SituationMessage(final String messageVersion,
                                      final String messageType,
                                      final String message) {
        this.messageVersion = messageVersion;
        this.messageType = messageType;
        this.message = message;
    }

    public DataDatex2SituationMessage() {
    }

    public String getMessageVersion() {
        return messageVersion;
    }

    public void setSituation(final DataDatex2Situation situation) {
        this.situation = situation;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }
}
