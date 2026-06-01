package com.fgm.gestion.service;

import com.fgm.gestion.model.Provision;
import com.fgm.gestion.repository.ProvisionRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class ProvisionService {

    private final ProvisionRepository provisionRepository;

    public ProvisionService(ProvisionRepository provisionRepository) {
        this.provisionRepository = provisionRepository;
    }

    // CREATE
    public Provision save(Provision provision) {
        return provisionRepository.save(provision);
    }

    // GET ALL
    public List<Provision> getAll() {
        return provisionRepository.findAll();
    }

    // GET BY DATE
    public List<Provision> getByDate(LocalDate date) {
        return provisionRepository.findByDateCalcul(date);
    }

    // DELETE
    public void deleteAll() {
        provisionRepository.deleteAll();
    }

    public void deleteByDateCalcul(LocalDate dateCalcul) {
       provisionRepository.deleteByDateCalcul(dateCalcul);
    }

    
}