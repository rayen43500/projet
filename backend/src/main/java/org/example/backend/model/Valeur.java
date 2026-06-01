package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Document(collection = "valeur")
public class Valeur {

    @Id
    private String id;

    private String nomFichier;
    private LocalDate seance;

    private String codeValeur;
    private String libelleValeur;

    private double veille;
    private double cloture;

    // GETTERS / SETTERS 

    public String getId() { return id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public void setSeance(LocalDate seance) { this.seance = seance; }

   public LocalDate getSeance() {
    return this.seance;
}
   

    public String getCodeValeur() { return codeValeur; }
    public void setCodeValeur(String codeValeur) { this.codeValeur = codeValeur; }

    public String getLibelleValeur() { return libelleValeur; }
    public void setLibelleValeur(String libelleValeur) { this.libelleValeur = libelleValeur; }

    public double getVeille() { return veille; }
    public void setVeille(double veille) { this.veille = veille; }

    public double getCloture() { return cloture; }
    public void setCloture(double cloture) { this.cloture = cloture; }
}