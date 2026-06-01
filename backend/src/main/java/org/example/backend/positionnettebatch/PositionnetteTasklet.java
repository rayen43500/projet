package com.fgm.gestion.positionnettebatch;

import com.fgm.gestion.model.*;
import com.fgm.gestion.service.*;

import com.fgm.gestion.repository.*;


import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

import java.time.LocalDate;
import java.util.*;

@Component
public class PositionnetteTasklet implements Tasklet {

    private final TransactionRepository transactionRepository;
    private final ValeurRepository valeurRepository;
    private final PositionnetteRepository positionnetteRepository;
    private final SeanceRepository seanceRepository;
    private final HistoryService historyService;
    private final PositionNetteCalculator positionNetteCalculator;
    private final IntermediaireLookupService intermediaireLookup;

    private static final double MAX_LINE_VOLUME = 50_000_000;

    public PositionnetteTasklet(TransactionRepository transactionRepository,
                                ValeurRepository valeurRepository,
                                PositionnetteRepository positionnetteRepository,
                                SeanceRepository seanceRepository,
                                HistoryService historyService,
                                PositionNetteCalculator positionNetteCalculator,
                                IntermediaireLookupService intermediaireLookup) {
        this.transactionRepository = transactionRepository;
        this.valeurRepository = valeurRepository;
        this.positionnetteRepository = positionnetteRepository;
        this.seanceRepository = seanceRepository;
        this.historyService = historyService;
        this.positionNetteCalculator = positionNetteCalculator;
        this.intermediaireLookup = intermediaireLookup;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        // Recuperer seance depuis parametre
        String seanceParam = (String) chunkContext.getStepContext()
                .getJobParameters().get("seance");

// format correspondant
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

// Conversion en date
        LocalDate seance = LocalDate.parse(seanceParam, formatter);


// Scoped delete: recalcul propre pour cette séance uniquement
        positionnetteRepository.deleteBySeance(seance);

// Charger transactions et valeurs
        List<Transaction> transactions =
                transactionRepository.findBySeance(seance);
        List<Valeur> valeurs =
                valeurRepository.findBySeance(seance);

        // Map valeur -> prix cloture
        Map<String, Double> mapCloture = new HashMap<>();
        for (Valeur v : valeurs) {
            if (v.getSeance().equals(seance)) {
                if (v.getLibelleValeur() != null && !v.getLibelleValeur().isBlank()) {
                    mapCloture.put(v.getLibelleValeur().trim(), v.getCloture());
                }
                if (v.getCodeValeur() != null && !v.getCodeValeur().isBlank()) {
                    mapCloture.put(v.getCodeValeur().trim(), v.getCloture());
                }
            }
        }

        // Calcul positions acheteur et vendeur
        Map<String, double[]> mapAcheteur = new HashMap<>();

        //  MAP VENDEUR
        Map<String, double[]> mapVendeur = new HashMap<>();

        for (Transaction t : transactions) {

            if (!t.getSeance().equals(seance)) continue;

            String valeur = t.getLibelleValeur();
            double cloture = resolveCloture(mapCloture, valeur, t.getCodeValeur());

            int quantite = t.getQuantiteNegociee();
            double lineNotional = effectiveLineNotional(quantite, cloture, t.getVolume());

            // ACHETEUR
            if (isValidInterCode(t.getCodeIntermediaireAcheteur())) {

                String key = posKey(t.getCodeIntermediaireAcheteur(), valeur);

                mapAcheteur.merge(
                        key,
                        new double[]{quantite, cloture, lineNotional},
                        (oldVal, newVal) -> new double[]{
                                oldVal[0] + newVal[0],
                                newVal[1],
                                oldVal[2] + newVal[2]
                        }
                );
            }

            //VENDEUR
            if (isValidInterCode(t.getCodeIntermediaireVendeur())) {

                String key = posKey(t.getCodeIntermediaireVendeur(), valeur);

                mapVendeur.merge(
                        key,
                        new double[]{quantite, cloture, lineNotional},
                        (oldVal, newVal) -> new double[]{
                                oldVal[0] + newVal[0],
                                newVal[1],
                                oldVal[2] + newVal[2]
                        }
                );
            }
        }

        //  fusion des cles
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(mapAcheteur.keySet());
        allKeys.addAll(mapVendeur.keySet());

        //  construction Positionnette
        for (String key : allKeys) {

            String[] parts = key.split("\\|", 2);

            int codeInt = Integer.parseInt(parts[0]);
            String valeur = parts.length > 1 ? parts[1] : "";

            double[] ach = mapAcheteur.getOrDefault(key, new double[]{0,0,0});
            double[] vend = mapVendeur.getOrDefault(key, new double[]{0,0,0});

            int quantiteAchete = (int) ach[0];
            int quantiteVendu = (int) vend[0];

            double prix = ach[1] != 0 ? ach[1] : vend[1];
            double montantRecu = effectiveMontant(vend[2], quantiteVendu, prix);
            double montantVerse = effectiveMontant(ach[2], quantiteAchete, prix);

            int quantiteNette = positionNetteCalculator.computeQuantiteNette(quantiteAchete, quantiteVendu);
            double montantNetteSigned = positionNetteCalculator.computeMontantNetteSigned(montantVerse, montantRecu);

            String pntSign = positionNetteCalculator.signPnt(quantiteNette);
            String pneSign = positionNetteCalculator.signPne(montantNetteSigned);

            if (quantiteNette == 0 && montantNetteSigned == 0) {
                continue;
            }

            if (positionNetteCalculator.violatesExclusivityRule(pntSign, pneSign)) {
                continue;
            }

            String interLabel = intermediaireLookup.resolveLibelleCourt(codeInt, seance);
            if (interLabel.isBlank()) {
                interLabel = tLibelleFromMaps(mapAcheteur, mapVendeur, key);
            }

            Positionnette p = new Positionnette();

            p.setSeance(seance);
            p.setCodeIntermediaire(codeInt);
            p.setIntermediaire(interLabel);
            p.setValeur(valeur);

            p.setCloture(prix);

            p.setQuantiteAchete(quantiteAchete);
            p.setQuantiteVendu(quantiteVendu);
            p.setQuantiteNette(Math.abs(quantiteNette));

            p.setMontantRecu(montantRecu);
            p.setMontantVerse(montantVerse);
            p.setMontantNette(Math.abs(montantNetteSigned));

            p.setPnt(pntSign);
            p.setPne(pneSign);

            positionnetteRepository.save(p);
        }

        if (!seanceRepository.existsBySeance(seance)) {
            Seance s = new Seance();
            s.setSeance(seance);
            s.setStatut("OUVERTE");
            s.setAnomalies(new ArrayList<>());
            seanceRepository.save(s);
        }



        // ajouter Positionette
        historyService.saveHistory(
                "positionnette_" + seance,
                seance
        );




        return RepeatStatus.FINISHED;
    }

