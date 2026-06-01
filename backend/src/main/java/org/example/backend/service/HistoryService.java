package com.fgm.gestion.service;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;

import org.springframework.stereotype.Service;

import java.time.*;
import java.time.LocalDate;
import java.util.*;
import java.time.format.DateTimeFormatter;

@Service
public class HistoryService {

    private final RisqueRepository risqueRepository;
    private final MouvementBancaireRepository mouvementBancaireRepository;
    private final PositionnetteRepository positionnetteRepository;
    private final SeanceRepository seanceRepository;
    private final HistoryRepository historyRepository;

    public HistoryService(RisqueRepository t,
                             MouvementBancaireRepository v,
                             PositionnetteRepository i,
                             SeanceRepository s,
                             HistoryRepository m) {
        this.risqueRepository = t;
        this.mouvementBancaireRepository = v;
        this.positionnetteRepository = i;
        this.seanceRepository = s;
        this.historyRepository = m;
    }

    public List<History> generateHistory(LocalDate date) {

        List<History> result = new ArrayList<>();

     boolean seanceExists = seanceRepository.existsBySeance(date);
       

        // Risque
        History m1 = new History();
        m1.setType("risque");
        m1.setDate(date);
        m1.setNomFichier("risque_" + date);
   

        // mouvementBancaire
        History m2 = new History();
        m2.setType("MouvementBancaire");
        m2.setDate(date);
        m2.setNomFichier("MouvementBancaire_" + date);
       
        // Positionnette
        History m3 = new History();
        m3.setType("Positionnette");
        m3.setDate(date);
        m3.setNomFichier("Positionnette_" + date);
        

        result.add(m1);
        result.add(m2);
        result.add(m3);

        historyRepository.saveAll(result);

        return result;
    }

    public List<History> getAll() {
        return historyRepository.findAll();
    }

    public void saveHistory(String nomFichier, LocalDate date) {

    boolean exists = historyRepository
            .existsByNomFichierAndDate(nomFichier, date);

    if (!exists) {
        History m = new History();
        m.setNomFichier(nomFichier);
        m.setDate(date);
      

        historyRepository.save(m);
    }
}

public void deleteByDate(LocalDate date) {
        historyRepository.deleteByDate(date);
    }
}