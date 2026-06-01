package com.fgm.gestion.risquebatch;

import com.fgm.gestion.model.*;
import com.fgm.gestion.service.*;
import com.fgm.gestion.repository.*;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class RisqueTasklet implements Tasklet {

    private final PositionnetteRepository positionnetteRepository;
    private final RisqueRepository risqueRepository;
    private final ValeurRepository valeurRepository;
    private final SeanceRepository seanceRepository;
    private final HistoryService historyService;
    private final ParametrageRepository parametrageRepository;
    private final PositionNetteCalculator positionNetteCalculator;
    private final IntermediaireLookupService intermediaireLookup;

    public RisqueTasklet(PositionnetteRepository pr,
                         RisqueRepository rr,
                         ValeurRepository vr,
                         SeanceRepository seanceRepository,
                         HistoryService historyService,
                         ParametrageRepository parametrageRepository,
                         PositionNetteCalculator positionNetteCalculator,
                         IntermediaireLookupService intermediaireLookup) {
        this.positionnetteRepository = pr;
        this.risqueRepository = rr;
        this.valeurRepository = vr;
        this.seanceRepository = seanceRepository;
        this.historyService = historyService;
        this.parametrageRepository = parametrageRepository;
        this.positionNetteCalculator = positionNetteCalculator;
        this.intermediaireLookup = intermediaireLookup;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) {

        Parametrage param = parametrageRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Parametrage introuvable"));

        double seuil = param.getSeuil_var_3() / 100.0;
        int delai = param.getDel_reg_liv();

        String seanceParam = (String) chunkContext
                .getStepContext()
                .getJobParameters()
                .get("seance");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate seanceJ = LocalDate.parse(seanceParam, formatter);

        risqueRepository.deleteBySeance(seanceJ);

        List<Positionnette> listJ = positionnetteRepository.findBySeance(seanceJ);

        Optional<Seance> seancePrecedenteOpt = seanceRepository
                .findTopBySeanceLessThanOrderBySeanceDesc(seanceJ);

        List<Positionnette> listJ1 = new ArrayList<>();
        if (seancePrecedenteOpt.isPresent()) {
            LocalDate seanceJ_1 = seancePrecedenteOpt.get().getSeance();
            listJ1 = positionnetteRepository.findBySeance(seanceJ_1);
        }

        Map<String, Double> mapClotureJ = new HashMap<>();
        List<Valeur> valeursJ = valeurRepository.findBySeance(seanceJ);
        for (Valeur v : valeursJ) {
            mapClotureJ.put(v.getLibelleValeur(), v.getCloture());
        }

        Map<String, Positionnette> mapJ = new HashMap<>();
        for (Positionnette p : listJ) {
            mapJ.put(posKey(p.getCodeIntermediaire(), p.getValeur()), p);
        }

        Map<String, Positionnette> mapJ1 = new HashMap<>();
        for (Positionnette p : listJ1) {
            mapJ1.put(posKey(p.getCodeIntermediaire(), p.getValeur()), p);
        }

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(mapJ.keySet());
        allKeys.addAll(mapJ1.keySet());

        for (String key : allKeys) {
            Positionnette pj = mapJ.get(key);
            Positionnette pj1 = mapJ1.get(key);

            String[] parts = key.split("\\|", 2);
            int codeInter = Integer.parseInt(parts[0]);
            String valeurKey = parts.length > 1 ? parts[1] : "";

            Risque r = new Risque();
            r.setSeance(seanceJ);
            r.setCodeIntermediaire(codeInter);
            r.setIntermediaire(intermediaireLookup.resolveLibelleCourt(codeInter, seanceJ));
            r.setValeur(valeurKey);

            if (pj != null && pj1 != null) {
                r.setCloture(pj.getCloture());
                r.setQuantitenette(pj.getQuantiteNette());
                r.setMontantnette(pj.getMontantNette());
                r.setQuantitenettej_1(pj1.getQuantiteNette());
                r.setMontantnettej_1(pj1.getMontantNette());
                r.setPntj(pj.getPnt());
                r.setPntj_1(pj1.getPnt());

                double risqueJ = positionNetteCalculator.computeRisqueJour(
                        pj.getCloture(), pj.getQuantiteNette(), pj.getPnt(),
                        pj.getMontantNette(), pj.getPne(), seuil, delai);

                double risqueJ1 = positionNetteCalculator.computeRisqueJour(
                        pj.getCloture(), pj1.getQuantiteNette(), pj1.getPnt(),
                        pj1.getMontantNette(), pj1.getPne(), seuil, Math.max(0, delai - 1));

                r.setRisquej(positionNetteCalculator.roundRisque(risqueJ));
                r.setRisquej_1(positionNetteCalculator.roundRisque(risqueJ1));
                setRsSuspens(r, pj, pj.getPne());
            } else if (pj != null) {
                r.setCloture(pj.getCloture());
                r.setQuantitenette(pj.getQuantiteNette());
                r.setMontantnette(pj.getMontantNette());
                r.setPntj(pj.getPnt());

                double risqueJ = positionNetteCalculator.computeRisqueJour(
                        pj.getCloture(), pj.getQuantiteNette(), pj.getPnt(),
                        pj.getMontantNette(), pj.getPne(), seuil, delai);

                r.setRisquej(positionNetteCalculator.roundRisque(risqueJ));
                setRsSuspens(r, pj, pj.getPne());
            } else if (pj1 != null) {
                String valeur = valeurKey;
                double clotureJ = mapClotureJ.getOrDefault(valeur, pj1.getCloture());

                r.setCloture(clotureJ);
                r.setQuantitenettej_1(pj1.getQuantiteNette());
                r.setMontantnettej_1(pj1.getMontantNette());
                r.setPntj_1(pj1.getPnt());

                double risqueJ1 = positionNetteCalculator.computeRisqueJour(
                        clotureJ, pj1.getQuantiteNette(), pj1.getPnt(),
                        pj1.getMontantNette(), pj1.getPne(), seuil, Math.max(0, delai - 1));

                r.setRisquej_1(positionNetteCalculator.roundRisque(risqueJ1));
                setRsSuspens(r, pj1, pj1.getPne());
            }

            if (seancePrecedenteOpt.isPresent() && isSeanceDenouee(seancePrecedenteOpt.get(), seanceJ)) {
                r.setRisquej_1(0);
            }

            risqueRepository.save(r);
        }

        historyService.saveHistory("risque_" + seanceJ, seanceJ);
        return RepeatStatus.FINISHED;
    }

    private void setRsSuspens(Risque r, Positionnette p, String pneSign) {
        double rs = positionNetteCalculator.computeRsSuspens(
                p.getCloture(),
                p.getQuantiteNette(),
                p.getPnt(),
                p.getMontantNette(),
                pneSign);
        r.setRisqueSuspens(positionNetteCalculator.roundRisque(rs));
    }

    private static String posKey(int code, String valeur) {
        return code + "|" + (valeur != null ? valeur.trim() : "");
    }

    /** Séance J-1 dénouée si date de livraison ≤ séance courante. */
    private boolean isSeanceDenouee(Seance seanceJ1, LocalDate seanceJ) {
        LocalDate livraison = seanceJ1.getDateLivraison();
        return livraison != null && !livraison.isAfter(seanceJ);
    }
}
