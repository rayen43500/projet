
package com.fgm.gestion.service;

import com.fgm.gestion.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDate;
import com.fgm.gestion.model.*;
import java.time.format.DateTimeFormatter;

@Service
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final ParametrageRepository parametrageRepository;
    private final JourFerieRepository jourFerieRepository;

   public SeanceService(SeanceRepository seanceRepository,
                     ParametrageRepository parametrageRepository,
                     JourFerieRepository jourFerieRepository) {

    this.seanceRepository = seanceRepository;
    this.parametrageRepository = parametrageRepository;
    this.jourFerieRepository = jourFerieRepository;
}

private LocalDate parseSeance(String seance) {
    return LocalDate.parse(seance, DateTimeFormatter.ofPattern("yyyyMMdd"));
}

    public LocalDate parseSeanceIso(String isoDate) {
        return LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String toSeanceCompact(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /** Une séance par date — évite l'erreur Mongo « non unique result ». */
    public Optional<Seance> findOneBySeance(LocalDate seance) {
        return seanceRepository.findTopBySeanceOrderByIdDesc(seance);
    }

    public boolean existsBySeance(LocalDate seance) {
        return seanceRepository.existsBySeance(seance);
    }

    /** Supprime les doublons historiques pour une même date de séance. */
    public void dedupeSeances(LocalDate seance) {
        List<Seance> all = seanceRepository.findAllBySeance(seance);
        if (all.size() <= 1) return;
        Seance keep = all.get(all.size() - 1);
        for (Seance s : all) {
            if (!Objects.equals(s.getId(), keep.getId())) {
                seanceRepository.deleteById(s.getId());
            }
        }
    }

    public void deleteBySeance(LocalDate seance) {
        seanceRepository.deleteAllBySeance(seance);
    }

    /** Liste dédoublonnée (une entrée par date). */
    public List<Seance> getAllSeancesDistinct() {
        Map<LocalDate, Seance> byDate = new TreeMap<>(Comparator.reverseOrder());
        for (Seance s : seanceRepository.findAll()) {
            if (s.getSeance() != null) {
                byDate.putIfAbsent(s.getSeance(), s);
            }
        }
        return new ArrayList<>(byDate.values());
    }

    public List<Seance> getAllSeances() {
        return getAllSeancesDistinct();
    }

public List<Seance> getSeancesBetween(LocalDate debut, LocalDate fin) {
    return seanceRepository.findBySeanceBetween(debut, fin);
}

    public Seance createSeance(String seanceStr) {
        LocalDate dateSeance = parseSeance(seanceStr);
        dedupeSeances(dateSeance);
        Optional<Seance> existing = findOneBySeance(dateSeance);
        if (existing.isPresent()) {
            return existing.get();
        }

        Parametrage param = parametrageRepository.findAll().stream().findFirst().orElse(null);
        int delai = param != null ? param.getDel_reg_liv() : 2;
        if (delai < 0) {
            delai = 2;
        }

        LocalDate dateLivraison = calculerDateLivraison(dateSeance, delai);

        Seance seance = new Seance();
        seance.setSeance(dateSeance);
        seance.setDelai(delai);
        seance.setDateLivraison(dateLivraison);
        seance.setStatut("OUVERTE");
        seance.setMotifAnnulation(null);
        seance.setAnomalies(new ArrayList<>());

        return seanceRepository.save(seance);
    }

    public Seance createSeanceFromIso(String isoDate) {
        String compact = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return createSeance(compact);
    }

    public void ensureSeanceExists(String isoDate) {
        LocalDate d = parseSeanceIso(isoDate);
        dedupeSeances(d);
        if (!existsBySeance(d)) {
            createSeanceFromIso(isoDate);
        }
    }

    public Seance saveSeance(Seance seance) {
        return seanceRepository.save(seance);
    }

    public Seance cloturer(String seanceCompact) {
        LocalDate d = parseSeance(seanceCompact);
        dedupeSeances(d);
        Seance s = findOneBySeance(d)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        s.setStatut("CLOTUREE");
        return seanceRepository.save(s);
    }

    public Seance cloturerFromIso(String isoDate) {
        return cloturer(toSeanceCompact(parseSeanceIso(isoDate)));
    }

    public Seance annuler(String seanceCompact, String motif) {
        LocalDate d = parseSeance(seanceCompact);
        dedupeSeances(d);
        Seance s = findOneBySeance(d)
                .orElseThrow(() -> new RuntimeException("Séance introuvable"));
        s.setStatut("ANNULEE");
        s.setMotifAnnulation(motif);
        return seanceRepository.save(s);
    }

    public Seance annulerFromIso(String isoDate, String motif) {
        return annuler(toSeanceCompact(parseSeanceIso(isoDate)), motif);
    }

private LocalDate calculerDateLivraison(LocalDate seance, int delai) {
    LocalDate dateLivraison = seance.plusDays(delai);
    while (jourFerieRepository.existsByJour(dateLivraison)) {
        dateLivraison = dateLivraison.plusDays(1);
    }
    return dateLivraison;
}
}
