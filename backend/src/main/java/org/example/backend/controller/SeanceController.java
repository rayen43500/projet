package com.fgm.gestion.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.fgm.gestion.service.SeanceService;
import com.fgm.gestion.service.FgmDashboardService;
import com.fgm.gestion.model.*;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/seance")
public class SeanceController {

   
    private final SeanceService seanceService;
    private final FgmDashboardService dashboardService;

    public SeanceController(SeanceService seanceService, FgmDashboardService dashboardService) {
       
        this.seanceService = seanceService;
        this.dashboardService = dashboardService;
        
    }

    @GetMapping("/exists")
public ResponseEntity<?> existsSeance(@RequestParam String seance) {

    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(seance, formatter);

        boolean exists = seanceService.existsBySeance(date);

        return ResponseEntity.ok(Map.of(
                "exists", exists
        ));

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Format de date invalide"
        ));
    }
}

    
      //  ANNULER SEANCE
    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelSeance(@RequestParam String seance) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(seance, formatter);

           seanceService.deleteBySeance(date);

            return ResponseEntity.ok(Map.of(
                    "message", "seance supprimées pour la séance " + seance
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // GET SEANCE COURANTE (OUVERTE) — used by intermediaire component
    @GetMapping("/courante")
    public ResponseEntity<?> getSeanceCourante() {
        try {
            return seanceService.getAllSeances().stream()
                    .filter(s -> "OUVERTE".equals(s.getStatut()))
                    .findFirst()
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.ok(null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET ALL SEANCES
@GetMapping("/all")
public ResponseEntity<?> getAllSeances() {
    try {
        return ResponseEntity.ok(seanceService.getAllSeances());
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
        ));
    }
}


@PostMapping("/create")
public ResponseEntity<?> createSeance(@RequestBody Map<String, String> body) {

    String seanceString = body.get("seance"); 

    Seance saved = seanceService.createSeance(seanceString);

    return ResponseEntity.ok(saved);
}

    /** Alias legacy pour le frontend Angular */
    @PostMapping("/cloturer")
    public ResponseEntity<?> cloturerLegacy(@RequestBody Map<String, String> body) {
        try {
            String compact = body.get("seance");
            if (compact == null || compact.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "seance requis"));
            }
            String iso = LocalDate.parse(compact, DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
            seanceService.cloturerFromIso(iso);
            return ResponseEntity.ok(Map.of("message", "Séance clôturée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/annuler")
    public ResponseEntity<?> annulerLegacy(@RequestBody Map<String, String> body) {
        try {
            String compact = body.get("seance");
            String motif = body.getOrDefault("motif", "");
            if (compact == null || compact.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "seance requis"));
            }
            String iso = LocalDate.parse(compact, DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
            seanceService.annulerFromIso(iso, motif);
            return ResponseEntity.ok(Map.of("message", "Séance annulée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/anomalies")
    public ResponseEntity<?> anomaliesLegacy(@RequestBody Map<String, String> body) {
        try {
            String compact = body.get("seance");
            if (compact == null || compact.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "seance requis"));
            }
            String iso = LocalDate.parse(compact, DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
            int n = dashboardService.countAnomalies(dashboardService.parseIso(iso));
            return ResponseEntity.ok(Map.of("nbAnomalies", n, "message", n == 0 ? "Aucune anomalie" : n + " position(s) à risque"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}