package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "apportinitial")
public class ApportInitial {

    @Id
    private String id;

    private LocalDate seance;

    private String codeInterm;
    private String intermediaire;

    private double positionAch;
    private double positionVenduEns;

    private int apportInitial;
    private int apportInitialAjuste;

    private int appelContrib;
    private int restitution;
    private LocalDate debut;
    
    private LocalDate fin;
    private double moyequot;

    // GETTERS & SETTERS

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getSeance() {
        return seance;
    }

    public void setSeance(LocalDate seance) {
        this.seance = seance;
    }

    public String getCodeInterm() {
        return codeInterm;
    }

    public void setCodeInterm(String codeInterm) {
        this.codeInterm = codeInterm;
    }

    public String getIntermediaire() {
        return intermediaire;
    }

    public void setIntermediaire(String intermediaire) {
        this.intermediaire = intermediaire;
    }

    public double getPositionAch() {
        return positionAch;
    }

    public void setPositionAch(double positionAch) {
        this.positionAch = positionAch;
    }

    public double getPositionVenduEns() {
        return positionVenduEns;
    }

    public void setPositionVenduEns(double positionVenduEns) {
        this.positionVenduEns = positionVenduEns;
    }

    public int getApportInitial() {
        return apportInitial;
    }

    public void setApportInitial(int apportInitial) {
        this.apportInitial = apportInitial;
    }

    public int getApportInitialAjuste() {
        return apportInitialAjuste;
    }

    public void setApportInitialAjuste(int apportInitialAjuste) {
        this.apportInitialAjuste = apportInitialAjuste;
    }

    public int getAppelContrib() {
        return appelContrib;
    }

    public void setAppelContrib(int appelContrib) {
        this.appelContrib = appelContrib;
    }

    public int getRestitution() {
        return restitution;
    }

    public void setRestitution(int restitution) {
        this.restitution = restitution;
    }


    public LocalDate getDebut() {
    return debut;
}

public void setDebut(LocalDate debut) {
    this.debut = debut;
}

public LocalDate getFin() {
    return fin;
}

public void setFin(LocalDate fin) {
    this.fin = fin;
}

public double getMoyequot() {
    return moyequot;
}

public void setMoyequot(double moyequot) {
    this.moyequot = moyequot;
}
}