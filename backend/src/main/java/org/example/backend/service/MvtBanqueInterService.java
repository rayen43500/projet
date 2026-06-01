package com.fgm.gestion.service;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MvtBanqueInterService {

    private final MvtBanqueInterRepository mvtRepo;
    private final IntermediaireRepository interRepo;
    private final BanqueRepository banqueRepo;
    private final MouvementBancaireRepository mouvRepo;
    private final IntermediaireLookupService intermediaireLookup;

    public MvtBanqueInterService(
            MvtBanqueInterRepository mvtRepo,
            IntermediaireRepository interRepo,
            BanqueRepository banqueRepo,
            MouvementBancaireRepository mouvRepo,
            IntermediaireLookupService intermediaireLookup) {

        this.mvtRepo = mvtRepo;
        this.interRepo = interRepo;
        this.banqueRepo = banqueRepo;
        this.mouvRepo = mouvRepo;
        this.intermediaireLookup = intermediaireLookup;
    }

    public MvtBanqueInter save(MvtBanqueInter obj) {
        return mvtRepo.save(obj);
    }

    public List<MvtBanqueInter> getAll() {
        return mvtRepo.findAll();
    }

    public List<MvtBanqueInter> getBySeance(LocalDate date) {
        return mvtRepo.findByDateSeance(date);
    }

    public void deleteAll() {
        mvtRepo.deleteAll();
    }

    public void deleteBySeance(LocalDate date) {
        mvtRepo.deleteByDateSeance(date);
    }

    private String normalizeCode(String code) {
        if (code == null) return null;
        return code.replaceFirst("^0+", "");
    }

    public void genererMvtBanque(LocalDate seance) {


        mvtRepo.deleteByDateSeance(seance);

        List<MouvementBancaire> mouvements = mouvRepo.findBySeance(seance);

        for (MouvementBancaire m : mouvements) {


            int codeInt = m.getCodeIntermediaire();

            Intermediaire inter = intermediaireLookup.findForSeance(codeInt, seance);

            if (inter == null) continue;

            List<Banque> banques = banqueRepo.findBycBque(inter.getCodeBanque());

            if (banques.isEmpty()) continue;

            Banque banque = banques.get(0);

            if (banque == null) continue;

            MvtBanqueInter mvt = new MvtBanqueInter();

            mvt.setIntermediaire(inter.getLibelleLong());
            mvt.setCodeInterm(String.valueOf(inter.getCodeIntermediaire()));
            mvt.setBanque(banque.getlCourBque());
            mvt.setNumeroCompte(inter.getNumeroCompte());

            mvt.setDateSeance(seance);
            mvt.setDateValeur(seance.plusDays(1));

            double debit = m.getAppel();
            double credit = m.getRestitution();

            mvt.setDebit(debit);
            mvt.setCredit(credit);

            double total = credit - debit;
            mvt.setTotal(total);

            mvt.setSoldeCredit(Math.max(total, 0));
            mvt.setCBQUECOMP(banque.getcBqueComp());

            mvtRepo.save(mvt);
        }
    }
}