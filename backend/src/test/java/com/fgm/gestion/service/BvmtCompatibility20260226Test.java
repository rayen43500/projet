package com.fgm.gestion.service;

import com.fgm.gestion.model.Valeur;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Garde-fou de compatibilite BVMT/FGM sur les fichiers reels fournis (seance 2026-02-26).
 * Ce test verifie quelques lignes de reference du document attendu.
 */
class BvmtCompatibility20260226Test {

    private static final Path FILE_DIR = Paths.get("file").toAbsolutePath().normalize();

    private record Agg(int qtyA, int qtyV, double amtA, double amtV) {
        Agg addA(int q, double a) { return new Agg(qtyA + q, qtyV, amtA + a, amtV); }
        Agg addV(int q, double a) { return new Agg(qtyA, qtyV + q, amtA, amtV + a); }
    }

    @Test
    void should_match_reference_lines_from_bvmt_document() throws Exception {
        Path txPath = FILE_DIR.resolve("transactions.20260226.txt");
        Path valPath = FILE_DIR.resolve("valeurs.20260226.txt");

        assertTrue(Files.exists(txPath), "transactions.20260226.txt manquant");
        assertTrue(Files.exists(valPath), "valeurs.20260226.txt manquant");

        Map<String, Double> clotureByKey = loadClotures(valPath);
        Map<String, Agg> aggByInterAndValeur = aggregateFromTransactions(txPath, clotureByKey);

        // 1 U.F.I ARTES 15,110 - 366 + 5 585,500
        assertLine(aggByInterAndValeur, 1, "ARTES", -366, +5585.5, 2.0);

        // 7 Union Capital MONOPRIX 6,700 - 5 + 33,500
        assertLine(aggByInterAndValeur, 7, "MONOPRIX", -5, +33.5, 0.5);

        // 2 T.S.I BNA 14,300 - 3 436 + 49 046,040
        assertLine(aggByInterAndValeur, 2, "BNA", -3436, +49046.040, 5.0);
    }

    private static void assertLine(Map<String, Agg> map, int interCode, String valeur, int expectedQN, double expectedPN, double amountTolerance) {
        String k = key(interCode, valeur);
        Agg a = map.getOrDefault(k, new Agg(0, 0, 0, 0));
        int qn = a.qtyA - a.qtyV;
        double pn = a.amtA - a.amtV;
        assertEquals(expectedQN, qn, "QN mismatch for " + interCode + " / " + valeur);
        assertEquals(expectedPN, pn, amountTolerance, "PN mismatch for " + interCode + " / " + valeur);
    }

    private static Map<String, Double> loadClotures(Path valeursPath) throws Exception {
        Map<String, Double> m = new HashMap<>();
        List<Valeur> valeurs = BvmtFileParser.parseValeurs(valeursPath, null);
        for (Valeur v : valeurs) {
            if (v.getCodeValeur() != null && !v.getCodeValeur().isBlank()) {
                m.put(v.getCodeValeur().trim(), v.getCloture());
            }
            if (v.getLibelleValeur() != null && !v.getLibelleValeur().isBlank()) {
                m.put(v.getLibelleValeur().trim(), v.getCloture());
            }
        }
        return m;
    }

    private static Map<String, Agg> aggregateFromTransactions(Path txPath, Map<String, Double> clotureByKey) throws Exception {
        Map<String, Agg> agg = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(txPath, StandardCharsets.UTF_8);
        List<BvmtFileParser.TxRow> rows = BvmtFileParser.parseAllTransactions(lines);

        for (BvmtFileParser.TxRow r : rows) {
            if (!"20260226".equals(r.seanceCompact())) continue;
            String valLib = r.libelleValeur() != null ? r.libelleValeur().trim() : "";
            String valCode = r.codeValeur() != null ? r.codeValeur().trim() : "";
            double cl = 0.0;
            if (!valCode.isBlank()) cl = clotureByKey.getOrDefault(valCode, 0.0);
            if (cl <= 0 && !valLib.isBlank()) cl = clotureByKey.getOrDefault(valLib, 0.0);

            double lineAmount = r.prixTotal() > 0 ? r.prixTotal() : (r.quantite() * cl);
            if (r.codeAcheteur() > 0) {
                String kA = key(r.codeAcheteur(), valLib);
                agg.putIfAbsent(kA, new Agg(0, 0, 0, 0));
                agg.put(kA, agg.get(kA).addA(r.quantite(), lineAmount));
            }
            if (r.codeVendeur() > 0) {
                String kV = key(r.codeVendeur(), valLib);
                agg.putIfAbsent(kV, new Agg(0, 0, 0, 0));
                agg.put(kV, agg.get(kV).addV(r.quantite(), lineAmount));
            }
        }
        return agg;
    }

    private static String key(int interCode, String valeur) {
        return interCode + "|" + (valeur == null ? "" : valeur.trim());
    }
}

