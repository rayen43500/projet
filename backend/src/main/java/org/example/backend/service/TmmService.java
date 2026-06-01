package com.fgm.gestion.service;

import org.springframework.stereotype.Service;
import java.util.*;
import com.fgm.gestion.model.Tmm;
import com.fgm.gestion.repository.TmmRepository;

@Service
public class TmmService {

    private final TmmRepository tmmRepository;

    public TmmService(TmmRepository tmmRepository) {
        this.tmmRepository = tmmRepository;
    }

    //  Normalisation des mois (corrige les erreurs d'encodage)
    private String normalizeMois(String mois) {

        if (mois == null) return null;

        mois = mois.toLowerCase();

        switch (mois) {
            case "aoÃ»t":
            case "aout":
                return "aout";

            case "dÃ©cembre":
            case "decembre":
                return "decembre";

            case "fÃ©vrier":
            case "fevrier":
                return "fevrier";

            default:
                return mois;
        }
    }

    //  Ajouter ou mettre à jour un TMM
    public Tmm save(Tmm tmm) {

        String moisNormalise = normalizeMois(tmm.getMois());
        tmm.setMois(moisNormalise);

        Optional<Tmm> exist = tmmRepository
                .findByMoisAndAnnee(moisNormalise, tmm.getAnnee());

        if (exist.isPresent()) {
            Tmm existing = exist.get();
            existing.setTmm(tmm.getTmm());
            return tmmRepository.save(existing);
        }

        return tmmRepository.save(tmm);
    }

    //  Récupérer tous les TMM
    public List<Tmm> getAll() {
        return tmmRepository.findAll();
    }

    // Récupérer par mois + année
    public Optional<Tmm> getByMoisAndAnnee(String mois, int annee) {
        return tmmRepository.findByMoisAndAnnee(normalizeMois(mois), annee);
    }

    //  Supprimer
    public void delete(String id) {
        tmmRepository.deleteById(id);
    }
}