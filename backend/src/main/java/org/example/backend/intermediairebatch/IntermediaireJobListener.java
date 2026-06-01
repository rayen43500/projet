package com.fgm.gestion.intermediairebatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;
import com.fgm.gestion.repository.IntermediaireRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class IntermediaireJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(IntermediaireJobListener.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final IntermediaireRepository repository;

    public IntermediaireJobListener(IntermediaireRepository repository) {
        this.repository = repository;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String dateSeanceIso = jobExecution.getJobParameters().getString("dateSeance");
        LocalDate importDate = (dateSeanceIso != null && !dateSeanceIso.isBlank())
                ? LocalDate.parse(dateSeanceIso, ISO)
                : LocalDate.now();
        long count = repository.findByDateImport(importDate).size();
        log.info("[INTERMEDIAIRE-LISTENER] Début du job — suppression de {} enregistrements pour {}", count, importDate);
        repository.deleteByDateImport(importDate);
        log.info("[INTERMEDIAIRE-LISTENER] Suppression terminée");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long saved = repository.count();
        log.info("[INTERMEDIAIRE-LISTENER] Job terminé - statut: {}, {} enregistrements en base",
                jobExecution.getStatus(), saved);
        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            jobExecution.getAllFailureExceptions()
                    .forEach(ex -> log.error("[INTERMEDIAIRE-LISTENER] Exception: {}", ex.getMessage(), ex));
        }
    }
}