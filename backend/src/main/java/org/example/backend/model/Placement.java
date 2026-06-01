package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "placement")
public class Placement {

    @Id
    private String id;

    @Field("SessionDate")
    private String sessionDate; // format: dd/MM/yyyy

    @Field("CodeIntermediaire")
    private Integer codeIntermediaire;

    @Field("Intermediaire")
    private String intermediaire;

    @Field("MtProv")
    private double mtProv;

    @Field("MontantSaisi")
private double montantSaisi;

@Field("Cumule")
private double cumule;

@Field("SoldePlace")
private double soldePlace;

@Field("TotalProvision")
private double totalProvision;

@Field("TotalCumule")
private double totalCumule;

@Field("TotalMontantSaisi")
private double totalMontantSaisi;

@Field("TotalSoldePlace")
private double totalSoldePlace;

@Field("Divers")
private double divers;

@Field("TotalGeneral")
private double totalGeneral;

@Field("Interet")
private double interet;

    // GETTERS / SETTERS

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Integer getCodeIntermediaire() {
        return codeIntermediaire;
    }

    public void setCodeIntermediaire(Integer codeIntermediaire) {
        this.codeIntermediaire = codeIntermediaire;
    }

    public String getIntermediaire() {
        return intermediaire;
    }

    public void setIntermediaire(String intermediaire) {
        this.intermediaire = intermediaire;
    }

    public double getMtProv() {
        return mtProv;
    }

    public void setMtProv(double mtProv) {
        this.mtProv = mtProv;
    }

    public double getMontantSaisi() {
    return montantSaisi;
}

public void setMontantSaisi(double montantSaisi) {
    this.montantSaisi = montantSaisi;
}

public double getCumule() {
    return cumule;
}

public void setCumule(double cumule) {
    this.cumule = cumule;
}

public double getSoldePlace() {
    return soldePlace;
}

public void setSoldePlace(double soldePlace) {
    this.soldePlace = soldePlace;
}

public double getTotalProvision() {
    return totalProvision;
}

public void setTotalProvision(double totalProvision) {
    this.totalProvision = totalProvision;
}

public double getTotalCumule() {
    return totalCumule;
}

public void setTotalCumule(double totalCumule) {
    this.totalCumule = totalCumule;
}

public double getTotalMontantSaisi() {
    return totalMontantSaisi;
}

public void setTotalMontantSaisi(double totalMontantSaisi) {
    this.totalMontantSaisi = totalMontantSaisi;
}

public double getTotalSoldePlace() {
    return totalSoldePlace;
}

public void setTotalSoldePlace(double totalSoldePlace) {
    this.totalSoldePlace = totalSoldePlace;
}

public double getDivers() {
    return divers;
}

public void setDivers(double divers) {
    this.divers = divers;
}

public double getTotalGeneral() {
    return totalGeneral;
}

public void setTotalGeneral(double totalGeneral) {
    this.totalGeneral = totalGeneral;
}

public double getInteret() {
    return interet;
}

public void setInteret(double interet) {
    this.interet = interet;
}
}