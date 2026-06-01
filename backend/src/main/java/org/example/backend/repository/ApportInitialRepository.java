package com.fgm.gestion.repository;

import com.fgm.gestion.model.ApportInitial;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.*;

public interface ApportInitialRepository extends MongoRepository<ApportInitial, String> {

    List<ApportInitial> findBySeance(LocalDate seance);

    void deleteBySeance(LocalDate seance);

    Optional<ApportInitial> findByCodeIntermAndSeance(int codeInterm, LocalDate seance);

    ApportInitial findTopByCodeIntermAndSeanceLessThanOrderBySeanceDesc(
            int codeInterm, LocalDate seance
    );


    Optional<ApportInitial> findTopByCodeIntermAndSeanceLessThanEqualOrderBySeanceDesc(
            int codeInterm,
            LocalDate seance
    );

    Optional<ApportInitial> findTopByOrderBySeanceDesc();

}