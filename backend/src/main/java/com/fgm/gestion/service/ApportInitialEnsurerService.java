package com.fgm.gestion.service;

import com.fgm.gestion.model.ApportInitial;
import com.fgm.gestion.model.Transaction;
import com.fgm.gestion.repository.ApportInitialRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Garantit un apport initial pour chaque intermédiaire actif avant le batch marge. */
@Service
public class ApportInitialEnsurerService {

    private final ApportInitialRepository apportRepo;

    public ApportInitialEnsurerService(ApportInitialRepository apportRepo) {
        this.apportRepo = apportRepo;
    }

    public void ensureForSeance(LocalDate seance, List<Transaction> transactions) {
        Set<Integer> codes = new HashSet<>();
        for (Transaction t : transactions) {
            if (t.getCodeIntermediaireAcheteur() != 0) codes.add(t.getCodeIntermediaireAcheteur());
            if (t.getCodeIntermediaireVendeur() != 0) codes.add(t.getCodeIntermediaireVendeur());
        }
        for (Integer code : codes) {
            if (apportRepo.findTopByCodeIntermAndSeanceLessThanEqualOrderBySeanceDesc(code, seance).isPresent()) {
                continue;
            }
            ApportInitial a = new ApportInitial();
            a.setSeance(seance);
            a.setCodeInterm(String.valueOf(code));
            a.setApportInitial(0);
            a.setApportInitialAjuste(0);
            apportRepo.save(a);
        }
    }
}
