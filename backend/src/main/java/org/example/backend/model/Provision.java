package com.fgm.gestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Document(collection = "provision")
public class Provision {

    @Id
    private String id;

    @Field("D_CALC_PROV")
    @JsonProperty("D_CALC_PROV")
    private LocalDate dateCalcul;

    @Field("C_ID_ADC")
    @JsonProperty("C_ID_ADC")
    private int idAdc;

    @Field("MT_PROV")
    @JsonProperty("MT_PROV")
    private double montantProvision;

    @Field("MT_APP_INI")
    @JsonProperty("MT_APP_INI")
    private int montantApportInitial;

    @Field("MT_RISQ_TOT")
    @JsonProperty("MT_RISQ_TOT")
    private double montantRisqueTotal;

    @DBRef
    @JsonProperty("intermediaire")
    private Intermediaire intermediaire;

    private String libelleCourtinter;

    public String getlibelleCourtinter(){return libelleCourtinter;}
    public void setLibelleCourtinter(String libelle){
        this.libelleCourtinter=libelle;
    }

    // ================= GETTERS & SETTERS =================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getDateCalcul() { return dateCalcul; }
    public void setDateCalcul(LocalDate dateCalcul) { this.dateCalcul = dateCalcul; }

    public int getIdAdc() { return idAdc; }
    public void setIdAdc(int idAdc) { this.idAdc = idAdc; }

    public double getMontantProvision() { return montantProvision; }
    public void setMontantProvision(double montantProvision) { this.montantProvision = montantProvision; }

    public int getMontantApportInitial() { return montantApportInitial; }
    public void setMontantApportInitial(int montantApportInitial) { this.montantApportInitial = montantApportInitial; }

    public double getMontantRisqueTotal() { return montantRisqueTotal; }
    public void setMontantRisqueTotal(double montantRisqueTotal) { this.montantRisqueTotal = montantRisqueTotal; }

    public Intermediaire getIntermediaire() { return intermediaire; }
    public void setIntermediaire(Intermediaire intermediaire) { this.intermediaire = intermediaire; }
}