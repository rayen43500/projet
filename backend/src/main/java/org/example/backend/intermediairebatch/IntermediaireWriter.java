package com.fgm.gestion.intermediairebatch;

import com.fgm.gestion.repository.IntermediaireRepository;
import com.fgm.gestion.service.MonitoringService;
import com.fgm.gestion.model.Intermediaire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@StepScope
public class IntermediaireWriter implements ItemWriter<Intermediaire> {

    private static final Logger log = LoggerFactory.getLogger(IntermediaireWriter.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final IntermediaireRepository repository;
    private final MonitoringService monitoringService;

    @Value("#{jobParameters['dateSeance']}")
    private String dateSeanceIso;

    public IntermediaireWriter(IntermediaireRepository repository,
                               MonitoringService monitoringService) {
        this.repository = repository;
        this.monitoringService = monitoringService;
    }

    @Override
    public void write(Chunk<? extends Intermediaire> chunk) {

        List<Intermediaire> items = new ArrayList<>(chunk.getItems());

        log.info("[INTERMEDIAIRE-WRITER] Chunk reçu: {} items", items.size());

        if (items.isEmpty()) {
            log.warn("[INTERMEDIAIRE-WRITER] Chunk vide, rien à sauvegarder");
            return;
        }

        LocalDate seanceDate = (dateSeanceIso != null && !dateSeanceIso.isBlank())
                ? LocalDate.parse(dateSeanceIso, ISO)
                : LocalDate.now();
        log.info("[INTERMEDIAIRE-WRITER] Date import: {}", seanceDate);

        for (Intermediaire i : items) {
            i.setNomFichier("batch-intermediaire");
            i.setDateImport(seanceDate);
        }

        // Un seul enregistrement par code pour cette séance (évite doublons Mongo)
        Map<Integer, Intermediaire> byCode = new LinkedHashMap<>();
        for (Intermediaire i : items) {
            if (i.getCodeIntermediaire() != 0) {
                byCode.put(i.getCodeIntermediaire(), i);
            }
        }
        List<Intermediaire> deduped = new ArrayList<>(byCode.values());

        log.info("[INTERMEDIAIRE-WRITER] Sauvegarde de {} intermédiaires en base...", deduped.size());
        repository.saveAll(deduped);
        log.info("[INTERMEDIAIRE-WRITER] Sauvegarde OK");

        monitoringService.saveMonitoring(
                "intermediaire_" + seanceDate,
                seanceDate,
                "TRAITE"
        );
    }
}