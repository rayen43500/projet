package com.fgm.gestion.model;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "risque")
public class Risque {

    @Id
    private String id;

    private LocalDate seance;

    private String intermediaire;
    private String valeur;

    private double cloture;

    private double quantitenette;
    private double montantnette;


    private double quantitenettej_1;
    private double montantnettej_1;

    private String pntj;
    private String pntj_1;


    private int risquej;
    private int risquej_1;
    /** RS — risque suspens au dernier cours */
    private int risqueSuspens;

    private int codeIntermediaire;







    public String getId() {
        return id;
    }

    public LocalDate getSeance() {
        return seance;
    }

    public String getIntermediaire() {
        return intermediaire;
    }

    public String getValeur() {
        return valeur;
    }

    public double getCloture() {
        return cloture;
    }

    public double getQuantitenette() {
        return quantitenette;
    }

    public double getMontantnette() {
        return montantnette;
    }

    public double getQuantitenettej_1() {
        return quantitenettej_1;
    }

    public double getMontantnettej_1() {
        return montantnettej_1;
    }

    public String getPntj() {
        return pntj;
    }

    public String getPntj_1() {
        return pntj_1;
    }

    public int getRisquej() {
        return risquej;
    }

    public int getRisquej_1() {
        return risquej_1;
    }

    public int getRisqueSuspens() {
        return risqueSuspens;
    }



    public void setId(String id) {
        this.id = id;
    }

    public void setSeance(LocalDate seance) {
        this.seance = seance;
    }

    public void setIntermediaire(String intermediaire) {
        this.intermediaire = intermediaire;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public void setCloture(double cloture) {
        this.cloture = cloture;
    }

    public void setQuantitenette(double quantitenette) {
        this.quantitenette = quantitenette;
    }

    public void setMontantnette(double montantnette) {
        this.montantnette = montantnette;
    }

    public void setQuantitenettej_1(double quantitenettej_1) {
        this.quantitenettej_1 = quantitenettej_1;
    }

    public void setMontantnettej_1(double montantnettej_1) {
        this.montantnettej_1 = montantnettej_1;
    }

    public void setPntj(String pntj) {
        this.pntj = pntj;
    }

    public void setPntj_1(String pntj_1) {
        this.pntj_1 = pntj_1;
    }

    public void setRisquej(int risquej) {
        this.risquej = risquej;
    }

    public void setRisquej_1(int risquej_1) {
        this.risquej_1 = risquej_1;
    }

    public void setRisqueSuspens(int risqueSuspens) {
        this.risqueSuspens = risqueSuspens;
    }

    public int getCodeIntermediaire() { return codeIntermediaire; }
    public void setCodeIntermediaire(int codeIntermediaire) { this.codeIntermediaire = codeIntermediaire; }


}