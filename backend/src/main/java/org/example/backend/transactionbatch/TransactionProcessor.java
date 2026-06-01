package com.fgm.gestion.transactionbatch;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.fgm.gestion.model.Transaction;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class TransactionProcessor implements ItemProcessor<DataLine, Transaction> {

    private static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("#{jobParameters['seanceCompact']}")
    private String seanceCompact;

    @Override
    public Transaction process(DataLine d) {

        Transaction t = new Transaction();

        LocalDate seance;
        if (seanceCompact != null && !seanceCompact.isBlank()) {
            seance = LocalDate.parse(seanceCompact, COMPACT);
        } else {
            seance = LocalDate.parse(d.seance(), COMPACT);
        }

        t.setSeance(seance);

        t.setCodeValeur(d.codeValeur());
        t.setLibelleValeur(d.produit());

        t.setCodeIntermediaireAcheteur(d.code_ach());
        t.setLibelleIntermediaireAcheteur(d.acheteur());

        t.setCodeIntermediaireVendeur(d.code_vend());
        t.setLibelleIntermediaireVendeur(d.vendeur());

        t.setQuantiteNegociee(d.quantite());
        t.setCoursTransaction(d.coursTransaction());
        t.setVolume(d.prixTotal());

        return t;
    }
}