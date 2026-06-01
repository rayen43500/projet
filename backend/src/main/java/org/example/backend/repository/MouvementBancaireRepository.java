package com.fgm.gestion.repository;
import java.util.Optional;
import com.fgm.gestion.model.MouvementBancaire;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface MouvementBancaireRepository extends MongoRepository<MouvementBancaire, String> {

    List<MouvementBancaire> findBySeance(LocalDate seance);
    void deleteBySeance(LocalDate seance);
    Optional<MouvementBancaire> findTopByOrderBySeanceDesc();
}