package com.fgm.gestion.service;

import com.fgm.gestion.model.Intermediaire;
import com.fgm.gestion.repository.IntermediaireRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Résout un intermédiaire par code sans erreur « non unique result » (plusieurs imports / dates). */
@Service
public class IntermediaireLookupService {

    private final IntermediaireRepository intermediaireRepository;

    public IntermediaireLookupService(IntermediaireRepository intermediaireRepository) {
        this.intermediaireRepository = intermediaireRepository;
    }

    public Intermediaire findForSeance(int codeIntermediaire, LocalDate seance) {
        if (codeIntermediaire == 0 || seance == null) return null;

        List<Intermediaire> exact = intermediaireRepository
                .findByCodeIntermediaireAndDateImport(codeIntermediaire, seance);
        if (!exact.isEmpty()) {
            return exact.get(exact.size() - 1);
        }

        return intermediaireRepository
                .findTopByCodeIntermediaireAndDateImportLessThanEqualOrderByDateImportDesc(
                        codeIntermediaire, seance)
                .orElse(null);
    }

    public String resolveLibelleCourt(int codeIntermediaire, LocalDate seance) {
        Intermediaire i = findForSeance(codeIntermediaire, seance);
        if (i == null) return "";
        String court = i.getLibelleCourt();
        if (court != null && !court.isBlank()) return court.trim();
        String lon = i.getLibelleLong();
        return lon != null ? lon.trim() : "";
    }
}