    private static double resolveCloture(Map<String, Double> map, String libelle, String codeValeur) {
        if (libelle != null) {
            Double c = map.get(libelle.trim());
            if (c != null) return c;
            for (Map.Entry<String, Double> e : map.entrySet()) {
                if (e.getKey().equalsIgnoreCase(libelle.trim())) return e.getValue();
            }
        }
        if (codeValeur != null && !codeValeur.isBlank()) {
            Double c = map.get(codeValeur.trim());
            if (c != null) return c;
        }
        return 0.0;
    }

    private static String posKey(int code, String valeur) {
        return code + "|" + (valeur != null ? valeur.trim() : "");
    }

    private static boolean isValidInterCode(int code) {
        return code > 0 && code <= 99_999;
    }

    /** Montant ligne : qty × cloture (TND) ; volume fichier en millimes si fiable. */
    private static double effectiveLineNotional(int qty, double cloture, double volume) {
        double fromCours = qty > 0 && cloture > 0 ? qty * cloture * 1000.0 : 0;
        if (volume > 0 && volume <= MAX_LINE_VOLUME) {
            if (fromCours <= 0 || volume <= fromCours * 1000) {
                return volume;
            }
        }
        return fromCours;
    }

    private static double effectiveMontant(double notionalSum, int qty, double cloture) {
        if (notionalSum > 0 && notionalSum <= MAX_LINE_VOLUME * 1000) {
            return notionalSum / 1000.0;
        }
        return qty > 0 && cloture > 0 ? qty * cloture : 0;
    }

    private static String tLibelleFromMaps(
            Map<String, double[]> mapAcheteur,
            Map<String, double[]> mapVendeur,
            String key) {
        return "#" + key.split("\\|", 2)[0];
    }
}