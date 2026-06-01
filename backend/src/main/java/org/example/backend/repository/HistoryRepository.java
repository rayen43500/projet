package com.fgm.gestion.repository;

import com.fgm.gestion.model.History;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface HistoryRepository extends MongoRepository<History, String> {

    List<History> findByDate(LocalDate date);
    boolean existsByNomFichierAndDate(String nomFichier, LocalDate date);
    void deleteByDate(LocalDate date);
}