package com.fgm.gestion.valeurbatch;
import com.fgm.gestion.service.*;
import com.fgm.gestion.model.Valeur;
import com.fgm.gestion.repository.ValeurRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.fgm.gestion.service.*;

@Component
public class ValeurWriter implements ItemWriter<Valeur> {

    private final ValeurRepository repository;
    private final MonitoringService monitoringService;

    public ValeurWriter(ValeurRepository repository,
                        MonitoringService monitoringService) {
        this.repository = repository;
        this.monitoringService = monitoringService;
    }

    @Override
    public void write(Chunk<? extends Valeur> chunk) {

        List<Valeur> items = new ArrayList<>(chunk.getItems());

        if (items.isEmpty()) return;

        //  récupérer la date depuis les items
        LocalDate seance = items.get(0).getSeance();

        // Supprimer les valeurs existantes pour cette séance (evite accumulation)
        repository.deleteBySeance(seance);

        repository.saveAll(items);

        // ajouter monitoring
        monitoringService.saveMonitoring(
                "valeur_" + seance,
                seance,
                "TRAITE"
        );
    }
}