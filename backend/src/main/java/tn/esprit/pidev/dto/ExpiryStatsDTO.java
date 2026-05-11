package tn.esprit.pidev.dto;

public class ExpiryStatsDTO {
    private long withinSevenDays;
    private long withinThirtyDays;
    private long withinNinetyDays;
    private long alreadyExpired;

    // Constructors
    public ExpiryStatsDTO() {}

    public ExpiryStatsDTO(long withinSevenDays, long withinThirtyDays, long withinNinetyDays, long alreadyExpired) {
        this.withinSevenDays = withinSevenDays;
        this.withinThirtyDays = withinThirtyDays;
        this.withinNinetyDays = withinNinetyDays;
        this.alreadyExpired = alreadyExpired;
    }

    // Getters and Setters
    public long getWithinSevenDays() {
        return withinSevenDays;
    }

    public void setWithinSevenDays(long withinSevenDays) {
        this.withinSevenDays = withinSevenDays;
    }

    public long getWithinThirtyDays() {
        return withinThirtyDays;
    }

    public void setWithinThirtyDays(long withinThirtyDays) {
        this.withinThirtyDays = withinThirtyDays;
    }

    public long getWithinNinetyDays() {
        return withinNinetyDays;
    }

    public void setWithinNinetyDays(long withinNinetyDays) {
        this.withinNinetyDays = withinNinetyDays;
    }

    public long getAlreadyExpired() {
        return alreadyExpired;
    }

    public void setAlreadyExpired(long alreadyExpired) {
        this.alreadyExpired = alreadyExpired;
    }
}

