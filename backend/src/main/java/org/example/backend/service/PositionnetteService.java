package com.fgm.gestion.service;
import java.util.*;
import com.fgm.gestion.repository.PositionnetteRepository;
import org.springframework.stereotype.Service;
import com.fgm.gestion.model.*;
import java.time.LocalDate;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.Table;
import java.io.ByteArrayOutputStream;
@Service
public class PositionnetteService {

    private final PositionnetteRepository positionnetteRepository;

    public PositionnetteService(PositionnetteRepository positionnetteRepository) {
        this.positionnetteRepository = positionnetteRepository;
    }

    // supprimer Positionnette d’une séance
    public void deleteBySeance(LocalDate seance) {
        positionnetteRepository.deleteBySeance(seance);
    }

    public List<Positionnette> getBySeance(LocalDate seance) {
        return positionnetteRepository.findBySeance(seance);
    }

    public byte[] generatePdfBySeance(LocalDate seance) {

        List<Positionnette> list = positionnetteRepository.findBySeance(seance);

        if (list.isEmpty()) {
            throw new RuntimeException("Aucune donnée pour cette séance");
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Table table = new Table(8);

            // HEADER
            table.addHeaderCell("codeIntermediaire");
            table.addHeaderCell("Intermediaire");
            table.addHeaderCell("valeur");
            table.addHeaderCell("cloture");
            table.addHeaderCell("quantitenette");
            table.addHeaderCell("montantnette");
            table.addHeaderCell("pnt");
            table.addHeaderCell("pne");

            // DATA
            for (Positionnette a : list) {
                table.addCell(String.valueOf(a.getCodeIntermediaire()));
                table.addCell(a.getIntermediaire());
                table.addCell(a.getValeur());
                table.addCell(String.valueOf(a.getCloture()));
                table.addCell(String.valueOf(a.getQuantiteNette()));
                table.addCell(String.valueOf(a.getMontantNette()));
                table.addCell(a.getPnt());
                table.addCell(a.getPne());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF", e);
        }
    }

}