package com.fgm.gestion.transactionbatch;
import java.time.LocalDate;
import com.fgm.gestion.model.Transaction;
import com.fgm.gestion.repository.TransactionRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;
import com.fgm.gestion.service.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionWriter implements ItemWriter<Transaction> {

    private final TransactionRepository transactionRepository;
    private final MonitoringService monitoringService;

    public TransactionWriter(TransactionRepository transactionRepository,
                             MonitoringService monitoringService) {
        this.transactionRepository = transactionRepository;
        this.monitoringService = monitoringService;
    }
    @Override
    public void write(Chunk<? extends Transaction> chunk) {

        List<Transaction> items = new ArrayList<>(chunk.getItems());

        if (items.isEmpty()) return;

        //  récupérer la date depuis les items
        LocalDate seance = items.get(0).getSeance();

        // Supprimer les transactions existantes pour cette séance (evite accumulation)
        transactionRepository.deleteBySeance(seance);

        // sauvegarde
        transactionRepository.saveAll(items);

        // ajouter monitoring
        monitoringService.saveMonitoring(
                "transaction_" + seance,
                seance,
                "TRAITE"
        );
    }
}