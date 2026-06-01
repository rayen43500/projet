package com.fgm.gestion.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Validation des 3 fichiers BVMT avant lancement du batch.
 * Formats acceptés : texte fixe BVMT ou export CSV séparateur {@code ;}.
 */
@Service
public class BvmtImportValidator {

    private static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public record ValidationResult(boolean valid, List<String> errors, List<String> warnings) {}

    public ValidationResult validate(
            MultipartFile transactionsFile,
            MultipartFile intermediairesFile,
            MultipartFile valeursFile,
            String expectedDateIso) throws Exception {

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (transactionsFile == null || transactionsFile.isEmpty()) {
            errors.add("Fichier transactions manquant ou vide");
        }
        if (intermediairesFile == null || intermediairesFile.isEmpty()) {
            errors.add("Fichier intermédiaires manquant ou vide");
        }
        if (valeursFile == null || valeursFile.isEmpty()) {
            errors.add("Fichier valeurs manquant ou vide");
        }
        if (!errors.isEmpty()) {
            return new ValidationResult(false, errors, warnings);
        }

        rejectExcelOnly(transactionsFile, "transactions", errors);
        rejectExcelOnly(intermediairesFile, "intermédiaires", errors);
        rejectExcelOnly(valeursFile, "valeurs", errors);
        if (!errors.isEmpty()) {
            return new ValidationResult(false, errors, warnings);
        }

        LocalDate expected = LocalDate.parse(expectedDateIso, ISO);
        String expectedCompact = expected.format(COMPACT);

        Set<String> txDates = new HashSet<>();
        Set<Integer> txBuyers = new HashSet<>();
        Set<Integer> txSellers = new HashSet<>();
        Set<String> txValeurs = new HashSet<>();
        int txCount = BvmtFileParser.parseTransactionsFromStream(
                transactionsFile.getInputStream(), txDates, txBuyers, txSellers, txValeurs);

        Set<Integer> interCodes = BvmtFileParser.parseIntermediaireCodes(intermediairesFile);
        Set<String> valDates = new HashSet<>();
        Set<String> valKeys = BvmtFileParser.parseValeurKeys(valeursFile, valDates, expected);

        if (txCount == 0) {
            errors.add("Aucune transaction valide dans le fichier (format fixe BVMT, ligne ≥ 140 car.)");
        }
        if (interCodes.isEmpty()) {
            errors.add("Aucun intermédiaire valide dans le fichier (format fixe, CSV CODE;LIBELLE;... ou JSON)");
        }
        if (valKeys.isEmpty()) {
            errors.add("Aucune valeur valide dans le fichier (format fixe ou CSV DATE;...;LIBELLE;...;CLOTURE)");
        }

        if (!txDates.isEmpty() && !txDates.contains(expectedCompact)) {
            warnings.add("Date transactions (" + txDates + ") ≠ date séance attendue (" + expectedCompact + ")");
        }
        if (!valDates.isEmpty()) {
            boolean dateOk = valDates.stream().anyMatch(d ->
                    d.equals(expectedCompact) || d.equals(expected.format(ISO)));
            if (!dateOk) {
                warnings.add("Date valeurs (" + valDates + ") peut différer de la séance " + expectedCompact);
            }
        }

        Set<Integer> allInterInTx = new HashSet<>();
        allInterInTx.addAll(txBuyers);
        allInterInTx.addAll(txSellers);
        allInterInTx.remove(0);

        for (Integer code : allInterInTx) {
            if (!interCodes.contains(code)) {
                warnings.add("Intermédiaire " + code + " présent dans transactions mais absent du fichier intermédiaires");
            }
        }

        for (String v : txValeurs) {
            if (!valKeys.contains(v) && !matchesValeurKey(v, valKeys)) {
                warnings.add("Valeur « " + v + " » absente du fichier valeurs (cours de clôture manquant)");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    private static boolean matchesValeurKey(String txKey, Set<String> valKeys) {
        if (txKey == null || txKey.isBlank()) return false;
        String k = txKey.trim();
        for (String vk : valKeys) {
            if (vk.equalsIgnoreCase(k)) return true;
            if (vk.contains(k) || k.contains(vk)) return true;
        }
        return false;
    }

    private void rejectExcelOnly(MultipartFile file, String label, List<String> errors) throws Exception {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            errors.add("Fichier " + label + " : Excel non supporté — utilisez .txt (fixe ou CSV point-virgule)");
            return;
        }
        byte[] head = file.getInputStream().readNBytes(4);
        if (head.length >= 2 && head[0] == 'P' && head[1] == 'K') {
            errors.add("Fichier " + label + " : fichier Excel détecté");
        }
    }
}
