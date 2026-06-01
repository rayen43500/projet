package com.fgm.gestion.repository;
import java.util.*;
import java.time.*;
import com.fgm.gestion.model.JourFerie;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JourFerieRepository extends MongoRepository<JourFerie, String> {


List<JourFerie> findByJour( LocalDate jour); 
boolean existsByJour(LocalDate jour);
}