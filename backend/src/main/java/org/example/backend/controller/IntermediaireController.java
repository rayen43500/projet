package com.fgm.gestion.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fgm.gestion.service.IntermediaireService;
import java.io.File;
import java.util.Map;
import java.time.*;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/intermediaire")
public class IntermediaireController {

    private final JobLauncher jobLauncher;
    private final Job intermediaireJob;
    private final IntermediaireService intermediaireService;

    public IntermediaireController(JobLauncher jobLauncher, Job intermediaireJob,IntermediaireService intermediaireService) {
        this.jobLauncher = jobLauncher;
        this.intermediaireJob = intermediaireJob;
        this.intermediaireService= intermediaireService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {

        try {
            String path = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
            file.transferTo(new File(path));

            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("filePath", path)
                    .addString("dateSeance", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .toJobParameters();

            jobLauncher.run(intermediaireJob, params);

            return ResponseEntity.ok(Map.of("message", "Batch Intermediaire lance"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllIntermediaires() {
        try {
            // Return the most recently imported batch of intermediaires
            var all = intermediaireService.findMostRecent();
            return ResponseEntity.ok(all);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/seance/{seance}")
public ResponseEntity<?> getValeursBySeance(@PathVariable String seance) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(seance, formatter);

        // récupérer les données
       var intermediaires =intermediaireService.findByDateImport(date);

        // LOG backend (très important)
        System.out.println("Nombre deIntermediaires trouvées : " +intermediaires.size());

        // retourner directement la liste
        return ResponseEntity.ok(intermediaires);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
        ));
    }
}


}