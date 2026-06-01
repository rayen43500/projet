package com.fgm.gestion.bootstrap;

import com.fgm.gestion.model.ApportInitial;
import com.fgm.gestion.model.FgmAppUser;
import com.fgm.gestion.model.Intermediaire;
import com.fgm.gestion.model.Parametrage;
import com.fgm.gestion.model.Seance;
import com.fgm.gestion.repository.ApportInitialRepository;
import com.fgm.gestion.repository.FgmAppUserRepository;
import com.fgm.gestion.repository.IntermediaireRepository;
import com.fgm.gestion.repository.ParametrageRepository;
import com.fgm.gestion.repository.SeanceRepository;
import com.fgm.gestion.service.SeanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Seed Mongo au démarrage.
 * Rôles : ADMIN (paramétrage, lecture totale) | USER (import, préparation séance).
 * Identifiants démo : admin/admin et user/user.
 */
@Component
@Order(10)
public class FgmMongoDataSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FgmMongoDataSeed.class);

    private final FgmAppUserRepository userRepo;
    private final IntermediaireRepository intermRepo;
    private final ParametrageRepository paramRepo;
    private final ApportInitialRepository apportRepo;
    private final SeanceRepository seanceRepo;
    private final SeanceService seanceService;
    private final PasswordEncoder encoder;

    @Value("${fgm.seed.enabled:true}")
    private boolean seedEnabled;

    /** Faux intermédiaires 101/102 — désactivé par défaut (utiliser le fichier BVMT). */
    @Value("${fgm.seed.demo-intermediaires:false}")
    private boolean demoIntermediaires;

    public FgmMongoDataSeed(FgmAppUserRepository userRepo,
                            IntermediaireRepository intermRepo,
                            ParametrageRepository paramRepo,
                            ApportInitialRepository apportRepo,
                            SeanceRepository seanceRepo,
                            SeanceService seanceService,
                            PasswordEncoder encoder) {
        this.userRepo  = userRepo;
        this.intermRepo = intermRepo;
        this.paramRepo = paramRepo;
        this.apportRepo = apportRepo;
        this.seanceRepo = seanceRepo;
        this.seanceService = seanceService;
        this.encoder   = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureParametrage();
        dedupeAllSeances();
        if (!seedEnabled) {
            purgeDemoIntermediaires();
            return;
        }
        seedUsers();
        if (demoIntermediaires) {
            seedDemoIntermediaires();
        } else {
            purgeDemoIntermediaires();
        }
    }

    /** Corrige les doublons Mongo « non unique result » sur findBySeance. */
    private void dedupeAllSeances() {
        seanceRepo.findAll().stream()
                .map(Seance::getSeance)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(seanceService::dedupeSeances);
    }

    /** Retire DEMO-101/102 créés par un ancien seed. */
    private void purgeDemoIntermediaires() {
        List<Intermediaire> demo = intermRepo.findAll().stream()
                .filter(i -> "seed".equalsIgnoreCase(i.getNomFichier()))
                .toList();
        if (!demo.isEmpty()) {
            intermRepo.deleteAll(demo);
            log.info("FGM: {} intermédiaire(s) démo supprimé(s) — importez intermediaires.txt ou .json BVMT", demo.size());
        }
    }

    /** Valeurs par défaut du document explicatif — créées uniquement si aucun parametrage n'existe. */
    private void ensureParametrage() {
        if (paramRepo.count() > 0) return;
        Parametrage p = new Parametrage();
        p.setSeuil_var_1(10);
        p.setSeuil_var_2(6.0);
        p.setSeuil_var_3(6);      // 6 % — coefficient majoration risque
        p.setSeuil_dep_pro(10);   // 10 % — seuil appel de marge
        p.setDep_risq(25000.0);   // 25 000 DT — seuil restitution
        p.setMin_contr_init(0);
        p.setDel_reg_liv(2);      // J+2
        p.setDel_reg_DT(2);
        p.setDel_reg_DE(2);
        p.setTaux(0.0);
        p.setBenefice(0.0);
        paramRepo.save(p);
        log.info("FGM: parametrage par défaut créé (seuil=6%, del=J+2, appel>10%, restit>25000 DT)");
    }

    private void seedUsers() {
        record Seed(String email, String username, String fullName, List<String> roles, String pwd, Integer codeInter) {}
        var seeds = List.of(
                new Seed("admin@fgm.local", "admin", "Administrateur FGM",
                        List.of("ADMIN_FGM", "ADMIN", "SUPERVISEUR"), "Admin123!", null),
                new Seed("superviseur@fgm.local", "superviseur", "Superviseur FGM",
                        List.of("SUPERVISEUR", "USER"), "Super123!", null),
                new Seed("inter@fgm.local", "inter101", "Intermédiaire 101",
                        List.of("INTERMEDIAIRE", "USER"), "Inter123!", 101)
        );
        int created = 0;
        for (var s : seeds) {
            Optional<FgmAppUser> ex = userRepo.findByEmailIgnoreCase(s.email());
            if (ex.isPresent()) {
                FgmAppUser u = ex.get();
                boolean changed = false;
                if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
                    u.setPasswordHash(encoder.encode(s.pwd()));
                    changed = true;
                }
                if (s.codeInter() != null && u.getCodeIntermediaire() == null) {
                    u.setCodeIntermediaire(s.codeInter());
                    changed = true;
                }
                if (!u.getRoles().containsAll(s.roles())) {
                    u.setRoles(s.roles());
                    changed = true;
                }
                if (changed) userRepo.save(u);
                continue;
            }
            FgmAppUser nu = new FgmAppUser(s.email(), s.username(), s.fullName(), s.roles(), s.codeInter());
            nu.setPasswordHash(encoder.encode(s.pwd()));
            userRepo.save(nu);
            created++;
        }
        if (created > 0)
            log.info("FGM seed: {} compte(s) — admin@fgm.local / Admin123!", created);
    }

    private void seedDemoIntermediaires() {
        LocalDate today = LocalDate.now();
        if (!intermRepo.findByDateImport(today).isEmpty()) return;
        intermRepo.saveAll(List.of(
                demo("101","DEMO-101","Intermédiaire démo 101", today),
                demo("102","DEMO-102","Intermédiaire démo 102", today)
        ));
        seedApportInitial(today, 101);
        seedApportInitial(today, 102);
        log.info("FGM seed: intermédiaires démo (101,102) + apports initiaux pour {}", today);
    }

    private void seedApportInitial(LocalDate seance, int codeInter) {
        if (apportRepo.findByCodeIntermAndSeance(codeInter, seance).isPresent()) return;
        ApportInitial a = new ApportInitial();
        a.setSeance(seance);
        a.setCodeInterm(String.valueOf(codeInter));
        a.setApportInitial(50000);
        a.setApportInitialAjuste(50000);
        apportRepo.save(a);
    }

    private Intermediaire demo(String code, String court, String long_, LocalDate d) {
        Intermediaire i = new Intermediaire();
        try { i.setCodeIntermediaire(Integer.parseInt(code)); } catch(Exception e) { i.setCodeIntermediaire(0); }
        i.setLibelleCourt(court);
        i.setLibelleLong(long_);
        i.setNumeroCompte("TN59000000000000000000");
        i.setTypeBanque(1);
        i.setAdresse("Seed auto");
        i.setCodeBanque(1);
        i.setNomFichier("seed");
        i.setDateImport(d);
        return i;
    }
}