package com.fgm.gestion.repository;

import com.fgm.gestion.model.Positionnette;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDate;

public interface PositionnetteRepository extends MongoRepository<Positionnette, String> {

    //  recherche par date
    List<Positionnette> findBySeance(LocalDate seance);

    List<Positionnette> findBySeanceLessThanOrderBySeanceDesc(LocalDate seance);

    void deleteBySeance(LocalDate seance);

    List<Positionnette> findBySeanceBetween( LocalDate debutPeriode, LocalDate dateActuelle);

}