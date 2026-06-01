package com.fgm.gestion.service;

import com.fgm.gestion.model.Placement;
import com.fgm.gestion.repository.PlacementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlacementService {

    private final PlacementRepository repository;

    public PlacementService(PlacementRepository repository) {
        this.repository = repository;
    }

    // CREATE
    public Placement save(Placement p) {
        return repository.save(p);
    }

    // GET ALL
    public List<Placement> getAll() {
        return repository.findAll();
    }

    // GET BY DATE (String dd/MM/yyyy)
    public List<Placement> getByDate(String date) {
        return repository.findBySessionDate(date);
    }

    // GET BY INTERMEDIAIRE
    public List<Placement> getByIntermediaire(String name) {
        return repository.findByIntermediaire(name);
    }
public Placement update(String id, Placement newPlacement) {
    return repository.findById(id).map(p -> {

        p.setSessionDate(newPlacement.getSessionDate());
        p.setCodeIntermediaire(newPlacement.getCodeIntermediaire());
        p.setIntermediaire(newPlacement.getIntermediaire());
        p.setMtProv(newPlacement.getMtProv());

        p.setMontantSaisi(newPlacement.getMontantSaisi());
        p.setCumule(newPlacement.getCumule());
        p.setSoldePlace(newPlacement.getSoldePlace());
        p.setTotalProvision(newPlacement.getTotalProvision());
        p.setTotalCumule(newPlacement.getTotalCumule());
        p.setTotalMontantSaisi(newPlacement.getTotalMontantSaisi());
        p.setTotalSoldePlace(newPlacement.getTotalSoldePlace());
        p.setDivers(newPlacement.getDivers());
        p.setTotalGeneral(newPlacement.getTotalGeneral());
        p.setInteret(newPlacement.getInteret());

        return repository.save(p);

    }).orElseThrow(() -> new RuntimeException("Placement non trouvé"));
}


public List<Placement> updateBulk(List<Placement> placements) {

    return placements.stream().map(p -> {

        Placement existing = repository.findById(p.getId())
            .orElseThrow(() -> new RuntimeException("Placement non trouvé : " + p.getId()));

        // Champs principaux
        existing.setSessionDate(p.getSessionDate());
        existing.setCodeIntermediaire(p.getCodeIntermediaire());
        existing.setIntermediaire(p.getIntermediaire());
        existing.setMtProv(p.getMtProv());

        // Champs calculés
        existing.setMontantSaisi(p.getMontantSaisi());
        existing.setCumule(p.getCumule());
        existing.setSoldePlace(p.getSoldePlace());

        existing.setTotalProvision(p.getTotalProvision());
        existing.setTotalCumule(p.getTotalCumule());
        existing.setTotalMontantSaisi(p.getTotalMontantSaisi());
        existing.setTotalSoldePlace(p.getTotalSoldePlace());

        existing.setDivers(p.getDivers());
        existing.setTotalGeneral(p.getTotalGeneral());
        existing.setInteret(p.getInteret());

        return repository.save(existing);

    }).toList();
}




}