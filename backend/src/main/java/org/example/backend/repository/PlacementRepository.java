package com.fgm.gestion.repository;

import com.fgm.gestion.model.Placement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PlacementRepository extends MongoRepository<Placement, String> {

    List<Placement> findBySessionDate(String sessionDate);

    List<Placement> findByIntermediaire(String intermediaire);
}