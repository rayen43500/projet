package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "intermediaire")
public class Intermediaire {

    @Id
    private String id;

    private String nomFichier;
    private LocalDate dateImport;


    private int codeIntermediaire;
    private String libelleCourt;
    private String libelleLong;

    private String numeroCompte;
    private int typeBanque;

    private String adresse;
    private int codeBanque;



    public String getId() {
        return id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public LocalDate getDateImport() {
        return dateImport;
    }

    public void setDateImport(LocalDate dateImport) {
        this.dateImport = dateImport;
    }

    public int getCodeIntermediaire() {
        return codeIntermediaire;
    }

    public void setCodeIntermediaire(int codeIntermediaire) {
        this.codeIntermediaire = codeIntermediaire;
    }

    public String getLibelleCourt() {
        return libelleCourt;
    }

    public void setLibelleCourt(String libelleCourt) {
        this.libelleCourt = libelleCourt;
    }

    public String getLibelleLong() {
        return libelleLong;
    }

    public void setLibelleLong(String libelleLong) {
        this.libelleLong = libelleLong;
    }

    public  String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte( String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public int getTypeBanque() {
        return typeBanque;
    }

    public void setTypeBanque(int typeBanque) {
        this.typeBanque = typeBanque;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public int getCodeBanque() {
        return codeBanque;
    }

    public void setCodeBanque(int codeBanque) {
        this.codeBanque = codeBanque;
    }
}