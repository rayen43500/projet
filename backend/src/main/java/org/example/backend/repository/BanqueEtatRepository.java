package com.fgm.gestion.repository;
import java.time.*;
import java.util.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.fgm.gestion.model.BanqueEtat;

public interface BanqueEtatRepository extends MongoRepository<BanqueEtat, String> {
    void deleteBySeance(LocalDate seance);
    List<BanqueEtat> findBySeance(LocalDate seance);

}