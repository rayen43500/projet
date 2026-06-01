package com.fgm.gestion.repository;
import java.util.Optional;
import com.fgm.gestion.model.Risque;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;


public interface RisqueRepository extends MongoRepository<Risque, String> {
   List<Risque> findBySeance(LocalDate seance); 
   void deleteBySeance(LocalDate seance);
   
}