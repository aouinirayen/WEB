package tn.esprit.pidev.dto;

import lombok.Data;

@Data
public class ActionSendResponse {
    private Long   passengerId;
    private String username;
    private String action;
    private String promoCode;
    private Double discountPercentage;
    private String riskLevel;
    private String message;
    private boolean emailSent;
}
