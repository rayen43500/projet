package com.fgm.gestion.controller;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.batch.core.Job;
import java.util.Map;
import com.fgm.gestion.service.TransactionService;
import java.util.Map;
import com.fgm.gestion.model.*;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final JobLauncher jobLauncher;
    private final Job transactionJob;
    private final TransactionService transactionService;

    public TransactionController(JobLauncher jobLauncher, Job transactionJob, TransactionService transactionService) {
        this.jobLauncher = jobLauncher;
        this.transactionJob = transactionJob;
        this.transactionService =transactionService;
    }

    @PostMapping("/upload")
    //Recupere le fichier envoye depuis un formulaire
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {

        try {
           
           //java.io.tmpdir est un dossier temporaire du systeme
            String path = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
           
           //Sauvegarde du fichier uploade sur le disque local.
            file.transferTo(new java.io.File(path));

            JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).addString("filePath", path).toJobParameters();
                    
                    

            jobLauncher.run(transactionJob, params);

            return ResponseEntity.ok(Map.of("message", "Batch lance avec succes"));

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

            transactionService.deleteBySeance(date);

            return ResponseEntity.ok(Map.of(
                    "message", "transactions supprimées pour la séance " + seance
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

     @GetMapping("/seance/{seance}")
public ResponseEntity<?> gettransactionsBySeance(@PathVariable String seance) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(seance, formatter);

        // récupérer les données
       var transactions = transactionService.findBySeance(date);

        // LOG backend (très important)
        System.out.println("Nombre de transactions trouvées : " + transactions.size());

        // retourner directement la liste
        return ResponseEntity.ok(transactions);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
        ));
    }
}
}