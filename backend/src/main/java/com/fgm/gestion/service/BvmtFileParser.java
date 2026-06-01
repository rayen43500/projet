package com.fgm.gestion.service;

import com.fgm.gestion.model.Intermediaire;
import com.fgm.gestion.model.Valeur;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lecture des fichiers BVMT :
 * - Transactions : format fixe classique OU clearing {@code 02 20260331COB...}
 * - Intermédiaires / Valeurs : texte fixe ou CSV {@code ;} (avec espaces autour du ;)
 */
public final class BvmtFileParser {

    private static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter VALEUR_DDMMYY = DateTimeFormatter.ofPattern("dd/MM/yy");

    /** Ligne transaction normalisée pour batch + validation */
    public record TxRow(
            String seanceCompact,
            String codeValeur,
            String libelleValeur,
            int codeAcheteur,
            String libelleAcheteur,
            int codeVendeur,
            String libelleVendeur,
            int quantite,
            double prixTotal,
            double cours) {}

    private static final Pattern CLEARING_LINE = Pattern.compile(
            // Garde-fou léger pour identifier une ligne détail clearing.
            "02\\s+\\d{8}[A-Z]{3}\\s+\\d{2}:\\d{2}:\\d{2}TN\\d{10}.*",
            Pattern.CASE_INSENSITIVE);

    private BvmtFileParser() {}

    // ── Transactions ───────────────────────────────────────────────────────────

    public static List<TxRow> parseAllTransactions(List<String> lines) {
        List<TxRow> out = new ArrayList<>();
        for (String raw : lines) {
            String line = cleanLine(raw);
            if (line.isEmpty() || isTransactionHeader(line)) continue;
            Optional<TxRow> row = parseClearingTransaction(line);
            if (row.isEmpty()) {
                row = parseFixedWidthTransaction(line);
            }
            row.ifPresent(out::add);
        }
        return out;
    }

