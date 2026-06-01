package com.fgm.gestion.service;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppelRestitutionService {

    private final AppelRestitutionRepository appelRepo;
    private final IntermediaireRepository interRepo;
    private final BanqueRepository banqueRepo;
    private final MouvementBancaireRepository mouvRepo;
    private final IntermediaireLookupService intermediaireLookup;

    public AppelRestitutionService(
            AppelRestitutionRepository appelRepo,
            IntermediaireRepository interRepo,
            BanqueRepository banqueRepo,
            MouvementBancaireRepository mouvRepo,
            IntermediaireLookupService intermediaireLookup) {

        this.appelRepo = appelRepo;
        this.interRepo = interRepo;
        this.banqueRepo = banqueRepo;
        this.mouvRepo = mouvRepo;
        this.intermediaireLookup = intermediaireLookup;
    }

    public AppelRestitutionParInter save(AppelRestitutionParInter obj) {
        return appelRepo.save(obj);
    }

    public List<AppelRestitutionParInter> getAll() {
        return appelRepo.findAll();
    }

    public List<AppelRestitutionParInter> getBySeance(LocalDate date) {
        return appelRepo.findByDateSeance(date);
    }

    public void deleteAll() {
        appelRepo.deleteAll();
    }
    public void deleteBySeance(LocalDate date) {
        appelRepo.deleteByDateSeance(date);
    }

    private String normalizeCode(String code) {
        if (code == null) return null;
        return code.replaceFirst("^0+", "");
    }

    public void genererAppelRestitution(LocalDate seance) {


        appelRepo.deleteByDateSeance(seance);

        List<MouvementBancaire> mouvements = mouvRepo.findBySeance(seance);

        for (MouvementBancaire m : mouvements) {

            int codeInt = m.getCodeIntermediaire();

            Intermediaire inter = intermediaireLookup.findForSeance(codeInt, seance);

            if (inter == null) {
                System.out.println("Intermediaire introuvable: " + m.getCodeIntermediaire());
                continue;
            }


            List<Banque> banques = banqueRepo.findBycBque(inter.getCodeBanque());
            if (banques.isEmpty()) {
                System.out.println("Banque introuvable: " + inter.getCodeBanque());
                continue;
            }

            Banque banque = banques.get(0);

            if (banque == null) continue;

            AppelRestitutionParInter appel = new AppelRestitutionParInter();

            appel.setIntermediaire(inter.getLibelleLong());
            appel.setCodeIntermediaire(String.valueOf(inter.getCodeIntermediaire()));
            appel.setAdresse(inter.getAdresse());
            appel.setFax(banque.getFaxBque());

            appel.setDateSeance(seance);
            appel.setDateValeur(seance.plusDays(1));

            appel.setRisque( m.getTotal());
            appel.setProvision( m.getProvision());
            appel.setRestitution( m.getRestitution());
            appel.setAppel( m.getAppel());


            appel.setNumeroCompte(inter.getNumeroCompte());

            appelRepo.save(appel);
        }
    }
}