package tn.esprit.pidev.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MLApiRequest {
    @JsonProperty("nb_abonnements")      private int nbAbonnements;
    @JsonProperty("nb_annulations")      private int nbAnnulations;
    @JsonProperty("nb_renouvellements")  private int nbRenouvellements;
    @JsonProperty("anciennete_jours")    private int ancienneteJours;
    @JsonProperty("transport_type")      private String transportType;
    @JsonProperty("plan_type")           private String planType;
    @JsonProperty("prix_moyen")          private double prixMoyen;
    @JsonProperty("duree_moy_jours")     private int dureeMoyJours;
    @JsonProperty("montant_total_paye")  private double montantTotalPaye;
    @JsonProperty("points_cumules")      private int pointsCumules;
    @JsonProperty("niveau_loyalty")      private String niveauLoyalty;
    @JsonProperty("nb_transactions")     private int nbTransactions;
    @JsonProperty("a_abonnement_actif")  private int aAbonnementActif;
    @JsonProperty("jours_restants")      private int joursRestants;
    @JsonProperty("expire_sans_renew")   private int expireSansRenew;
}