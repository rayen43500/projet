package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "appelrestitutionsem")
public class AppelRestitutionSem {

    @Id
    private String id;

    private LocalDate dateSeance;
    private String intermediaire;
    private String codeIntr;

    private LocalDate seanceValeur;

    private String fax;
    private String adresse;
    private String numeroCpt;

    private int apportInit;
    private int apportInitAjuste;

    private int appel;
    private int restitution;

    // GETTERS / SETTERS

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getDateSeance() { return dateSeance; }
    public void setDateSeance(LocalDate dateSeance) { this.dateSeance = dateSeance; }

    public String getIntermediaire() { return intermediaire; }
    public void setIntermediaire(String intermediaire) { this.intermediaire = intermediaire; }

    public String getCodeIntr() { return codeIntr; }
    public void setCodeIntr(String codeIntr) { this.codeIntr = codeIntr; }

    public LocalDate getSeanceValeur() { return seanceValeur; }
    public void setSeanceValeur(LocalDate seanceValeur) { this.seanceValeur = seanceValeur; }

    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getNumeroCpt() { return numeroCpt; }
    public void setNumeroCpt(String numeroCpt) { this.numeroCpt = numeroCpt; }

    public int getApportInit() { return apportInit; }
    public void setApportInit(int apportInit) { this.apportInit = apportInit; }

    public int getApportInitAjuste() { return apportInitAjuste; }
    public void setApportInitAjuste(int apportInitAjuste) { this.apportInitAjuste = apportInitAjuste; }

    public int getAppel() { return appel; }
    public void setAppel(int appel) { this.appel = appel; }

    public int getRestitution() { return restitution; }
    public void setRestitution(int restitution) { this.restitution = restitution; }
}