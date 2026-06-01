package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Document(collection = "swift")
public class Swift {

    @Id
    private String id;

    private String BICfgmbanque;
    private String BICbanquecentral;
    private String BICfgmbankopt;
    private String BICbanquecred;

    private String Numcptbanquecentral;
    private String Numcptbanquecred;

    private String BizMsgIdr;
    private String MsgId;
    private String CdtId;
    private String InstrId;

    private String pacs = "pacs.010.001.02";
    private String BizSvc = "RT";
    private String Prty = "0004";
    private String ccy = "TND";
    private String CtgyPurp = "DDBVM";

    private boolean btchBookg = false;

    private int total = 1000000;
    private int soldenette;
    private int Nbretat;
    private int Prtry = 85;

    private LocalDateTime datecreation;
    private LocalDate dateseance;
    private LocalDate datevaleur;

    // ===== GETTERS / SETTERS =====

    public boolean isBtchBookg() { return btchBookg; }
    public void setBtchBookg(boolean btchBookg) { this.btchBookg = btchBookg; }

    public String getBizSvc() { return BizSvc; }
    public void setBizSvc(String bizSvc) { BizSvc = bizSvc; }

    public String getPacs() { return pacs; }
    public void setPacs(String pacs) { this.pacs = pacs; }

    public String getPrty() { return Prty; }
    public void setPrty(String prty) { Prty = prty; }

    public String getCcy() { return ccy; }
    public void setCcy(String ccy) { this.ccy = ccy; }

    public int getPrtry() { return Prtry; }
    public void setPrtry(int prtry) { Prtry = prtry; }

    public String getCtgyPurp() { return CtgyPurp; }
    public void setCtgyPurp(String ctgyPurp) { CtgyPurp = ctgyPurp; }

    public String getBICfgmbanque() { return BICfgmbanque; }
    public void setBICfgmbanque(String BICfgmbanque) { this.BICfgmbanque = BICfgmbanque; }

    public String getBICbanquecentral() { return BICbanquecentral; }
    public void setBICbanquecentral(String BICbanquecentral) { this.BICbanquecentral = BICbanquecentral; }

    public String getBICbanquecred() { return BICbanquecred; }
    public void setBICbanquecred(String BICbanquecred) { this.BICbanquecred = BICbanquecred; }

    public String getNumcptbanquecentral() { return Numcptbanquecentral; }
    public void setNumcptbanquecentral(String num) { this.Numcptbanquecentral = num; }

    public String getNumcptbanquecred() { return Numcptbanquecred; }
    public void setNumcptbanquecred(String num) { this.Numcptbanquecred = num; }

    public String getInstrId() { return InstrId; }
    public void setInstrId(String instrId) { InstrId = instrId; }

    public String getCdtId() { return CdtId; }
    public void setCdtId(String cdtId) { CdtId = cdtId; }

    public String getMsgId() { return MsgId; }
    public void setMsgId(String msgId) { MsgId = msgId; }

    public String getBizMsgIdr() { return BizMsgIdr; }
    public void setBizMsgIdr(String bizMsgIdr) { BizMsgIdr = bizMsgIdr; }

    public int getSoldenette() { return soldenette; }
    public void setSoldenette(int soldenette) { this.soldenette = soldenette; }

    public int getNbretat() { return Nbretat; }
    public void setNbretat(int nbretat) { Nbretat = nbretat; }

    // FIX: added missing getter/setter for dateseance (needed by deleteByDateseance)
    public LocalDate getDateseance() { return dateseance; }
    public void setDateseance(LocalDate dateseance) { this.dateseance = dateseance; }

    public LocalDateTime getDatecreation() { return datecreation; }
    public void setDatecreation(LocalDateTime datecreation) { this.datecreation = datecreation; }

    public LocalDate getDatevaleur() { return datevaleur; }
    public void setDatevaleur(LocalDate datevaleur) { this.datevaleur = datevaleur; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getBICfgmbankopt() { return BICfgmbankopt; }
    public void setBICfgmbankopt(String BICfgmbankopt) { this.BICfgmbankopt = BICfgmbankopt; }
}