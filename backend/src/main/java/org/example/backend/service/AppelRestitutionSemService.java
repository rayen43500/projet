package com.fgm.gestion.service;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AppelRestitutionSemService {

    private final AppelRestitutionSemRepository repo;
    private final ApportInitialRepository apportRepo;
    private final IntermediaireRepository interRepo;
    private final JourFerieRepository jourRepo;
    private final IntermediaireLookupService intermediaireLookup;

    public AppelRestitutionSemService(
            AppelRestitutionSemRepository repo,
            ApportInitialRepository apportRepo,
            IntermediaireRepository interRepo,
            JourFerieRepository jourRepo,
            IntermediaireLookupService intermediaireLookup) {

        this.repo = repo;
        this.apportRepo = apportRepo;
        this.interRepo = interRepo;
        this.jourRepo = jourRepo;
        this.intermediaireLookup = intermediaireLookup;
    }



    // gérer jours fériés
    private LocalDate adjustDate(LocalDate date) {
        List<JourFerie> jours = jourRepo.findAll();

        Set<LocalDate> set = new HashSet<>();
        for (JourFerie j : jours) {
            set.add(j.getJour());
        }

        while (set.contains(date)) {
            date = date.plusDays(1);
        }

        return date;
    }

    public int generer(LocalDate seance) {



        repo.deleteByDateSeance(seance);

        List<ApportInitial> apports = apportRepo.findBySeance(seance);

        int count = 0;

        for (ApportInitial a : apports) {

            int codeInt;
            try { codeInt = Integer.parseInt(a.getCodeInterm()); } catch(Exception e) { continue; }
            Intermediaire inter = intermediaireLookup.findForSeance(codeInt, seance);
            if (inter == null) continue;

            // calcul appel / restitution
            int appel = 0;
            int restitution = 0;

            if (a.getApportInitialAjuste() > a.getApportInitial()) {
                appel = a.getApportInitialAjuste() - a.getApportInitial();
            } else {
                restitution = a.getApportInitial() - a.getApportInitialAjuste();
            }

            AppelRestitutionSem obj = new AppelRestitutionSem();

            obj.setDateSeance(seance);
            obj.setCodeIntr(a.getCodeInterm());
            obj.setIntermediaire(inter.getLibelleLong());

            obj.setAdresse(inter.getAdresse());
            obj.setNumeroCpt(inter.getNumeroCompte());

            obj.setFax(""); // si banque non utilisée ici

            obj.setApportInit(a.getApportInitial());
            obj.setApportInitAjuste(a.getApportInitialAjuste());

            obj.setAppel(appel);
            obj.setRestitution(restitution);

            //date valeur = J+1 corrigé
            LocalDate valeur = adjustDate(seance.plusDays(1));
            obj.setSeanceValeur(valeur);

            repo.save(obj);
            count++;
        }

        return count;
    }

    public List<AppelRestitutionSem> getAll() {
        return repo.findAll();
    }
}