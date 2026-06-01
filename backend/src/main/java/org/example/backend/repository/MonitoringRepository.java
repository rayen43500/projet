package com.fgm.gestion.repository;

import com.fgm.gestion.model.Monitoring;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface MonitoringRepository extends MongoRepository<Monitoring, String> {

    List<Monitoring> findByDate(LocalDate date);

    List<Monitoring> findByStatus(String status);

    boolean existsByNomFichierAndDate(String nomFichier, LocalDate date);
    void deleteByDate(LocalDate date);
}