package com.fgm.gestion.repository;
import java.util.Optional;
import com.fgm.gestion.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.*;
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findBySeance(LocalDate seance);
    void deleteBySeance(LocalDate seance);
}