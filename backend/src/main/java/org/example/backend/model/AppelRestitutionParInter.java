package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "appelrestitutionparinter")
public class AppelRestitutionParInter {

    @Id
    private String id;
    
    private int appel;
    private String intermediaire;
    private String codeIntermediaire;
    private String adresse;
    private String fax;

    private LocalDate dateSeance;
    private LocalDate dateValeur;
    private int risque;
    private int provision;
    private int restitution;

  
    private String numeroCompte;
// GETTERS & SETTERS

public String getId() {
    return id;
}

public void setId(String id) {
    this.id = id;
}

public String getIntermediaire() {
    return intermediaire;
}
public int getAppel() {
    return appel;
}

public void setAppel(int appel) {
    this.appel = appel;
}

public void setIntermediaire(String intermediaire) {
    this.intermediaire = intermediaire;
}

public String getCodeIntermediaire() {
    return codeIntermediaire;
}

public void setCodeIntermediaire(String codeIntermediaire) {
    this.codeIntermediaire = codeIntermediaire;
}

public String getAdresse() {
    return adresse;
}

public void setAdresse(String adresse) {
    this.adresse = adresse;
}

public String getFax() {
    return fax;
}

public void setFax(String fax) {
    this.fax = fax;
}

public LocalDate getDateSeance() {
    return dateSeance;
}

public void setDateSeance(LocalDate dateSeance) {
    this.dateSeance = dateSeance;
}

public LocalDate getDateValeur() {
    return dateValeur;
}

public void setDateValeur(LocalDate dateValeur) {
    this.dateValeur = dateValeur;
}

public int getRisque() {
    return risque;
}

public void setRisque(int risque) {
    this.risque = risque;
}

public int getProvision() {
    return provision;
}

public void setProvision(int provision) {
    this.provision = provision;
}

public int getRestitution() {
    return restitution;
}

public void setRestitution(int restitution) {
    this.restitution = restitution;
}





public String getNumeroCompte() {
    return numeroCompte;
}

public void setNumeroCompte(String numeroCompte) {
    this.numeroCompte = numeroCompte;
}
  
}