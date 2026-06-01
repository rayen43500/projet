package com.fgm.gestion.service;
import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDate;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

   
    public void deleteBySeance(LocalDate seance) {
        transactionRepository.deleteBySeance(seance);
    }

         
  public List<Transaction> findBySeance(LocalDate date) {
    return transactionRepository.findBySeance(date);
}
}