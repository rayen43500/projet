package com.fgm.gestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "seance")
public class Seance {

    @Id
    private String id;

    private LocalDate seance;

    private int delai;

    private LocalDate dateLivraison;

    /** OUVERTE | CLOTUREE | PREPAREE | ANNULEE — utilisé par le front FGM */
    private String statut;

    private String motifAnnulation;

    private List<String> anomalies = new ArrayList<>();

    public String getId() {
        return id;
    }

    /** Virtual field: returns the session date as ISO string "yyyy-MM-dd" for the frontend */
    @JsonProperty("dateSeance")
    public String getDateSeance() {
        return seance != null ? seance.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    /** Additional frontend fields with safe defaults */
    public int getNbTransactions()  { return 0; }
    public int getNbIntermediaires(){ return 0; }
    public double getVolumeTND()    { return 0.0; }
    public String getHeureOuverture(){ return null; }
    public String getHeureCloture() { return null; }

    public LocalDate getSeance() {
        return seance;
    }

    public void setSeance(LocalDate seance) {
        this.seance = seance;
    }

    public int getDelai() {
        return delai;
    }

    public void setDelai(int delai) {
        this.delai = delai;
    }

    public LocalDate getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDate dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMotifAnnulation() {
        return motifAnnulation;
    }

    public void setMotifAnnulation(String motifAnnulation) {
        this.motifAnnulation = motifAnnulation;
    }

    public List<String> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<String> anomalies) {
        this.anomalies = anomalies != null ? anomalies : new ArrayList<>();
    }
}