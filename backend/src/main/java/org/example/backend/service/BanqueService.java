package com.fgm.gestion.service;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import com.fgm.gestion.model.Banque;
import com.fgm.gestion.repository.BanqueRepository;

@Service
public class BanqueService {

    private final BanqueRepository banqueRepository;

    public BanqueService(BanqueRepository banqueRepository) {
        this.banqueRepository = banqueRepository;
    }

    public Banque save(Banque banque) {
        return banqueRepository.save(banque);
    }

    public List<Banque> getAll() {
        return banqueRepository.findAll();
    }

    public List<Banque> saveAll(List<Banque> banques) {
        return banqueRepository.saveAll(banques);
    }

    public List<Banque> findBycBque(int code) {
        return banqueRepository.findBycBque(code);
    }

    public void deleteAll() {
        banqueRepository.deleteAll();
    }

public Banque addOrUpdate(Banque banque) {

    List<Banque> existantes = banqueRepository.findBycBque(banque.getcBque());

    // SI LA BANQUE EXISTE → UPDATE
    if (!existantes.isEmpty()) {

        Banque existante = existantes.get(0);

        existante.setcBqueComp(banque.getcBqueComp());
        existante.setlCourBque(banque.getlCourBque());
        existante.setlLongBque(banque.getlLongBque());
        existante.setAdrBque(banque.getAdrBque());
        existante.setFaxBque(banque.getFaxBque());
        existante.setBic(banque.getBic());
        existante.setNumCpt(banque.getNumCpt());

        return banqueRepository.save(existante);
    }

    // SINON → AJOUT
    return banqueRepository.save(banque);
}

public void deleteByCode(int code) {
    banqueRepository.deleteBycBque(code);
}
   
}