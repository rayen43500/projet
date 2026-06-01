package com.fgm.gestion.repository;

import com.fgm.gestion.model.MvtBanqueInter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface MvtBanqueInterRepository 
        extends MongoRepository<MvtBanqueInter, String> {

    List<MvtBanqueInter> findByDateSeance(LocalDate dateSeance);

    List<MvtBanqueInter> findByCodeInterm(String codeInterm);
    void deleteByDateSeance(LocalDate dateSeance);
}