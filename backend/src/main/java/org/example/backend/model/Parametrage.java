package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "parametrage")
public class Parametrage {

    @Id
    private String id; 

    private int seuil_var_1 ;
    private double seuil_var_2 ;
    private int seuil_var_3 ;
    private int seuil_dep_pro ;
    private double dep_risq ;
    private int min_contr_init ;
    private int del_reg_liv ;
    private int del_reg_DT ;
    private int del_reg_DE ;
    private double taux;
    private double benefice;
    
   
    public Parametrage() {}

    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSeuil_var_1() {
        return seuil_var_1;
    }

    public void setSeuil_var_1(int seuil_var_1) {
        this.seuil_var_1 = seuil_var_1;
    }

    public double getSeuil_var_2() {
        return seuil_var_2;
    }

    public void setSeuil_var_2(double seuil_var_2) {
        this.seuil_var_2 = seuil_var_2;
    }

    public int getSeuil_var_3() {
        return seuil_var_3;
    }

    public void setSeuil_var_3(int seuil_var_3) {
        this.seuil_var_3 = seuil_var_3;
    }

    public int getSeuil_dep_pro() {
        return seuil_dep_pro;
    }

    public void setSeuil_dep_pro(int seuil_dep_pro) {
        this.seuil_dep_pro = seuil_dep_pro;
    }

    public double getDep_risq() {
        return dep_risq;
    }

    public void setDep_risq(double dep_risq) {
        this.dep_risq = dep_risq;
    }

    public int getMin_contr_init() {
        return min_contr_init;
    }

    public void setMin_contr_init(int min_contr_init) {
        this.min_contr_init = min_contr_init;
    }

    public int getDel_reg_liv() {
        return del_reg_liv;
    }

    public void setDel_reg_liv(int del_reg_liv) {
        this.del_reg_liv = del_reg_liv;
    }

    public int getDel_reg_DT() {
        return del_reg_DT;
    }

    public void setDel_reg_DT(int del_reg_DT) {
        this.del_reg_DT = del_reg_DT;
    }

    public int getDel_reg_DE() {
        return del_reg_DE;
    }

    public void setDel_reg_DE(int del_reg_DE) {
        this.del_reg_DE = del_reg_DE;
    }

    public double getTaux() { return taux; }
    public void setTaux(double taux) { this.taux = taux; }

    public double getBenefice() { return benefice; }
    public void setBenefice(double benefice) { this.benefice = benefice; }

   
}