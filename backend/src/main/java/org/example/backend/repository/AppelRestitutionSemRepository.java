package com.fgm.gestion.repository;

import com.fgm.gestion.model.AppelRestitutionSem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppelRestitutionSemRepository extends MongoRepository<AppelRestitutionSem, String> {

    List<AppelRestitutionSem> findByDateSeance(LocalDate dateSeance);

    void deleteByDateSeance(LocalDate dateSeance);
}