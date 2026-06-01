package com.fgm.gestion.service;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ApportInitialService {

    private final ApportInitialRepository repo;
    private final PositionnetteRepository positionRepo;
    private final JourFerieRepository jourFerieRepository;
    private final IntermediaireRepository interRepo;
    private final SeanceService seanceService;
    private final ParametrageRepository parametrageRepository;
    private final IntermediaireLookupService intermediaireLookup;

    public ApportInitialService(
            ApportInitialRepository repo,
            PositionnetteRepository positionRepo,
            JourFerieRepository jourFerieRepository,
            IntermediaireRepository interRepo,
            SeanceService seanceService,
            ParametrageRepository parametrageRepository,
            IntermediaireLookupService intermediaireLookup) {

        this.repo = repo;
        this.positionRepo = positionRepo;
        this.jourFerieRepository = jourFerieRepository;
        this.interRepo = interRepo;
        this.seanceService = seanceService;
        this.parametrageRepository = parametrageRepository;
        this.intermediaireLookup = intermediaireLookup;
    }


    private boolean isFerie(LocalDate date) {
        return jourFerieRepository.existsByJour(date);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6;
    }

    private boolean isJourValide(LocalDate date) {
        return !isWeekend(date) && !isFerie(date);
    }

    private LocalDate getNextWorkingDay(LocalDate date) {
        LocalDate next = date.plusDays(1);

        while (!isJourValide(next)) {
            next = next.plusDays(1);
        }

        return next;
    }
    public ApportInitial save(ApportInitial obj) {
        return repo.save(obj);
    }

    public List<ApportInitial> getAll() {
        return repo.findAll();
    }

    public List<ApportInitial> getBySeance(LocalDate seance) {
        return repo.findBySeance(seance);
    }

    public void deleteAll() {
        repo.deleteAll();
    }

    public void deleteBySeance(LocalDate seance) {
        repo.deleteBySeance(seance);
    }

    // NORMALISATION CODE
    private String normalizeCode(String code) {
        if (code == null) return null;
        String result = code.replaceFirst("^0+", "");
        return result.isEmpty() ? "0" : result;
    }

    public int generer(LocalDate debut, LocalDate fin) {

        Parametrage param = parametrageRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Parametrage introuvable"));

        double seuil = 0.06;
        int delai = 2;

        LocalDate seanceSortie = fin;

        // récupérer positions sur la période
        List<Positionnette> positions =
                positionRepo.findBySeanceBetween(debut, fin);

        Map<String, List<Positionnette>> grouped = new HashMap<>();

        for (Positionnette p : positions) {

            String code = normalizeCode(String.valueOf(p.getCodeIntermediaire()));
            grouped.computeIfAbsent(code, k -> new ArrayList<>()).add(p);
        }

        // nombre de jours ouvrables
        List<Seance> seances = seanceService.getSeancesBetween(debut, fin);

        int nbSeances = seances.size();

        if (nbSeances == 0) {
            throw new RuntimeException("Aucune séance trouvée entre les dates");
        }

        int count = 0;

        for (String code : grouped.keySet()) {

            List<Positionnette> list = grouped.get(code);

            int codeInt;
            try { codeInt = Integer.parseInt(code); } catch(Exception e) { continue; }
            Intermediaire inter = intermediaireLookup.findForSeance(codeInt, seanceSortie);
            if (inter == null) continue;

            double posAch = list.stream()
                    .mapToDouble(Positionnette::getMontantRecu)
                    .sum();

            double posVend = list.stream()
                    .mapToDouble(Positionnette::getMontantVerse)
                    .sum();

            // moyenne sur période
            double moyquot = (posAch + posVend) / nbSeances;

            int apportAjuste =
                    (int) (moyquot * Math.pow(1 + seuil, delai) - moyquot);

            // ancien apport
            ApportInitial prev = repo
                    .findTopByCodeIntermAndSeanceLessThanOrderBySeanceDesc(codeInt, seanceSortie);

            int apportInitial = (prev != null)
                    ? prev.getApportInitialAjuste()
                    : 0;

            int appel = 0;
            int restitution = 0;

            if (apportAjuste > apportInitial) {
                appel = apportAjuste - apportInitial;
            } else {
                restitution = apportInitial - apportAjuste;
            }

            ApportInitial a = new ApportInitial();


            a.setSeance(seanceSortie);

            a.setCodeInterm(String.valueOf(codeInt));
            a.setIntermediaire(inter.getLibelleLong());

            a.setPositionAch(posAch);
            a.setPositionVenduEns(posVend);

            a.setApportInitial(apportInitial);
            a.setApportInitialAjuste(apportAjuste);

            a.setAppelContrib(appel);
            a.setRestitution(restitution);
            a.setDebut(debut);
            a.setFin(fin);
            a.setMoyequot(moyquot);

            repo.save(a);
            count++;
        }

        return count;
    }
}