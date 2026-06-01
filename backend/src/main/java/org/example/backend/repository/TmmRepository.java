package com.fgm.gestion.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.fgm.gestion.model.Tmm;
import java.util.Optional;

public interface TmmRepository extends MongoRepository<Tmm, String> {

    Optional<Tmm> findByMoisAndAnnee(String mois, int annee);

}