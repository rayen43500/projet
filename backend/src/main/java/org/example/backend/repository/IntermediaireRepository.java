package com.fgm.gestion.repository;
import java.util.*;
import java.time.*;
import com.fgm.gestion.model.Intermediaire;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IntermediaireRepository extends MongoRepository<Intermediaire, String> {

    Optional<Intermediaire> findById( String id);
    List<Intermediaire> findByDateImport( LocalDate id);
    void deleteByDateImport(LocalDate dateImport);
    List<Intermediaire> findByCodeIntermediaireAndDateImport(int codeIntermediaire, LocalDate dateImport);
    java.util.Optional<Intermediaire> findTopByCodeIntermediaireAndDateImportLessThanEqualOrderByDateImportDesc(
            int codeIntermediaire, LocalDate dateImport);
}