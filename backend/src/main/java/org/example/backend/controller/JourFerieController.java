package com.fgm.gestion.controller;
import java.time.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.fgm.gestion.model.JourFerie;
import com.fgm.gestion.service.JourFerieService;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@RestController
@RequestMapping("/api/JourFerie")
public class JourFerieController {

    private final JourFerieService JourFerieService;

    public JourFerieController(JourFerieService JourFerieService) {
        this.JourFerieService = JourFerieService;
    }

    @PostMapping
    public JourFerie create(@RequestBody JourFerie JourFerie) {
        return JourFerieService.save(JourFerie);
    }

    @PostMapping("/bulk")
    public List<JourFerie> createAll(@RequestBody List<JourFerie> JourFeries) {
        return JourFerieService.saveAll(JourFeries);
    }

    @GetMapping
    public List<JourFerie> getAll() {
        return JourFerieService.getAll();
    }

    
    @GetMapping("/byJour")
    public List<JourFerie> getByCode(@RequestParam LocalDate jour) {
        return JourFerieService.findByJour(jour);
    }

     // DELETE ALL
    @DeleteMapping
    public String deleteAll() {
        JourFerieService.deleteAll();
        return "Toutes les Service supprimées";
    }

   @PostMapping("/upload")
public String uploadFile(@RequestParam("file") MultipartFile file) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());   

      

        List<JourFerie> jourFeries = Arrays.asList(
            mapper.readValue(file.getInputStream(), JourFerie[].class)
        );

        if (jourFeries.isEmpty()) {
            return "Le fichier JSON est vide ";
        }

        JourFerieService.saveAll(jourFeries);

        return "Fichier JSON enregistré avec succès  (" + jourFeries.size() + " jours fériés ajoutés)";

    } catch (Exception e) {
        e.printStackTrace();  
        return "Erreur lors de l'upload  : " + e.getMessage();
    }
}
}