package com.fgm.gestion.service;
import java.time.*;
import org.springframework.stereotype.Service;
import java.util.List;
import com.fgm.gestion.model.JourFerie;
import com.fgm.gestion.repository.JourFerieRepository;

@Service
public class JourFerieService {

    private final JourFerieRepository JourFerieRepository;

    public JourFerieService(JourFerieRepository JourFerieRepository) {
        this.JourFerieRepository = JourFerieRepository;
    }

    public JourFerie save(JourFerie JourFerie) {
        return JourFerieRepository.save(JourFerie);
    }

    public List<JourFerie> getAll() {
        return JourFerieRepository.findAll();
    }

    public List<JourFerie> saveAll(List<JourFerie> JourFeries) {
        return JourFerieRepository.saveAll(JourFeries);
    }

    public List<JourFerie> findByJour(LocalDate jour) {
        return JourFerieRepository.findByJour(jour);
    }

    public void deleteAll() {
        JourFerieRepository.deleteAll();
    }
}