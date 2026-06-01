package com.fgm.gestion.controller;

import com.fgm.gestion.model.*;
import com.fgm.gestion.service.MvtBanqueInterService;
import org.springframework.web.bind.annotation.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/mvt-banque")
public class MvtBanqueInterController {

    private final MvtBanqueInterService service;
    private final JobLauncher jobLauncher;
    private final Job mouvementBancaireJob;

    public MvtBanqueInterController(MvtBanqueInterService service,JobLauncher jobLauncher,
                                    Job mouvementBancaireJob) {
        this.service = service;
        this.jobLauncher = jobLauncher;
        this.mouvementBancaireJob = mouvementBancaireJob;
    }

    @PostMapping
    public MvtBanqueInter create(@RequestBody MvtBanqueInter obj) {
        return service.save(obj);
    }

    @GetMapping
    public List<MvtBanqueInter> getAll() {
        return service.getAll();
    }

    @GetMapping("/seance")
    public List<MvtBanqueInter> getBySeance(@RequestParam String date) {
        LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return service.getBySeance(d);
    }

    @DeleteMapping
    public void deleteAll() {
        service.deleteAll();
    }

@PostMapping("/generate")
public String generate(@RequestParam String seance) {

    try {
        JobParameters params = new JobParametersBuilder()
                .addString("seance", seance)
                .addLong("time", System.currentTimeMillis()) // obligatoire
                .toJobParameters();

        jobLauncher.run(mouvementBancaireJob, params);

        return "Batch Mouvement Bancaire lancé avec succès";

    } catch (Exception e) {
        e.printStackTrace();
        return "Erreur lors du lancement du batch";
    }
}
}