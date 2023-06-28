package fi.livi.digitraffic.tie.model.v2.variablesign;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.io.Serializable;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Immutable
public class DeviceDataRow implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    private int screen;

    private int rowNumber;

    private String text;

    public int getScreen() {
        return screen;
    }

    public void setScreen(final int screen) {
        this.screen = screen;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(final int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
