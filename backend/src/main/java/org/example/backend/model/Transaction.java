package com.fgm.gestion.model;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "transaction")
public class Transaction {

    @Id
    private String id;
    private LocalDate seance;
    private String codeValeur;
    private String libelleValeur;
    private int codeIntermediaireAcheteur;
    private String libelleIntermediaireAcheteur;

    private int codeIntermediaireVendeur;
    private String libelleIntermediaireVendeur;

    private int quantiteNegociee;
    private double coursTransaction;
    private double volume;

    public String getId() {
        return id;
    }

    public LocalDate getSeance() {

        return seance;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSeance(LocalDate seance) {
        this.seance = seance;
    }

    // GETTERS  SETTERS

    public String getCodeValeur() { return codeValeur; }
    public void setCodeValeur(String codeValeur) { this.codeValeur = codeValeur; }

    public String getLibelleValeur() { return libelleValeur; }
    public void setLibelleValeur(String libelleValeur) { this.libelleValeur = libelleValeur; }

    public int getCodeIntermediaireAcheteur() { return codeIntermediaireAcheteur; }
    public void setCodeIntermediaireAcheteur(int codeIntermediaireAcheteur) { this.codeIntermediaireAcheteur = codeIntermediaireAcheteur; }

    public String getLibelleIntermediaireAcheteur() { return libelleIntermediaireAcheteur; }
    public void setLibelleIntermediaireAcheteur(String libelleIntermediaireAcheteur) { this.libelleIntermediaireAcheteur = libelleIntermediaireAcheteur; }

    public int getCodeIntermediaireVendeur() { return codeIntermediaireVendeur; }
    public void setCodeIntermediaireVendeur(int codeIntermediaireVendeur) { this.codeIntermediaireVendeur = codeIntermediaireVendeur; }

    public String getLibelleIntermediaireVendeur() { return libelleIntermediaireVendeur; }
    public void setLibelleIntermediaireVendeur(String libelleIntermediaireVendeur) { this.libelleIntermediaireVendeur = libelleIntermediaireVendeur; }

    public int getQuantiteNegociee() { return quantiteNegociee; }
    public void setQuantiteNegociee(int quantiteNegociee) { this.quantiteNegociee = quantiteNegociee; }

    public double getCoursTransaction() { return coursTransaction; }
    public void setCoursTransaction(double coursTransaction) { this.coursTransaction = coursTransaction; }

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }


}