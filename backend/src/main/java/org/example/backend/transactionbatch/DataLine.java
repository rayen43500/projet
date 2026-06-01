package com.fgm.gestion.transactionbatch;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
public record DataLine(
        String seance,
        String codeValeur,
        String vendeur,
        String acheteur,
        String produit,
        int quantite,
        double prixTotal,
        int code_vend,
        int code_ach,
        double coursTransaction
) {}