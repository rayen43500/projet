package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "tmm")
public class Tmm {

    @Id
    private String id;

    @Field("MOIS")
    @JsonProperty("MOIS")
    private String mois;

    @Field("ANNEE")
    @JsonProperty("ANNEE")
    private int annee;

    @Field("TMM")
    @JsonProperty("TMM")
    private double tmm;

    public Tmm() {}

    public Tmm(String mois, int annee, double tmm) {
        this.mois = mois;
        this.annee = annee;
        this.tmm = tmm;
    }

    // Getters & Setters

    public String getId() {
        return id;
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public double getTmm() {
        return tmm;
    }

    public void setTmm(double tmm) {
        this.tmm = tmm;
    }
}