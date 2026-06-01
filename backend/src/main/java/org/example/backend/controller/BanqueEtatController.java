package com.fgm.gestion.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.*;
import java.time.format.*;
import com.fgm.gestion.model.BanqueEtat;
import com.fgm.gestion.service.BanqueEtatService;

@RestController
@RequestMapping("/api/banqueetat")
public class BanqueEtatController {

    private final BanqueEtatService banqueEtatService;

    public BanqueEtatController(BanqueEtatService banqueEtatService) {
        this.banqueEtatService = banqueEtatService;
    }

  @PostMapping("/generate")
public List<BanqueEtat> generate(@RequestParam String seance) {
    return banqueEtatService.generateFromMouvementBancaire(seance);
}

        @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelSeance(@RequestParam String seance) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(seance, formatter);

             banqueEtatService.deleteBySeance(date);

            return ResponseEntity.ok(Map.of(
                    "message", "banque Etat  supprimées pour la séance " + seance
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }


    @GetMapping
public ResponseEntity<?> getBySeance(@RequestParam String seance) {

    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(seance, formatter);

        List<BanqueEtat> result = banqueEtatService.getBySeance(date);

        return ResponseEntity.ok(result);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Format de date invalide (yyyyMMdd attendu)"
        ));
    }
}
}