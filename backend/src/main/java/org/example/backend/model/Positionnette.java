package com.fgm.gestion.model;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "positionnette")
public class Positionnette {

    @Id
    private String id;

    private LocalDate seance;

    private int codeIntermediaire;
    private String intermediaire;
    private String valeur;
    private double cloture;
    private int quantitevendu;
    private int quantiteachete;
    private int quantitenette;
    private double montantrecu;
    private double montantverse;
    private double montantnette;
    private String pnt;
    private String pne;




    // Getters / Setters

    public String getId() { return id; }

    public LocalDate getSeance() { return seance; }
    public void setSeance(LocalDate seance) { this.seance = seance; }



    public int getCodeIntermediaire() { return codeIntermediaire; }
    public void setCodeIntermediaire(int codeIntermediaire) { this.codeIntermediaire = codeIntermediaire; }

    public String getIntermediaire() { return intermediaire; }
    public void setIntermediaire(String intermediaire) { this.intermediaire = intermediaire; }

    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }

    public double getCloture() { return cloture; }
    public void setCloture(double cloture) { this.cloture = cloture; }

    public int getQuantiteNette() { return quantitenette; }
    public void setQuantiteNette(int quantitenette) { this.quantitenette = quantitenette; }

    public int getQuantiteVendu() { return quantitevendu; }
    public void setQuantiteVendu(int quantitevendu) { this.quantitevendu = quantitevendu; }

    public int getQuantiteAchete() { return quantiteachete; }
    public void setQuantiteAchete(int quantiteachete) { this.quantiteachete = quantiteachete; }

    public double getMontantRecu() { return montantrecu; }
    public void setMontantRecu(double montantrecu) { this.montantrecu = montantrecu; }

    public double getMontantVerse() { return montantverse; }
    public void setMontantVerse(double montantverse) { this.montantverse = montantverse; }

    public double getMontantNette() { return montantnette; }
    public void setMontantNette(double montantnette) { this.montantnette = montantnette; }



    public String getPnt() { return pnt; }
    public void setPnt(String pnt) { this.pnt = pnt; }

    public String getPne() { return pne; }
    public void setPne(String pne) { this.pne = pne; }


}