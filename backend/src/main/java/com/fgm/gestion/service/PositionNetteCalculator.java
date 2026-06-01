package com.fgm.gestion.service;

import org.springframework.stereotype.Service;

/**
 * Logique métier Position Nette (document explicatif FGM / cahier des charges BVMT).
 *
 * PNT = titres achetés − titres vendus
 * PNE = montant versé − montant reçu
 */
@Service
public class PositionNetteCalculator {

    public int computeQuantiteNette(int quantiteAchete, int quantiteVendu) {
        return quantiteAchete - quantiteVendu;
    }

    /** PNE signé : versé − reçu */
    public double computeMontantNetteSigned(double montantVerse, double montantRecu) {
        double diff = montantVerse - montantRecu;
        return Math.round(diff * 1000.0) / 1000.0;
    }

    public String signPnt(int quantiteNette) {
        return quantiteNette >= 0 ? "+" : "-";
    }

    /** PNE créditeur (+) si reçu > versé, débiteur (−) si versé > reçu */
    public String signPne(double montantNetteSigned) {
        if (montantNetteSigned > 0) return "-";
        if (montantNetteSigned < 0) return "+";
        return "+";
    }

    public boolean violatesExclusivityRule(String pntSign, String pneSign) {
        return "+".equals(pntSign) && "+".equals(pneSign);
    }

    /**
     * Risque de marché sur une valeur (RV) — 4 cas du cahier des charges :
     * 1) PNT+ PNE+ → 0
     * 2) PNT+ PNE- → |PNE + PNT×C×(1−D)^p|
     * 3) PNT- PNE+ → |PNE + PNT×C×(1+D)^p|  (doc. explicatif : C×|PNT|×(1+D)^n − |PNE|)
     * 4) PNT- PNE- → |PNE + PNT×C×(1+D)^p| = somme des charges
     */
    public double computeRisqueJour(
            double cloture,
            int quantiteNetteAbs,
            String pntSign,
            double montantNetteAbs,
            String pneSign,
            double seuilDecimal,
            int periodsRemaining) {

        if (violatesExclusivityRule(pntSign, pneSign)) {
            return 0;
        }

        double M = montantNetteAbs;
        int p = Math.max(0, periodsRemaining);
        double D = seuilDecimal;
        double C = cloture;
        double Q = quantiteNetteAbs;

        // Cas 1 : aucun risque de contrepartie net
        if ("+".equals(pntSign) && "+".equals(pneSign)) {
            return 0;
        }

        // Risque espèces seul (PNT nul)
        if (Q == 0 && "-".equals(pneSign)) {
            return M;
        }

        if (p == 0 || Q == 0) {
            if ("-".equals(pneSign)) return M;
            return 0;
        }

        // Cas 2 : PNT+ PNE-
        if ("+".equals(pntSign) && "-".equals(pneSign)) {
            double produit = C * Q * Math.pow(1 - D, p);
            return Math.abs(-M + produit);
        }

        // Cas 3 : PNT- PNE+
        if ("-".equals(pntSign) && "+".equals(pneSign)) {
            double chargeTitres = C * Q * Math.pow(1 + D, p);
            return Math.abs(chargeTitres - M);
        }

        // Cas 4 : PNT- PNE- (somme des charges)
        if ("-".equals(pntSign) && "-".equals(pneSign)) {
            double chargeTitres = C * Q * Math.pow(1 + D, p);
            return chargeTitres + M;
        }

        return 0;
    }

    public int roundRisque(double risque) {
        return (int) Math.round(Math.max(0, risque));
    }

    /**
     * RS — risque suspens au dernier cours connu (sans coefficient de volatilité).
     * Cahier des charges : |SV + RE| valorisé au cours courant.
     */
    public double computeRsSuspens(
            double cloture,
            int quantiteNetteAbs,
            String pntSign,
            double montantNetteAbs,
            String pneSign) {

        if (violatesExclusivityRule(pntSign, pneSign)) {
            return 0;
        }
        if (!"-".equals(pntSign) && !"-".equals(pneSign)) {
            return 0;
        }
        if (quantiteNetteAbs == 0 && montantNetteAbs == 0) {
            return 0;
        }
        if ("-".equals(pntSign) && "-".equals(pneSign)) {
            return cloture * quantiteNetteAbs + montantNetteAbs;
        }
        return Math.abs(cloture * quantiteNetteAbs - montantNetteAbs);
    }
}
