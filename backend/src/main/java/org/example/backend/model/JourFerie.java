package com.fgm.gestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;


@Document(collection = "jourferie")
public class JourFerie {

    @Id
    private String id;

    @Field("JOUR")
    @JsonProperty("JOUR")
    @JsonFormat(pattern = "yyyy-MM-dd") 
    private LocalDate jour;

   

    // GETTERS & SETTERS  
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getJour() {          
        return jour;
    }

    public void setJour(LocalDate jour) {
        this.jour = jour;
    }

   
}