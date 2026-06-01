package com.fgm.gestion.service;
import java.util.*;
import com.fgm.gestion.repository.MouvementBancaireRepository;
import org.springframework.stereotype.Service;
import com.fgm.gestion.model.*;
import java.time.LocalDate;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.Table;

import java.io.ByteArrayOutputStream;
@Service
public class MouvementBancaireService {

    private final MouvementBancaireRepository mouvementBancaireRepository;

    public MouvementBancaireService(MouvementBancaireRepository mouvementBancaireRepository) {
        this.mouvementBancaireRepository = mouvementBancaireRepository;
    }

    // supprimer mouvementBancaire d’une séance
    public void deleteBySeance(LocalDate seance) {
        mouvementBancaireRepository.deleteBySeance(seance);
    }

    public List<MouvementBancaire> getBySeance(LocalDate seance) {
        return mouvementBancaireRepository.findBySeance(seance);
    }

    public byte[] generatePdfBySeance(LocalDate seance) {

        List<MouvementBancaire> list = mouvementBancaireRepository.findBySeance(seance);

        if (list.isEmpty()) {
            throw new RuntimeException("Aucune donnée pour cette séance");
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // TABLE
            Table table = new Table(9);

            // HEADER
            table.addHeaderCell("Code Inter");
            table.addHeaderCell("Intermediaire");
            table.addHeaderCell("Total Seance");
            table.addHeaderCell("Total Seance Prec");
            table.addHeaderCell("Total");
            table.addHeaderCell("Provision");
            table.addHeaderCell("Difference %");
            table.addHeaderCell("Appel");
            table.addHeaderCell("Restitution");

            // DATA
            for (MouvementBancaire a : list) {
                table.addCell(String.valueOf(a.getCodeIntermediaire()));
                table.addCell(a.getIntermediaire());
                table.addCell(String.valueOf(a.getTotalSeance()));
                table.addCell(String.valueOf(a.getTotalSeancePrecedent()));
                table.addCell(String.valueOf(a.getTotal()));
                table.addCell(String.valueOf(a.getProvision()));
                table.addCell(String.valueOf(a.getDifference()));
                table.addCell(String.valueOf(a.getAppel()));
                table.addCell(String.valueOf(a.getRestitution()));
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF", e);
        }
    }

    public List<MouvementBancaire> getLastSeanceEtat() {

        Optional<MouvementBancaire> last = mouvementBancaireRepository.findTopByOrderBySeanceDesc();

        if (last.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate lastSeance = last.get().getSeance();

        return mouvementBancaireRepository.findBySeance(lastSeance);
    }


    public List<Map<String, Object>> getCourbeMontantsByIntermediaire(LocalDate seance) {

        List<MouvementBancaire> data = mouvementBancaireRepository.findBySeance(seance);

        return data.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("intermediaire", m.getIntermediaire());
            map.put("codeIntermediaire", m.getCodeIntermediaire());
            map.put("total", m.getTotal());
            map.put("provision", m.getProvision());
            return map;
        }).toList();
    }

}