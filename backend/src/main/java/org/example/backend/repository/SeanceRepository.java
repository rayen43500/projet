package com.fgm.gestion.repository;

import com.fgm.gestion.model.Seance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.*;

public interface SeanceRepository extends MongoRepository<Seance, String> {

    // recuperer la seance precedente directement
    Optional<Seance> findTopBySeanceLessThanOrderBySeanceDesc(LocalDate seance);
    void deleteBySeance(LocalDate seance);
    void deleteAllBySeance(LocalDate seance);
    boolean existsBySeance(LocalDate seance);

    /** Plusieurs documents possibles en base — toujours prendre le plus récent. */
    Optional<Seance> findTopBySeanceOrderByIdDesc(LocalDate seance);

    List<Seance> findAllBySeance(LocalDate seance);

    List<Seance> findBySeanceBetween(LocalDate debut, LocalDate fin);
}