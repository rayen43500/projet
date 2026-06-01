package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "mvtbanqueinter")
public class MvtBanqueInter {

    @Id
    private String id;

    private String intermediaire;
    private String codeInterm;
    private String banque;
    private String numeroCompte;

    private LocalDate dateSeance;
    private LocalDate dateValeur;

    private Double debit;
    private Double credit;
    private Double total;
    private Double soldeCredit;

    private String CBQUECOMP;
   

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

public void setIntermediaire(String intermediaire) {
    this.intermediaire = intermediaire;
}

public String getCodeInterm() {
    return codeInterm;
}

public void setCodeInterm(String codeInterm) {
    this.codeInterm = codeInterm;
}

public String getBanque() {
    return banque;
}

public void setBanque(String banque) {
    this.banque = banque;
}

public String getNumeroCompte() {
    return numeroCompte;
}

public void setNumeroCompte(String numeroCompte) {
    this.numeroCompte = numeroCompte;
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

public Double getDebit() {
    return debit;
}

public void setDebit(Double debit) {
    this.debit = debit;
}

public Double getCredit() {
    return credit;
}

public void setCredit(Double credit) {
    this.credit = credit;
}

public Double getTotal() {
    return total;
}

public void setTotal(Double total) {
    this.total = total;
}

public Double getSoldeCredit() {
    return soldeCredit;
}

public void setSoldeCredit(Double soldeCredit) {
    this.soldeCredit = soldeCredit;
}

public String getCBQUECOMP() {
    return CBQUECOMP;
}

public void setCBQUECOMP(String CBQUECOMP) {
    this.CBQUECOMP = CBQUECOMP;
}


}