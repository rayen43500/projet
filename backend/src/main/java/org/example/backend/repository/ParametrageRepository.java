package com.fgm.gestion.repository;

import com.fgm.gestion.model.Parametrage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ParametrageRepository extends MongoRepository<Parametrage, String> {
    
}