package com.fgm.gestion.repository;

import java.util.*;
import java.time.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.fgm.gestion.model.Banque;

public interface BanqueRepository extends MongoRepository<Banque, String> {

   List<Banque> findBycBque(int cBque);
  
  void deleteBycBque(int cBque);
}