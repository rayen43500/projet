package com.fgm.gestion.controller;

import com.fgm.gestion.model.MouvementBancaire;
import com.fgm.gestion.repository.MouvementBancaireRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/mouvementbancaire")
public class MouvementBancaireController {

    private final MouvementBancaireRepository repo;
    private final JobLauncher jobLauncher;
    private final Job mouvementBancaireJob;

    public MouvementBancaireController(MouvementBancaireRepository repo,
                                        JobLauncher jobLauncher,
                                        Job mouvementBancaireJob) {
        this.repo = repo;
        this.jobLauncher = jobLauncher;
        this.mouvementBancaireJob = mouvementBancaireJob;
    }

    @GetMapping("/affiche")
    public ResponseEntity<List<MouvementBancaire>> affiche(@RequestParam String seance) {
        LocalDate d = LocalDate.parse(seance, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return ResponseEntity.ok(repo.findBySeance(d));
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> run(@RequestParam String seance) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("seance", seance)
                    .addLong("ts", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(mouvementBancaireJob, params);
            return ResponseEntity.ok(Map.of("statut", "OK", "seance", seance));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("erreur", e.getMessage()));
        }
    }
}
