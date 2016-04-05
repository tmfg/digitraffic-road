package fi.livi.digitraffic.tie.data.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
public class LamFreeFlowSpeed {
    @Id
    private long lamId;

    private double freeFlowSpeed1;

    private double freeFlowSpeed2;

    public long getLamId() {
        return lamId;
    }

    public void setLamId(final long lamId) {
        this.lamId = lamId;
    }

    public double getFreeFlowSpeed1() {
        return freeFlowSpeed1;
    }

    public void setFreeFlowSpeed1(final double freeFlowSpeed1) {
        this.freeFlowSpeed1 = freeFlowSpeed1;
    }

    public double getFreeFlowSpeed2() {
        return freeFlowSpeed2;
    }

    public void setFreeFlowSpeed2(final double freeFlowSpeed2) {
        this.freeFlowSpeed2 = freeFlowSpeed2;
    }
}
