package com.fgm.gestion.repository;

import com.fgm.gestion.model.AppelRestitutionParInter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppelRestitutionRepository 
        extends MongoRepository<AppelRestitutionParInter, String> {

    List<AppelRestitutionParInter> findByDateSeance(LocalDate dateSeance);

    List<AppelRestitutionParInter> findByCodeIntermediaire(String codeIntermediaire);
    void deleteByDateSeance(LocalDate dateSeance);
}