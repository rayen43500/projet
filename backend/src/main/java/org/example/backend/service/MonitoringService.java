package com.fgm.gestion.service;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;

import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class MonitoringService {

    private final TransactionRepository transactionRepository;
    private final ValeurRepository valeurRepository;
    private final IntermediaireRepository intermediaireRepository;
    private final SeanceRepository seanceRepository;
    private final MonitoringRepository monitoringRepository;

    public MonitoringService(TransactionRepository t,
                             ValeurRepository v,
                             IntermediaireRepository i,
                             SeanceRepository s,
                             MonitoringRepository m) {
        this.transactionRepository = t;
        this.valeurRepository = v;
        this.intermediaireRepository = i;
        this.seanceRepository = s;
        this.monitoringRepository = m;
    }

    public List<Monitoring> generateMonitoring(LocalDate date) {

        List<Monitoring> result = new ArrayList<>();

      boolean seanceExists = seanceRepository.existsBySeance(date);

        String status = seanceExists ? "TRAITE" : "NON_TRAITE";

        // Transaction
        Monitoring m1 = new Monitoring();
        m1.setType("transaction");
        m1.setDate(date);
        m1.setNomFichier("transaction_" + date);
        m1.setStatus(status);

        // Valeur
        Monitoring m2 = new Monitoring();
        m2.setType("valeur");
        m2.setDate(date);
        m2.setNomFichier("valeur_" + date);
        m2.setStatus(status);

        // Intermediaire
        Monitoring m3 = new Monitoring();
        m3.setType("intermediaire");
        m3.setDate(date);
        m3.setNomFichier("intermediaire_" + date);
        m3.setStatus(status);

        result.add(m1);
        result.add(m2);
        result.add(m3);

        monitoringRepository.saveAll(result);

        return result;
    }

    public List<Monitoring> getAll() {
        return monitoringRepository.findAll();
    }

    public void saveMonitoring(String nomFichier, LocalDate date, String status) {

    boolean exists = monitoringRepository
            .existsByNomFichierAndDate(nomFichier, date);

    if (!exists) {
        Monitoring m = new Monitoring();
        m.setNomFichier(nomFichier);
        m.setDate(date);
        m.setStatus(status);

        monitoringRepository.save(m);
    }
}
 public void deleteByDate(LocalDate date) {
        monitoringRepository.deleteByDate(date);
    }

}