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
import com.fgm.gestion.service.ValeurService;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/valeur")
public class ValeurController {

    private final JobLauncher jobLauncher;
    private final Job valeurJob;
    private final ValeurService valeurService;

    public ValeurController(JobLauncher jobLauncher, Job valeurJob,  ValeurService valeurService) {
        this.jobLauncher = jobLauncher;
        this.valeurJob = valeurJob;
        this.valeurService = valeurService;
        
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {

        try {
            String path = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
            file.transferTo(new File(path));

            JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).addString("filePath", path).toJobParameters();

            jobLauncher.run(valeurJob, params);

            return ResponseEntity.ok(Map.of("message", "Batch Valeur lance"));

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

            valeurService.deleteBySeance(date);

            return ResponseEntity.ok(Map.of(
                    "message", "Valeurs supprimées pour la séance " + seance
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

   @GetMapping("/seance/{seance}")
public ResponseEntity<?> getValeursBySeance(@PathVariable String seance) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(seance, formatter);

        // récupérer les données
       var valeurs = valeurService.findBySeance(date);

        // LOG backend (très important)
        System.out.println("Nombre de valeurs trouvées : " + valeurs.size());

        // retourner directement la liste
        return ResponseEntity.ok(valeurs);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
        ));
    }
}

    
}