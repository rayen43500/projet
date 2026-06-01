package com.fgm.gestion.repository;

import com.fgm.gestion.model.Swift;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface SwiftRepository extends MongoRepository<Swift, String> {

    List<Swift> findByDatecreation(LocalDate datecreation);
    List<Swift> findByDateseance(LocalDate dateseance);
    void deleteByDateseance(LocalDate dateseance);

}