    public static List<TxRow> parseAllTransactions(Path path) throws Exception {
        return parseAllTransactions(Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    public static int parseTransactionsFromStream(
            InputStream in,
            Set<String> dates,
            Set<Integer> buyers,
            Set<Integer> sellers,
            Set<String> valeurs) throws Exception {

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
        int count = 0;
        for (TxRow tx : parseAllTransactions(lines)) {
            dates.add(tx.seanceCompact());
            buyers.add(tx.codeAcheteur());
            sellers.add(tx.codeVendeur());
            if (!tx.libelleValeur().isBlank()) valeurs.add(tx.libelleValeur().trim());
            if (!tx.codeValeur().isBlank()) valeurs.add(tx.codeValeur().trim());
            count++;
        }
        return count;
    }

    public static String detectDateFromTransactions(List<String> lines) {
        for (TxRow tx : parseAllTransactions(lines)) {
            if (tx.seanceCompact() != null && tx.seanceCompact().matches("\\d{8}")) {
                return tx.seanceCompact();
            }
        }
        return null;
    }

    public static String detectDateFromValeurs(MultipartFile file) throws Exception {
        for (String raw : readAllLines(file)) {
            String line = cleanLine(raw);
            if (!line.contains(";")) continue;
            String[] cols = splitSemicolon(line);
            if (cols.length < 1) continue;
            if (looksLikeValeurHeader(cols)) continue;
            LocalDate d = parseDateFlexible(cols[0]);
            if (d != null) return d.format(COMPACT);
        }
        return null;
    }

    /** Date de séance = fichier valeurs (cours) si dispo, sinon transactions */
    public static String detectSeanceCompact(MultipartFile transactionsFile, MultipartFile valeursFile) throws Exception {
        if (valeursFile != null && !valeursFile.isEmpty()) {
            String fromVal = detectDateFromValeurs(valeursFile);
            if (fromVal != null) return fromVal;
        }
        List<String> txLines = readAllLines(transactionsFile);
        String fromTx = detectDateFromTransactions(txLines);
        if (fromTx != null) return fromTx;
        return null;
    }

    private static Optional<TxRow> parseClearingTransaction(String line) {
        if (!line.contains("02") || !line.contains("COB") || !line.contains("TN")) {
            return Optional.empty();
        }
        int start = line.indexOf(" 02 ");
        if (start >= 0) {
            start = start + 1; // garder le "02" au début du body
        } else if (line.startsWith("02 ")) {
            start = 0;
        } else {
            start = line.indexOf("02 ");
            if (start < 0) return Optional.empty();
        }
        String body = line.substring(start);

        Matcher m = CLEARING_LINE.matcher(body);
        if (!m.matches()) return Optional.empty();

        // Mapping positionnel selon le format BVMT "transactions clearing E"
        // Positions 1-based dans le body qui commence à "02 ...":
        // 1-2: indicateur ligne (02), 4-11: séance, 12-16: type (COB),
        // 18-25: heure, 26-37: code valeur (ISIN), 38-55: libellé valeur,
        // 56-62: num tx, 63-70: code ach, 71-80: lib ach, 81-83: origine ach,
        // 84-91: code vend, 92-101: lib vend, 102-104: origine vend,
        // 105-113: quantité, 114-125: cours (3 déc), 126-140: volume (3 déc).
        String dateCompact = fixed(body, 4, 11);
        String isin = fixed(body, 26, 37);
        String libelle = fixed(body, 38, 55);

        int codeAcheteur = parseIntermediaireCode(fixed(body, 63, 70));
        String libAcheteur = fixed(body, 71, 80);
        int codeVendeur = parseIntermediaireCode(fixed(body, 84, 91));
        String libVendeur = fixed(body, 92, 101);

        int quantite = parseIntSafe(fixed(body, 105, 113));
        double cours = parseFixed3(fixed(body, 114, 125));
        double prixTotal = parseFixed3(fixed(body, 126, 140)); // en millimes

        if (!dateCompact.matches("\\d{8}") || isin.isBlank() || quantite <= 0) {
            return Optional.empty();
        }

        return Optional.of(new TxRow(
                dateCompact, isin, libelle,
                codeAcheteur, libAcheteur, codeVendeur, libVendeur,
                quantite, prixTotal, cours));
    }

    private static Optional<TxRow> parseFixedWidthTransaction(String line) {
        if (line.length() < 140) return Optional.empty();
        String dateField = safe(line, 14, 22);
        if (!dateField.matches("\\d{8}")) return Optional.empty();

        int codeAch = parseIntermediaireCode(safe(line, 72, 80));
        int codeVend = parseIntermediaireCode(safe(line, 93, 101));
        int qty = (int) parseDouble(safe(line, 114, 123));
        double cours = parseDouble(safe(line, 125, 135));
        double prix = parseDouble(safe(line, 135, 150));

        return Optional.of(new TxRow(
                dateField,
                safe(line, 35, 47).trim(),
                safe(line, 47, 65).trim(),
                codeAch, safe(line, 80, 90),
                codeVend, safe(line, 101, 111),
                qty, prix, cours));
    }

    // ── Intermédiaires ────────────────────────────────────────────────────────

    public static List<Intermediaire> parseIntermediaires(Path path) throws Exception {
        return parseIntermediaires(Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    public static List<Intermediaire> parseIntermediaires(MultipartFile file) throws Exception {
        return parseIntermediaires(readAllLines(file));
    }

    public static List<Intermediaire> parseIntermediaires(List<String> lines) {
        if (lines.isEmpty()) return List.of();
        String joined = String.join("\n", lines).trim();
        if (joined.startsWith("[") || joined.startsWith("{")) {
            List<Intermediaire> fromJson = parseIntermediairesJson(joined);
            if (!fromJson.isEmpty()) return fromJson;
        }
        if (isSemicolonCsv(lines)) {
            return parseIntermediairesCsv(lines);
        }
        return parseIntermediairesFixed(lines);
    }

    public static Set<Integer> parseIntermediaireCodes(MultipartFile file) throws Exception {
        Set<Integer> codes = new HashSet<>();
        for (Intermediaire i : parseIntermediaires(file)) {
            if (i.getCodeIntermediaire() != 0) codes.add(i.getCodeIntermediaire());
        }
        return codes;
    }

    private static List<Intermediaire> parseIntermediairesCsv(List<String> lines) {
        Map<Integer, Intermediaire> byCode = new LinkedHashMap<>();
        for (String raw : lines) {
            String line = cleanLine(raw);
            if (line.isEmpty() || !line.contains(";")) continue;
            String[] cols = splitSemicolon(line);
            if (cols.length < 2) continue;
            if (looksLikeInterHeader(cols)) continue;

            int code = parseIntSafe(cols[0]);
            if (code == 0) continue;

            Intermediaire i = new Intermediaire();
            i.setCodeIntermediaire(code);
            i.setLibelleCourt(cols.length > 1 ? cols[1].trim() : "");
            i.setLibelleLong(cols.length > 2 ? cols[2].trim() : i.getLibelleCourt());
            i.setNumeroCompte(cols.length > 3 ? cols[3].trim() : "");
            i.setTypeBanque(cols.length > 4 ? parseIntSafe(cols[4]) : 0);
            i.setAdresse(cols.length > 5 ? cols[5].trim() : "");
            i.setCodeBanque(cols.length > 6 ? parseIntSafe(cols[6]) : 0);
            byCode.put(code, i);
        }
        return new ArrayList<>(byCode.values());
    }

    private static List<Intermediaire> parseIntermediairesJson(String json) {
        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(json);
            JsonNode arr = root.isArray() ? root : root.path("intermediaires");
            if (!arr.isArray()) return List.of();

            Map<Integer, Intermediaire> byCode = new LinkedHashMap<>();
            for (JsonNode node : arr) {
                Intermediaire i = mapIntermediaireJson(node);
                if (i.getCodeIntermediaire() != 0) {
                    byCode.put(i.getCodeIntermediaire(), i);
                }
            }
            return new ArrayList<>(byCode.values());
        } catch (Exception e) {
            return List.of();
        }
    }

    private static Intermediaire mapIntermediaireJson(JsonNode n) {
        Intermediaire i = new Intermediaire();
        int code = firstInt(n, "codeIntermediaire", "code", "id", "code_intermediaire");
        i.setCodeIntermediaire(code);
        String court = firstText(n, "libelleCourt", "libelle_court", "libelleCourtIntermediaire", "mnemo", "libelle");
        i.setLibelleCourt(court);
        String lon = firstText(n, "libelleLong", "libelle_long", "libelleLongIntermediaire", "raisonSociale");
        i.setLibelleLong(lon != null && !lon.isBlank() ? lon : court);
        i.setNumeroCompte(firstText(n, "numeroCompte", "numero_compte", "numeroCompteIntermediaire", "compte"));
        i.setAdresse(firstText(n, "adresse", "adresseIntermediaire", "address"));
        i.setTypeBanque(firstInt(n, "typeBanque", "type_banque", "typeBanqueIntermediaire"));
        i.setCodeBanque(firstInt(n, "codeBanque", "code_banque", "codeBanqueIntermediaire"));
        return i;
    }

    private static String firstText(JsonNode n, String... keys) {
        for (String k : keys) {
            JsonNode v = n.get(k);
            if (v != null && !v.isNull() && !v.asText().isBlank()) {
                return v.asText().trim();
            }
        }
        return "";
    }

    private static int firstInt(JsonNode n, String... keys) {
        for (String k : keys) {
            JsonNode v = n.get(k);
            if (v != null && !v.isNull()) {
                if (v.isNumber()) return v.intValue();
                String s = v.asText().trim();
                if (!s.isEmpty()) return parseIntermediaireCode(s);
            }
        }
        return 0;
    }

    private static List<Intermediaire> parseIntermediairesFixed(List<String> lines) {
        Map<Integer, Intermediaire> byCode = new LinkedHashMap<>();
        for (String raw : lines) {
            String line = cleanLine(raw);
            if (line.length() < 12) continue;
            int code = parseIntSafe(safe(line, 0, 11));
            if (code == 0) continue;
            Intermediaire i = new Intermediaire();
            i.setCodeIntermediaire(code);
            i.setLibelleCourt(safe(line, 12, 34));
            i.setLibelleLong(safe(line, 35, 87));
            i.setNumeroCompte(safe(line, 88, 140));
            i.setTypeBanque(parseIntSafe(safe(line, 141, 153)));
            i.setAdresse(safe(line, 154, 236));
            i.setCodeBanque(parseIntSafe(safe(line, 237, 248)));
            byCode.put(code, i);
        }
        return new ArrayList<>(byCode.values());
    }

    // ── Valeurs ───────────────────────────────────────────────────────────────

    public static List<Valeur> parseValeurs(Path path, LocalDate seanceFromJob) throws Exception {
        return parseValeurs(Files.readAllLines(path, StandardCharsets.UTF_8), seanceFromJob);
    }

    public static List<Valeur> parseValeurs(MultipartFile file, LocalDate seanceFromJob) throws Exception {
        return parseValeurs(readAllLines(file), seanceFromJob);
    }

    public static Set<String> parseValeurKeys(MultipartFile file, Set<String> dates, LocalDate seanceFromJob) throws Exception {
        Set<String> keys = new HashSet<>();
        for (Valeur v : parseValeurs(file, seanceFromJob)) {
            if (v.getLibelleValeur() != null && !v.getLibelleValeur().isBlank()) {
                keys.add(v.getLibelleValeur().trim());
            }
            if (v.getCodeValeur() != null && !v.getCodeValeur().isBlank()) {
                keys.add(v.getCodeValeur().trim());
            }
        }
        for (String raw : readAllLines(file)) {
            String line = cleanLine(raw);
            if (!line.contains(";")) continue;
            String[] cols = splitSemicolon(line);
            if (cols.length > 0 && !looksLikeValeurHeader(cols)) {
                LocalDate d = parseDateFlexible(cols[0]);
                if (d != null) dates.add(d.format(COMPACT));
            }
        }
        if (seanceFromJob != null) {
            dates.add(seanceFromJob.format(COMPACT));
        }
        return keys;
    }

    public static List<Valeur> parseValeurs(List<String> lines, LocalDate seanceFromJob) {
        if (lines.isEmpty()) return List.of();
        if (isSemicolonCsv(lines)) {
            return parseValeursCsv(lines, seanceFromJob);
        }
        return parseValeursFixed(lines, seanceFromJob);
    }

    private static List<Valeur> parseValeursCsv(List<String> lines, LocalDate seanceFromJob) {
        List<Valeur> list = new ArrayList<>();
        for (String raw : lines) {
            String line = cleanLine(raw);
            if (line.isEmpty() || !line.contains(";")) continue;
            String[] cols = splitSemicolon(line);
            if (cols.length < 4) continue;
            if (looksLikeValeurHeader(cols)) continue;

            LocalDate seance = seanceFromJob != null ? seanceFromJob : parseDateFlexible(cols[0]);
            if (seance == null) continue;

            Valeur v = new Valeur();
            v.setSeance(seance);
            v.setCodeValeur(cols.length > 2 ? cols[2].trim() : "");
            v.setLibelleValeur(cols.length > 3 ? cols[3].trim() : "");
            if (cols.length >= 6) {
                v.setVeille(parseDouble(cols[4]));
                v.setCloture(parseDouble(cols[5]));
            } else if (cols.length >= 5) {
                v.setCloture(parseDouble(cols[cols.length - 1]));
            }
            v.setNomFichier("batch-file");
            if (!v.getLibelleValeur().isBlank()) {
                list.add(v);
            }
        }
        return list;
    }

    private static List<Valeur> parseValeursFixed(List<String> lines, LocalDate seanceFromJob) {
        List<Valeur> list = new ArrayList<>();
        for (String raw : lines) {
            String line = cleanLine(raw);
            if (line.trim().isEmpty() || line.length() < 63) continue;
            Valeur v = new Valeur();
            LocalDate seance = seanceFromJob;
            if (seance == null) {
                try {
                    seance = LocalDate.parse(safe(line, 0, 8), VALEUR_DDMMYY);
                } catch (DateTimeParseException e) {
                    continue;
                }
            }
            v.setSeance(seance);
            v.setCodeValeur(safe(line, 12, 24));
            v.setLibelleValeur(safe(line, 25, 43));
            v.setVeille(parseDouble(safe(line, 44, 53)));
            v.setCloture(parseDouble(safe(line, 54, 63)));
            v.setNomFichier("batch-file");
            list.add(v);
        }
        return list;
    }

    // ── Détection format ──────────────────────────────────────────────────────

    public static boolean isSemicolonCsv(List<String> lines) {
        for (String raw : lines) {
            String line = cleanLine(raw);
            if (line.isEmpty()) continue;
            if (line.contains(" 02 ") && line.contains("COB")) return false;
            if (line.contains(";")) {
                String[] cols = splitSemicolon(line);
                if (cols.length >= 2) return true;
            }
            if (line.length() >= 140 && safe(line, 14, 22).matches("\\d{8}")) {
                return false;
            }
        }
        return false;
    }

    private static boolean looksLikeInterHeader(String[] cols) {
        if (cols.length == 0) return false;
        String c0 = cols[0].trim().toUpperCase(Locale.ROOT);
        return c0.contains("CODE") || c0.equals("ID");
    }

    private static boolean looksLikeValeurHeader(String[] cols) {
        if (cols.length == 0) return false;
        String c0 = cols[0].trim().toUpperCase(Locale.ROOT);
        return c0.contains("DATE") || c0.equals("ISIN");
    }

    public static boolean isTransactionHeader(String line) {
        String u = line.toUpperCase(Locale.ROOT);
        return u.contains(" 01 ") && (u.contains("TRANSACTION") || u.contains("CLEARING"))
                || u.startsWith("EN-TETE") || u.startsWith("EN-TÊTE")
                || u.startsWith("HEADER") || u.startsWith("DATE;");
    }

    /** Découpe {@code code ; libelle ; ...} avec espaces autour des points-virgules */
    public static String[] splitSemicolon(String line) {
        return Arrays.stream(line.split("\\s*;\\s*", -1))
                .map(String::trim)
                .toArray(String[]::new);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    public static LocalDate parseDateFlexible(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim();
        try {
            if (s.matches("\\d{8}")) {
                return LocalDate.parse(s, COMPACT);
            }
            if (s.matches("\\d{2}/\\d{2}/\\d{2}")) {
                return LocalDate.parse(s, VALEUR_DDMMYY);
            }
            if (s.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static List<String> readAllLines(MultipartFile file) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
            return lines;
        }
    }

    public static String cleanLine(String line) {
        return line == null ? "" : line.replace("\r", "").replace("\uFEFF", "").trim();
    }

    public static String safe(String s, int start, int end) {
        if (s == null || s.length() < end) return "";
        return s.substring(start, end).trim();
    }

    /** Slice 1-based inclusif, robuste si la ligne est plus courte. */
    private static String fixed(String s, int from1, int to1) {
        if (s == null || s.isEmpty() || from1 > to1) return "";
        int start = Math.max(0, from1 - 1);
        int end = Math.min(s.length(), to1);
        if (start >= end) return "";
        return s.substring(start, end).trim();
    }

    /** Champs numériques BVMT à 3 décimales implicites (ex: 000000006700 -> 6.700). */
    private static double parseFixed3(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        String t = raw.trim().replaceAll("\\s+", "");
        if (t.isEmpty()) return 0;
        if (t.contains(".") || t.contains(",")) {
            return parseDouble(t);
        }
        try {
            String digits = t.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return 0;
            return Long.parseLong(digits) / 1000.0;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Code IB : champ 7 chiffres, bloc {@code 00100000009} → 9 (pas 1000000009). */
    public static int parseIntermediaireCode(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        String t = raw.trim();
        Matcher padded = Pattern.compile("001(\\d{8})").matcher(t);
        if (padded.find()) {
            int c = Integer.parseInt(padded.group(1));
            return validInterCode(c);
        }
        if (t.matches("\\d{7}")) {
            return validInterCode(Integer.parseInt(t));
        }
        if (t.matches("\\d{1,6}")) {
            return validInterCode(Integer.parseInt(t));
        }
        return validInterCode(parseIntSafe(t));
    }

    private static int validInterCode(int code) {
        return (code > 0 && code <= 99_999) ? code : 0;
    }

    public static int parseIntSafe(String s) {
        if (s == null) return 0;
        try {
            String digits = s.trim().replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return 0;
            if (digits.length() > 8) {
                digits = digits.substring(digits.length() - 8);
            }
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return 0;
        }
    }

    public static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0;
        String t = s.trim().replaceAll("\\s+", "");
        if (t.isEmpty()) return 0;

        try {
            // Support simple des formats courants :
            // - "15,110"     => décimale virgule
            // - "1.234,567" => milliers '.' + décimale ','
            // - "12,500,000" => milliers ',' (décimale absente)
            String sign = "";
            if (t.startsWith("-") || t.startsWith("+")) {
                sign = t.substring(0, 1);
                t = t.substring(1);
            }
            if (t.startsWith("(") && t.endsWith(")")) {
                sign = "-";
                t = t.substring(1, t.length() - 1);
            }

            int lastComma = t.lastIndexOf(',');
            int lastDot = t.lastIndexOf('.');

            if (lastComma >= 0 && lastDot >= 0) {
                // Les deux séparateurs sont présents : le plus à droite est la décimale.
                if (lastComma > lastDot) {
                    // décimale ',' ; '.' = milliers
                    t = t.replace(".", "");
                    t = t.replace(',', '.');
                } else {
                    // décimale '.' ; ',' = milliers
                    t = t.replace(",", "");
                }
            } else if (lastComma >= 0) {
                // Un seul séparateur : ','.
                int commaCount = 0;
                for (int i = 0; i < t.length(); i++) if (t.charAt(i) == ',') commaCount++;

                if (commaCount == 1) {
                    // une virgule => décimale
                    t = t.replace(',', '.');
                } else {
                    // plusieurs virgules => milliers la plupart du temps
                    int idx = t.lastIndexOf(',');
                    String frac = t.substring(idx + 1);
                    if (frac.length() != 3) {
                        // probablement une décimale sur la dernière virgule
                        String whole = t.substring(0, idx).replace(",", "");
                        t = whole + "." + frac;
                    } else {
                        // probablement des milliers (groupes à 3)
                        t = t.replace(",", "");
                    }
                }
            } else if (lastDot >= 0) {
                // Un seul séparateur : '.'
                int dotCount = 0;
                for (int i = 0; i < t.length(); i++) if (t.charAt(i) == '.') dotCount++;

                if (dotCount == 1) {
                    // une seule occurrence => décimale
                } else {
                    int idx = t.lastIndexOf('.');
                    String frac = t.substring(idx + 1);
                    if (frac.length() != 3) {
                        // probablement une décimale sur la dernière '.' ; le reste = milliers
                        String whole = t.substring(0, idx).replace(".", "");
                        t = whole + "." + frac;
                    } else {
                        // probablement des milliers (groupes à 3)
                        t = t.replace(".", "");
                    }
                }
            }

            double parsed = Double.parseDouble(sign + t);
            if (Double.isNaN(parsed) || Double.isInfinite(parsed)) return 0;
            return parsed;
        } catch (Exception e) {
            return 0;
        }
    }
}
