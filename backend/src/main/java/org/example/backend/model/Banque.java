package com.fgm.gestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "banque")
public class Banque {

    @Id
    private String id;

    @Field("C_BQUE")
    @JsonProperty("cBque")
    private int cBque;

    @Field("C_BQUE_COMP")
    @JsonProperty("cBqueComp")
    private String cBqueComp;

    @Field("L_COUR_BQUE")
    @JsonProperty("lCourBque")
    private String lCourBque;

    @Field("L_LONG_BQUE")
    @JsonProperty("lLongBque")
    private String lLongBque;

    @Field("ADR_BQUE")
    @JsonProperty("adrBque")
    private String adrBque;

    @Field("FAX_BQUE")
    @JsonProperty("faxBque")
    private String faxBque;

    @Field("BIC")
    @JsonProperty("bic")
    private String bic;

    @Field("Num_CPT")
    @JsonProperty("numCpt")
    private String numCpt;

    // GETTERS & SETTERS 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getcBque() {         
        return cBque;
    }

    public void setcBque(int cBque) {
        this.cBque = cBque;
    }

    public String getcBqueComp() {
        return cBqueComp;
    }

    public void setcBqueComp(String cBqueComp) {
        this.cBqueComp = cBqueComp;
    }

    public String getlCourBque() {
        return lCourBque;
    }

    public void setlCourBque(String lCourBque) {
        this.lCourBque = lCourBque;
    }

    public String getlLongBque() {
        return lLongBque;
    }

    public void setlLongBque(String lLongBque) {
        this.lLongBque = lLongBque;
    }

    public String getAdrBque() {
        return adrBque;
    }

    public void setAdrBque(String adrBque) {
        this.adrBque = adrBque;
    }

    public String getFaxBque() {
        return faxBque;
    }

    public void setFaxBque(String faxBque) {
        this.faxBque = faxBque;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getNumCpt() {
        return numCpt;
    }

    public void setNumCpt(String numCpt) {
        this.numCpt = numCpt;
    }
}