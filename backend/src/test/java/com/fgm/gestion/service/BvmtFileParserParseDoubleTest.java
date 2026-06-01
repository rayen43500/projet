package com.fgm.gestion.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BvmtFileParserParseDoubleTest {

    @Test
    void parseDouble_fr_decimal_comma() {
        assertEquals(15.110, BvmtFileParser.parseDouble("15,110"), 0.000001);
    }

    @Test
    void parseDouble_en_thousands_dot_decimal_comma() {
        assertEquals(1234.567, BvmtFileParser.parseDouble("1.234,567"), 0.000001);
    }

    @Test
    void parseDouble_thousands_comma_no_decimal() {
        assertEquals(12_500_000d, BvmtFileParser.parseDouble("12,500,000"), 0.0001);
    }

    @Test
    void parseDouble_thousands_dot_no_decimal() {
        assertEquals(12_500_000d, BvmtFileParser.parseDouble("12.500.000"), 0.0001);
    }

    @Test
    void parseDouble_negative_decimal_comma() {
        assertEquals(-15.110, BvmtFileParser.parseDouble("-15,110"), 0.000001);
    }
}

