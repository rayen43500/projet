package com.fgm.gestion.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilisateur applicatif Mongo : identité + mot de passe (BCrypt) + rôles pour les JWT émis par le backend.
 */
@Data
@Document(collection = "fgm_app_user")
public class FgmAppUser {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String username;
    private String fullName;
    /** Hash BCrypt — jamais renvoyé par l’API. */
    private String passwordHash;
    private List<String> roles = new ArrayList<>();
    /** Renseigné pour le rôle INTERMEDIAIRE (claim JWT {@code intermediaire_code}). */
    private Integer codeIntermediaire;

    public FgmAppUser() {}

    public FgmAppUser(String email, String username, String fullName, List<String> roles, Integer codeIntermediaire) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
        this.codeIntermediaire = codeIntermediaire;
    }
}