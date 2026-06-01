package com.fgm.gestion.repository;
import java.util.Optional;
import com.fgm.gestion.model.Valeur;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface ValeurRepository extends MongoRepository<Valeur, LocalDate> {
List<Valeur> findBySeance(LocalDate seance);
void deleteBySeance(LocalDate seance);
}