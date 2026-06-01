package com.fgm.gestion.service;

import com.fgm.gestion.repository.ValeurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import com.fgm.gestion.model.*;

@Service
public class ValeurService {

    private final ValeurRepository valeurRepository;

    public ValeurService(ValeurRepository valeurRepository) {
        this.valeurRepository = valeurRepository;
    }

    // supprimer valeurs d’une séance
    public void deleteBySeance(LocalDate seance) {
        valeurRepository.deleteBySeance(seance);
    }
     // supprimer valeurs d’une séance
  public List<Valeur> findBySeance(LocalDate date) {
    return valeurRepository.findBySeance(date);
}
}