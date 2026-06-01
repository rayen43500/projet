package com.fgm.gestion.service;
import java.util.*;
import com.fgm.gestion.repository.RisqueRepository;
import org.springframework.stereotype.Service;
import com.fgm.gestion.model.*;
import java.time.LocalDate;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.Table;
import java.io.ByteArrayOutputStream;
@Service
public class RisqueService {

    private final RisqueRepository risqueRepository;

    public RisqueService(RisqueRepository risqueRepository) {
        this.risqueRepository = risqueRepository;
    }

    // supprimer Risque d’une séance
    public void deleteBySeance(LocalDate seance) {
        risqueRepository.deleteBySeance(seance);
    }

    public List<Risque> getBySeance(LocalDate seance) {
        return risqueRepository.findBySeance(seance);
    }

    public byte[] generatePdfBySeance(LocalDate seance) {

        List<Risque> list = risqueRepository.findBySeance(seance);

        if (list.isEmpty()) {
            throw new RuntimeException("Aucune donnée pour cette séance");
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // TABLE (9 colonnes selon ton modèle)
            Table table = new Table(8);

            // HEADER
            table.addHeaderCell("codeIntermediaire");
            table.addHeaderCell("Intermediaire");
            table.addHeaderCell("valeur");
            table.addHeaderCell("cloture");
            table.addHeaderCell("pntj");
            table.addHeaderCell("pntj_1");
            table.addHeaderCell("risquej");
            table.addHeaderCell("risquej_1");

            // DATA
            for (Risque a : list) {
                table.addCell(String.valueOf(a.getCodeIntermediaire()));
                table.addCell(a.getIntermediaire());
                table.addCell(a.getValeur());
                table.addCell(String.valueOf(a.getCloture()));
                table.addCell(a.getPntj());
                table.addCell(a.getPntj_1());
                table.addCell(String.valueOf(a.getRisquej()));
                table.addCell(String.valueOf(a.getRisquej_1()));

            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF", e);
        }
    }

}