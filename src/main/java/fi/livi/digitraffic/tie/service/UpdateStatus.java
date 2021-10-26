package fi.livi.digitraffic.tie.service;

public enum UpdateStatus {
    UPDATED,
    NOT_UPDATED,
    INSERTED;

    public boolean isUpdateOrInsert() {
        return this.equals(UPDATED) || this.equals(INSERTED);
    }
}
