package com.fgm.gestion.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "monitoring")
public class Monitoring {

    @Id
    private String id;

    private String nomFichier; // transaction
    private LocalDate date;
    private String type; // transaction / valeur / intermediaire
    private String status; // TRAITE / NON_TRAITE

    // ===== GETTERS =====
    public String getId() { return id; }

    public String getNomFichier() { return nomFichier; }

    public LocalDate getDate() { return date; }

    public String getType() { return type; }

    public String getStatus() { return status; }

    // ===== SETTERS =====
    public void setId(String id) { this.id = id; }

    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public void setDate(LocalDate date) { this.date = date; }

    public void setType(String type) { this.type = type; }

    public void setStatus(String status) { this.status = status; }
}