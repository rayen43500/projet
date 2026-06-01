package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "history")
public class History {

    @Id
    private String id;

    private String nomFichier; 
    private LocalDate date;
    private String type; 

    // ===== GETTERS =====
    public String getId() { return id; }

    public String getNomFichier() { return nomFichier; }

    public LocalDate getDate() { return date; }

    public String getType() { return type; }


    // ===== SETTERS =====
    public void setId(String id) { this.id = id; }

    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public void setDate(LocalDate date) { this.date = date; }

    public void setType(String type) { this.type = type; }

}