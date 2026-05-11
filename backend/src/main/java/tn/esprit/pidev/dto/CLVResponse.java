package tn.esprit.pidev.dto;

import lombok.Data;

@Data
public class CLVResponse {
    private Long   passengerId;
    private String username;
    private double clvValue;
    private String currency = "DT";
    private String action;
    private String interpretation;
}