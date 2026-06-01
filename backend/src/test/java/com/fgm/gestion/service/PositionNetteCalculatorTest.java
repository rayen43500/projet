package com.fgm.gestion.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vérifie l'exemple ARTES / FINAC du document explicatif FGM.
 * Risque(J) = 15,110 × 366 × (1,06)² − 5 585,5 ≈ 628,17 DT
 * RS = |15,110 × 366 − 5 585,5| ≈ 56 DT (sans coefficient de volatilité)
 */
class PositionNetteCalculatorTest {

    private final PositionNetteCalculator calc = new PositionNetteCalculator();

    @Test
    void exempleArtesFinacRval() {
        double risque = calc.computeRisqueJour(
                15.110,
                366,
                "-",
                5585.5,
                "+",
                0.06,
                2
        );
        assertEquals(628, calc.roundRisque(risque));
    }

    @Test
    void exempleArtesFinacRsSuspens() {
        double rs = calc.computeRsSuspens(
                15.110,
                366,
                "-",
                5585.5,
                "+"
        );
        assertEquals(56, calc.roundRisque(rs));
    }

    @Test
    void rsNulQuandAucunSuspens() {
        double rs = calc.computeRsSuspens(10.0, 100, "+", 500.0, "+");
        assertEquals(0, rs, 0.001);
    }

    @Test
    void risqueNulQuandPntEtPneCrediteurs() {
        double risque = calc.computeRisqueJour(10.0, 100, "+", 500.0, "+", 0.06, 2);
        assertEquals(0, risque, 0.001);
    }

    @Test
    void pneSigneSelonDocument() {
        assertEquals("+", calc.signPne(calc.computeMontantNetteSigned(1000, 2000)));
        assertEquals("-", calc.signPne(calc.computeMontantNetteSigned(2000, 1000)));
    }

    @Test
    void cas4PntEtPneDebiteursSommeCharges() {
        double risque = calc.computeRisqueJour(10.0, 100, "-", 3000.0, "-", 0.06, 2);
        double chargeTitres = 10.0 * 100 * Math.pow(1.06, 2);
        assertEquals(chargeTitres + 3000.0, risque, 1.0);
    }

    @Test
    void cas4RsSuspensSommeValorisee() {
        double rs = calc.computeRsSuspens(10.0, 100, "-", 3000.0, "-");
        assertEquals(10.0 * 100 + 3000.0, rs, 0.01);
    }

    @Test
    void exclusivitePntPne() {
        assertTrue(calc.violatesExclusivityRule("+", "+"));
    }
}
