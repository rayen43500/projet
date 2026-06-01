package com.fgm.gestion.service;

import com.fgm.gestion.testutil.BvmtFixedWidthBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Valide le parsing des fichiers BVMT d'exemple (ARTES/FINAC) et le rejet CSV/Excel.
 * Les fichiers sources sont dans {@code testdata/bvmt/} à la racine du projet.
 */
class BvmtSampleFilesTest {

    private static final Path BVMT_DIR = Paths.get("..", "testdata", "bvmt").toAbsolutePath().normalize();
    private static final String SEANCE = "20260115";
    private static final String SEANCE_ISO = "2026-01-15";

    private final BvmtImportValidator validator = new BvmtImportValidator();
    private final PositionNetteCalculator calc = new PositionNetteCalculator();

    @Test
    void fichiersExemplePresents() throws Exception {
        assertTrue(Files.isRegularFile(BVMT_DIR.resolve("transactions_20260115.txt")),
                "Manquant: testdata/bvmt/transactions_20260115.txt");
        assertTrue(Files.isRegularFile(BVMT_DIR.resolve("intermediaires_20260115.txt")),
                "Manquant: testdata/bvmt/intermediaires_20260115.txt");
        assertTrue(Files.isRegularFile(BVMT_DIR.resolve("valeurs_20260115.txt")),
                "Manquant: testdata/bvmt/valeurs_20260115.txt");
    }

    @Test
    void validationFichiersExempleOk() throws Exception {
        MockMultipartFile tx = file("transactions_20260115.txt");
        MockMultipartFile inter = file("intermediaires_20260115.txt");
        MockMultipartFile val = file("valeurs_20260115.txt");

        BvmtImportValidator.ValidationResult result =
                validator.validate(tx, inter, val, SEANCE_ISO);

        assertTrue(result.valid(), "Erreurs: " + result.errors());
    }

    @Test
    void scenarioArtesFinacPntPneEtRisques() {
        // Vendeur seul : 366 FINAC, montant reçu 5585,5 TND, clôture 15,110
        int qA = 0, qV = 366;
        double montantRecu = 5585.5, montantVerse = 0;
        int qNet = calc.computeQuantiteNette(qA, qV);
        double mNetSigned = calc.computeMontantNetteSigned(montantVerse, montantRecu);
        String pnt = calc.signPnt(qNet);
        String pne = calc.signPne(mNetSigned);

        assertEquals("-", pnt);
        assertEquals("+", pne);

        double rval = calc.computeRisqueJour(15.110, Math.abs(qNet), pnt, Math.abs(mNetSigned), pne, 0.06, 2);
        double rs = calc.computeRsSuspens(15.110, Math.abs(qNet), pnt, Math.abs(mNetSigned), pne);

        assertEquals(628, calc.roundRisque(rval), "R_val (risque valeur J) ARTES/FINAC");
        assertEquals(56, calc.roundRisque(rs), "RS (risque suspens) ARTES/FINAC");
    }

    @Test
    void rejetteExcel() throws Exception {
        MockMultipartFile xlsx = new MockMultipartFile(
                "transactionsFile", "transactions.xlsx", "application/vnd.ms-excel",
                new byte[]{0x50, 0x4B, 0x03, 0x04});
        MockMultipartFile inter = file("intermediaires_20260115.txt");
        MockMultipartFile val = file("valeurs_20260115.txt");

        BvmtImportValidator.ValidationResult result =
                validator.validate(xlsx, inter, val, SEANCE_ISO);

        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.toLowerCase().contains("excel")));
    }

    @Test
    void accepteIntermediairesCsv() throws Exception {
        String csv = "CODE;LIBELLESCOURT\n48;TEST INTER\n";
        MockMultipartFile inter = new MockMultipartFile(
                "intermediairesFile", "intermediaires.txt", "text/plain",
                csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Set<Integer> codes = BvmtFileParser.parseIntermediaireCodes(inter);
        assertTrue(codes.contains(48));
    }

    private MockMultipartFile file(String name) throws Exception {
        byte[] bytes = Files.readAllBytes(BVMT_DIR.resolve(name));
        return new MockMultipartFile(
                name.replace(".txt", "File"),
                name,
                "text/plain",
                bytes);
    }
}
