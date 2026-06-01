package com.fgm.gestion.controller;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.fgm.gestion.service.PositionnetteService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/positionnette")
public class PositionnetteController {

    private final JobLauncher jobLauncher;
    private final Job positionnetteJob;
    private final PositionnetteService positionnetteService;

    public PositionnetteController(JobLauncher jobLauncher, Job positionnetteJob, PositionnetteService positionnetteService) {
        this.jobLauncher = jobLauncher;
        this.positionnetteJob = positionnetteJob;
        this.positionnetteService = positionnetteService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam String seance) {

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("seance", seance)
                    .toJobParameters();

            jobLauncher.run(positionnetteJob, params);

            return ResponseEntity.ok(Map.of("message", "Positionnette generee"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
      //  ANNULER SEANCE
    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelSeance(@RequestParam String seance) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(seance, formatter);

            positionnetteService.deleteBySeance(date);

            return ResponseEntity.ok(Map.of(
                    "message", "positionette supprimées pour la séance " + seance
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

        var result = positionnetteService.getBySeance(date);

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

        byte[] pdf = positionnetteService.generatePdfBySeance(date);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=positionnette_" + seance + ".pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
        ));
    }
}
}