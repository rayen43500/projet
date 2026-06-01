package com.fgm.gestion.repository;

import com.fgm.gestion.model.FgmAppUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FgmAppUserRepository extends MongoRepository<FgmAppUser, String> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<FgmAppUser> findByEmailIgnoreCase(String email);

    Optional<FgmAppUser> findByUsernameIgnoreCase(String username);
}
