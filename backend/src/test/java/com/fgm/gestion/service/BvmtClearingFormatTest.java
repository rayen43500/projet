package com.fgm.gestion.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Tests format clearing BVMT réel (fichiers utilisateur 2026). */
class BvmtClearingFormatTest {

    private static final String TX_LINE =
            "0000000002 02 20260331COB  09:00:05TN0001000108MONOPRIX          "
                    + "000076900000007UNION CAP 00100000009AFC       001000000546000000009480000000005176080807             10568/2400284";

    private static final String VAL_LINE = "26/02/26;12;TN0001000108;MONOPRIX          ;    6.700;    6.700;";

    private static final String INTER_LINE =
            "         1 ; U.F.I                ; UNION FINANCIERE D'INTERMEDIATION                  ; 12 029 000 0033003187 40                           ;         81 ; Grandes Entreprises les Berges du Lac                                            ;         12";

    @Test
    void parseClearingTransaction() {
        List<BvmtFileParser.TxRow> rows = BvmtFileParser.parseAllTransactions(List.of(
                "0000000001 01 transactions clearing E",
                TX_LINE));
        assertEquals(1, rows.size());
        BvmtFileParser.TxRow r = rows.get(0);
        assertEquals("20260331", r.seanceCompact());
        assertEquals("TN0001000108", r.codeValeur());
        assertEquals("MONOPRIX", r.libelleValeur());
        assertEquals(7, r.codeVendeur());
        assertEquals(9, r.codeAcheteur());
        assertTrue(r.quantite() > 0);
    }

    @Test
    void parseValeursSemicolon() {
        var v = BvmtFileParser.parseValeurs(List.of(VAL_LINE), null);
        assertEquals(1, v.size());
        assertEquals("TN0001000108", v.get(0).getCodeValeur());
        assertEquals("MONOPRIX", v.get(0).getLibelleValeur().trim());
        assertEquals(6.7, v.get(0).getCloture(), 0.01);
    }

    @Test
    void parseIntermediairesSpacedSemicolon() {
        var inter = BvmtFileParser.parseIntermediaires(List.of(INTER_LINE));
        assertEquals(1, inter.size());
        assertEquals(1, inter.get(0).getCodeIntermediaire());
        assertEquals("U.F.I", inter.get(0).getLibelleCourt().trim());
    }

    @Test
    void detectSeanceFromValeurs() throws Exception {
        String compact = BvmtFileParser.detectDateFromValeurs(
                new org.springframework.mock.web.MockMultipartFile(
                        "v", "v.txt", "text/plain", VAL_LINE.getBytes()));
        assertEquals("20260226", compact);
    }
}
