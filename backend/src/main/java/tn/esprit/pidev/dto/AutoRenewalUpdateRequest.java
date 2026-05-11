package tn.esprit.pidev.dto;

import jakarta.validation.constraints.NotNull;

public class AutoRenewalUpdateRequest {
    @NotNull
    private Boolean autoRenewal;

    public Boolean getAutoRenewal() {
        return autoRenewal;
    }

    public void setAutoRenewal(Boolean autoRenewal) {
        this.autoRenewal = autoRenewal;
    }
}
