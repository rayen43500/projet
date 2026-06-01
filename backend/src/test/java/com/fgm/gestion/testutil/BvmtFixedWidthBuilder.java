package com.fgm.gestion.testutil;

import java.util.Arrays;

/** Génère des lignes BVMT texte fixe pour les tests d'import. */
public final class BvmtFixedWidthBuilder {

    private BvmtFixedWidthBuilder() {}

    public static String transactionLine(
            String seanceYyyyMmDd,
            String codeValeur,
            String libelleValeur,
            int codeAcheteur,
            String libAcheteur,
            int codeVendeur,
            String libVendeur,
            int quantite,
            double cours,
            double prixTotalMillimes) {

        char[] line = new char[160];
        Arrays.fill(line, ' ');
        put(line, 14, seanceYyyyMmDd);
        put(line, 35, codeValeur);
        put(line, 47, libelleValeur);
        put(line, 72, String.format("%8d", codeAcheteur));
        put(line, 80, libAcheteur);
        put(line, 93, String.format("%8d", codeVendeur));
        put(line, 101, libVendeur);
        put(line, 114, String.format("%10d", quantite));
        put(line, 125, String.format("%10.3f", cours));
        put(line, 135, String.format("%15.0f", prixTotalMillimes));
        return new String(line);
    }

    public static String intermediaireLine(
            int code,
            String libelleCourt,
            String libelleLong,
            String numeroCompte,
            int typeBanque,
            String adresse,
            int codeBanque) {

        char[] line = new char[260];
        Arrays.fill(line, ' ');
        put(line, 0, String.format("%11d", code));
        put(line, 12, libelleCourt);
        put(line, 35, libelleLong);
        put(line, 88, numeroCompte);
        put(line, 141, String.format("%13d", typeBanque));
        put(line, 154, adresse);
        put(line, 237, String.format("%12d", codeBanque));
        return new String(line);
    }

    public static String valeurLine(
            String dateDdMmYy,
            String codeValeur,
            String libelleValeur,
            double veille,
            double cloture) {

        char[] line = new char[70];
        Arrays.fill(line, ' ');
        put(line, 0, dateDdMmYy);
        put(line, 12, codeValeur);
        put(line, 25, libelleValeur);
        put(line, 44, String.format("%10.3f", veille));
        put(line, 54, String.format("%10.3f", cloture));
        return new String(line);
    }

    private static void put(char[] line, int start, String value) {
        if (value == null) return;
        for (int i = 0; i < value.length() && start + i < line.length; i++) {
            line[start + i] = value.charAt(i);
        }
    }
}
