package com.fgm.gestion.service;

import com.fgm.gestion.service.BvmtFileParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BvmtClearingCodeValueRegexTest {

    @Test
    void parseClearingCodeValueShouldNotConsumeLibelle() {
        // Le code valeur doit ressortir exactement "TN0001000108"
        // même si le libellé débute directement après le code.
        String txLine =
                "0000000002 02 20260330COB  09:00:23TN0001000108MONOPRIX          " +
                "000025700000024TUN VAL   00100000018MAC       0010000002000000000090800000000018160002906866         61020";

        List<BvmtFileParser.TxRow> rows = BvmtFileParser.parseAllTransactions(List.of(
                "0000000001 01 transactions clearing E",
                txLine));

        assertEquals(1, rows.size());
        assertEquals("TN0001000108", rows.get(0).codeValeur());
        assertTrue(rows.get(0).libelleValeur().startsWith("MONOPRIX"));
    }
}

