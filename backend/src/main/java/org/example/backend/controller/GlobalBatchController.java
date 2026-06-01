package com.fgm.gestion.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fgm.gestion.service.*;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/global")
public class GlobalBatchController {

    private final JobLauncher jobLauncher;

    private final Job transactionJob;
    private final Job intermediaireJob;
    private final Job valeurJob;
    private final Job positionnetteJob;
    private final Job risqueJob;
    private final Job mouvementBancaireJob;
    private final MouvementBancaireService mouvementBancaireService;
    private final RisqueService risqueService;
    private final PositionnetteService positionnetteService;
    private final TransactionService transactionService;
    private final ValeurService valeurService;
    private final SeanceService seanceService;
    private final BanqueEtatService banqueEtatService;
    private final  SwiftService swiftService;
    private final MonitoringService monitoringService;
    private final HistoryService historyService;
    private final ProvisionService provisionService;
    private final AppelRestitutionService appelRestitutionService;
    private final MvtBanqueInterService mvtBanqueInterService;


    public GlobalBatchController(
            JobLauncher jobLauncher,
            Job transactionJob,
            Job intermediaireJob,
            Job valeurJob,
            Job positionnetteJob,
            Job risqueJob,
            Job mouvementBancaireJob,
            MouvementBancaireService mouvementBancaireService,
            RisqueService risqueService,
            PositionnetteService positionnetteService,
            TransactionService transactionService,
            ValeurService valeurService,
            SeanceService seanceService,
            BanqueEtatService banqueEtatService,
            SwiftService swiftService,
            MonitoringService monitoringService,
            HistoryService historyService,
            ProvisionService provisionService,
            AppelRestitutionService appelRestitutionService,
            MvtBanqueInterService mvtBanqueInterService) {

        this.jobLauncher = jobLauncher;
        this.transactionJob = transactionJob;
        this.intermediaireJob = intermediaireJob;
        this.valeurJob = valeurJob;
        this.positionnetteJob = positionnetteJob;
        this.risqueJob = risqueJob;
        this.mouvementBancaireJob = mouvementBancaireJob;

        this.mouvementBancaireService = mouvementBancaireService;
        this.risqueService = risqueService;
        this.positionnetteService = positionnetteService;
        this.transactionService = transactionService;
        this.valeurService = valeurService;
        this.seanceService = seanceService;
        this.banqueEtatService = banqueEtatService;
        this.swiftService = swiftService;
        this.historyService = historyService;
        this.monitoringService = monitoringService;
        this.provisionService = provisionService;
        this.appelRestitutionService = appelRestitutionService;
        this.mvtBanqueInterService = mvtBanqueInterService;

    }
    @PostMapping("/run-all")
    public ResponseEntity<?> runAll(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam(value = "file2", required = false) MultipartFile file2,
            @RequestParam("file3") MultipartFile file3,
            @RequestParam String seance) {

        try {

            // FILES
            String tmpDir = System.getProperty("java.io.tmpdir");

            String path1 = tmpDir + "/" + file1.getOriginalFilename();
            String path3 = tmpDir + "/" + file3.getOriginalFilename();

            file1.transferTo(new File(path1));
            file3.transferTo(new File(path3));

            // TRANSACTION
            jobLauncher.run(transactionJob, new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("filePath", path1)
                    .toJobParameters());

            // INTERMEDIAIRE
            if (file2 != null && !file2.isEmpty()) {
                String path2 = tmpDir + "/" + file2.getOriginalFilename();
                file2.transferTo(new File(path2));
                jobLauncher.run(intermediaireJob, new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .addString("filePath", path2)
                        .addString("seance", seance)
                        .toJobParameters());
            }

            // VALEUR
            jobLauncher.run(valeurJob, new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("filePath", path3)
                    .toJobParameters());

            // VALIDATION SEANCE
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date =LocalDate.parse(seance, formatter);

            JobParameters params = new JobParametersBuilder()
                    .addString("seance", seance)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            // POSITIONNETTE
            jobLauncher.run(positionnetteJob, params);

            // RISQUE
            jobLauncher.run(risqueJob, params);

            //APPEL MARGE
            jobLauncher.run(mouvementBancaireJob, params);

            // BANQUE ETAT
            banqueEtatService.generateFromMouvementBancaire(seance);


            appelRestitutionService.genererAppelRestitution(date);

            // MVT BANQUE INTERMEDIAIRE
            mvtBanqueInterService.genererMvtBanque(date);

            return ResponseEntity.ok(Map.of(
                    "message", "Tous les batchs sont lances avec succes"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur globale",
                    "details", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelAll(@RequestParam String seance) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(seance, formatter);


            mouvementBancaireService.deleteBySeance(date);
            risqueService.deleteBySeance(date);
            positionnetteService.deleteBySeance(date);
            transactionService.deleteBySeance(date);
            valeurService.deleteBySeance(date);
            seanceService.deleteBySeance(date);
            banqueEtatService.deleteBySeance(date);
            monitoringService.deleteByDate(date);
            historyService.deleteByDate(date);
            swiftService.deleteByDateseance(date);
            provisionService.deleteByDateCalcul(date);
            appelRestitutionService.deleteBySeance(date);
            mvtBanqueInterService.deleteBySeance(date);

            return ResponseEntity.ok(Map.of(
                    "message", "Seance " + seance + " annulee"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Erreur lors de l'annulation",
                    "details", e.getMessage()
            ));
        }
    }
}