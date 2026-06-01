package com.fgm.gestion.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.List;

@Data
@Document(collection = "mouvementbancaire")
public class MouvementBancaire {

    @Id
    private String id;

    private LocalDate seance;

    private String intermediaire;

    private int totalSeance;
    private int totalSeancePrecedent;
    private int totalRsusp;
    private int total;
    private int provision;
    private double difference;
    private int appel;
    private int restitution;
    private int  codeIntermediaire;
    private double apportInitial;


    // Getter et Setter pour seance
    public LocalDate getSeance() {
        return seance;
    }

    public void setSeance(LocalDate seance) {
        this.seance = seance;
    }

    // Getter et Setter pour intermediaire
    public String getIntermediaire() {
        return intermediaire;
    }

    public void setIntermediaire(String intermediaire) {
        this.intermediaire = intermediaire;
    }

    // Getter et Setter pour totalSeance
    public int getTotalSeance() {
        return totalSeance;
    }

    public void setTotalSeance(int totalSeance) {
        this.totalSeance = totalSeance;
    }

    // Getter et Setter pour totalSeancePrecedent
    public int getTotalSeancePrecedent() {
        return totalSeancePrecedent;
    }

    public void setTotalSeancePrecedent(int totalSeancePrecedent) {
        this.totalSeancePrecedent = totalSeancePrecedent;
    }

    public int getTotalRsusp() {
        return totalRsusp;
    }

    public void setTotalRsusp(int totalRsusp) {
        this.totalRsusp = totalRsusp;
    }

    // Getter et Setter pour total
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    // Getter et Setter pour provision
    public int getProvision() {
        return provision;
    }

    public void setProvision(int provision) {
        this.provision = provision;
    }

    // Getter et Setter pour difference
    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    // Getter et Setter pour appel
    public int getAppel() {
        return appel;
    }

    public void setAppel(int appel) {
        this.appel = appel;
    }

    // Getter et Setter pour restitution
    public int getRestitution() {
        return restitution;
    }

    public void setRestitution(int restitution) {
        this.restitution = restitution;
    }

    public int getCodeIntermediaire() { return codeIntermediaire; }
    public void setCodeIntermediaire(int codeIntermediaire) { this.codeIntermediaire = codeIntermediaire; }


    public double getApportInitial() {
        return apportInitial;
    }

    public void setApportInitial(double apportInitial) {
        this.apportInitial = apportInitial;
    }

}