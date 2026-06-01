package com.fgm.gestion.repository;

import com.fgm.gestion.model.Provision;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProvisionRepository extends MongoRepository<Provision, String> {

    // rechercher par id ADC
    List<Provision> findByIdAdc(int idAdc);

    List<Provision> findByDateCalculBetween(LocalDate debut, LocalDate fin);
    List<Provision> findByDateCalculLessThan(LocalDate seance);
    // rechercher par date
    List<Provision> findByDateCalcul(LocalDate dateCalcul);
    Optional<Provision> findByIdAdcAndDateCalcul(int idAdc, LocalDate dateCalcul);
    // dernier provision avant une date (par idAdc)
    Optional<Provision> findTopByIdAdcAndDateCalculLessThanOrderByDateCalculDesc(
            int idAdc,
            LocalDate date
    );

    void deleteByDateCalcul(LocalDate date);

    // Dernières 6 provisions d'un intermédiaire
    List<Provision>
    findTop6ByIdAdcOrderByDateCalculDesc(int idAdc);

}