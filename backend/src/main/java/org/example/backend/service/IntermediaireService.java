package com.fgm.gestion.service;

import com.fgm.gestion.model.Intermediaire;
import com.fgm.gestion.repository.IntermediaireRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

@Service
public class IntermediaireService {

    private final IntermediaireRepository intermediaireRepository;

    public IntermediaireService(IntermediaireRepository intermediaireRepository) {
        this.intermediaireRepository = intermediaireRepository;
    }

    public void processIntermediaireFile(MultipartFile file) {

        List<Intermediaire> list = parseFile(file);

        if (!list.isEmpty()) {
            for (Intermediaire i : list) {
                i.setNomFichier(file.getOriginalFilename());
                i.setDateImport(LocalDate.now());
            }

            intermediaireRepository.saveAll(list);
        }
    }

    private List<Intermediaire> parseFile(MultipartFile file) {

        List<Intermediaire> list = new ArrayList<>();

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] lines = content.split("\n");

            for (String line : lines) {

                line = line.replace("\r", "");

                if (line.trim().isEmpty()) continue;

                try {
                    Intermediaire i = new Intermediaire();

                    i.setCodeIntermediaire(parseIntSafe(safeSubstring(line, 0, 11)));
                    i.setLibelleCourt(safeSubstring(line, 12, 35));
                    i.setLibelleLong(safeSubstring(line, 35, 88));
                    i.setNumeroCompte(safeSubstring(line, 88, 140));

                    i.setTypeBanque(parseIntSafe(safeSubstring(line, 141, 153)));
                    i.setAdresse(safeSubstring(line, 154, 236));
                    i.setCodeBanque(parseIntSafe(safeSubstring(line, 237, 248)));

                    list.add(i);

                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing intermediaire.txt : " + e.getMessage());
        }

        return list;
    }

    private String safeSubstring(String str, int start, int end) {
        if (str.length() < end) return "";
        return str.substring(start, end).trim();
    }

    private long parseLongSafe(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public Optional<Intermediaire> findById(String id) {
        return intermediaireRepository.findById(id);
    }

    public List<Intermediaire> findByDateImport(LocalDate date) {
        return intermediaireRepository.findByDateImport(date);
    }

    /** Dernier import BVMT à la date de séance ou juste avant (évite liste vide si séance ≠ aujourd'hui). */
    public List<Intermediaire> findForSeance(LocalDate seance) {
        List<Intermediaire> exact = findByDateImport(seance);
        if (!exact.isEmpty()) return withoutSeedWhenRealData(exact);

        List<Intermediaire> all = intermediaireRepository.findAll();
        LocalDate best = all.stream()
                .map(Intermediaire::getDateImport)
                .filter(java.util.Objects::nonNull)
                .filter(d -> !d.isAfter(seance))
                .max(LocalDate::compareTo)
                .orElse(null);
        if (best == null) return List.of();
        List<Intermediaire> onBest = all.stream()
                .filter(i -> best.equals(i.getDateImport()))
                .toList();
        return withoutSeedWhenRealData(onBest);
    }

    private static List<Intermediaire> withoutSeedWhenRealData(List<Intermediaire> list) {
        boolean hasBvmt = list.stream().anyMatch(i -> i.getNomFichier() != null && !"seed".equalsIgnoreCase(i.getNomFichier()));
        if (!hasBvmt) return list;
        return list.stream()
                .filter(i -> i.getNomFichier() == null || !"seed".equalsIgnoreCase(i.getNomFichier()))
                .toList();
    }

    /** Returns intermediaires for the most recent import date */
    public List<Intermediaire> findMostRecent() {
        List<Intermediaire> all = intermediaireRepository.findAll();
        if (all.isEmpty()) return all;
        // Find the latest dateImport
        LocalDate latest = all.stream()
                .map(Intermediaire::getDateImport)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
        if (latest == null) return all;
        return all.stream()
                .filter(i -> latest.equals(i.getDateImport()))
                .collect(java.util.stream.Collectors.toList());
    }

}