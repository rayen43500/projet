package com.fgm.gestion.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.fgm.gestion.service.RisqueService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/risque")
public class RisqueController {

    private final JobLauncher jobLauncher;
    private final Job risqueJob;
    private final RisqueService risqueService;

    public RisqueController(JobLauncher jobLauncher, Job risqueJob,RisqueService risqueService) {
        this.jobLauncher = jobLauncher;
        this.risqueJob = risqueJob;
        this.risqueService =risqueService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam String seance) {

        try {
            // validation format yyyyMMdd
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate.parse(seance, formatter); 

            // parametres batch
            JobParameters params = new JobParametersBuilder().addString("seance", seance).addLong("time", System.currentTimeMillis()) .toJobParameters();

            JobExecution execution = jobLauncher.run(risqueJob, params);

            return ResponseEntity.ok(Map.of("message", "Risque genere avec succes","seance", seance,"status", execution.getStatus().toString()));

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(Map.of("error", "Erreur lors de la generation du risque","details", e.getMessage()));
        }
    }
      //  ANNULER SEANCE
    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelSeance(@RequestParam String seance) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(seance, formatter);

           risqueService.deleteBySeance(date);

            return ResponseEntity.ok(Map.of(
                    "message", "risque supprimées pour la séance " + seance
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

      // GET positionnette par séance
@GetMapping("/affiche")
public ResponseEntity<?> getBySeance(@RequestParam String seance) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(seance, formatter);

        var result =  risqueService.getBySeance(date);

        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Aucune positionnette trouvée pour la séance " + seance
            ));
        }

        return ResponseEntity.ok(result);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
        ));
    }
}

@GetMapping("/pdf")
public ResponseEntity<?> generatePdf(@RequestParam String seance) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(seance, formatter);

        byte[] pdf = risqueService.generatePdfBySeance(date);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=risque_" + seance + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
        ));
    }
}
}