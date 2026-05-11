package tn.esprit.pidev.dto.admin;

public class BanRequest {
    // durationDays: 1, 3, 7, 30, or null for permanent ban
    private Integer durationDays;

    public BanRequest() {}

    public BanRequest(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
}

