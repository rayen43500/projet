package com.fgm.gestion.model;
import java.time.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "banqueetat")
public class BanqueEtat {

    @Id
    private String id;

    private String banque;
    private int type;
    private int code;
    private LocalDate seance;
    private LocalDate seancevaleur;
    private int debitNbr;
    private int montantDebit;

    private int creditNbr;
    private int montantCredit;

    private int solde;

    // GETTERS & SETTERS
    public String getId() { return id; }
    public String getBanque() { return banque; }
    public int getType() { return type; }
    public int getCode() { return code; }
    public int getDebitNbr() { return debitNbr; }
    public int getMontantDebit() { return montantDebit; }
    public int getCreditNbr() { return creditNbr; }
    public int getMontantCredit() { return montantCredit; }
    public int getSolde() { return solde; }
    public LocalDate getSeance(){return seance;}
    public LocalDate getSeanceValeur(){return seancevaleur;}


    public void setId(String id) { this.id = id; }
    public void setBanque(String banque) { this.banque = banque; }
    public void setType(int type) { this.type = type; }
    public void setCode(int code) { this.code = code; }
    public void setDebitNbr(int debitNbr) { this.debitNbr = debitNbr; }
    public void setMontantDebit(int montantDebit) { this.montantDebit = montantDebit; }
    public void setCreditNbr(int creditNbr) { this.creditNbr = creditNbr; }
    public void setMontantCredit(int montantCredit) { this.montantCredit = montantCredit; }
    public void setSolde(int solde) { this.solde = solde; }
    public void setSeance(LocalDate seance){    this.seance=seance;}
    public void setSeanceValeur(LocalDate seancevaleur ){this.seancevaleur=seancevaleur;}

}