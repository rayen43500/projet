package com.fgm.gestion.mouvementbancairebatch;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;
import com.fgm.gestion.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class MouvementBancaireTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(MouvementBancaireTasklet.class);

    private final RisqueRepository risqueRepository;
    private final MouvementBancaireRepository mouvementBancaireRepository;
    private final ProvisionRepository provisionRepository;
    private final HistoryService historyService;
    private final ApportInitialRepository apportRepo;
    private final ParametrageRepository parametrageRepository;

    public MouvementBancaireTasklet(
            RisqueRepository risqueRepository,
            MouvementBancaireRepository mouvementBancaireRepository,
            ProvisionRepository provisionRepository,
            HistoryService historyService,
            ApportInitialRepository apportRepo,
            ParametrageRepository parametrageRepository) {

        this.risqueRepository = risqueRepository;
        this.mouvementBancaireRepository = mouvementBancaireRepository;
        this.provisionRepository = provisionRepository;
        this.historyService = historyService;
        this.apportRepo = apportRepo;
        this.parametrageRepository = parametrageRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("[MouvementBancaire] === DEBUT DU TASKLET ===");

        String seanceParam = (String) chunkContext
                .getStepContext()
                .getJobParameters()
                .get("seance");

        log.info("[MouvementBancaire] Parametre seance recu : {}", seanceParam);

        if (seanceParam == null || seanceParam.isBlank()) {
            log.error("[MouvementBancaire] ERREUR : le parametre 'seance' est null ou vide");
            throw new RuntimeException("Le parametre 'seance' est manquant dans les JobParameters");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate seance = LocalDate.parse(seanceParam, formatter);
        log.info("[MouvementBancaire] Seance parsee : {}", seance);

        Parametrage param = parametrageRepository.findAll().stream().findFirst().orElse(null);
        double seuilAppelPct = param != null ? param.getSeuil_dep_pro() : 10;
        double seuilRestitution = param != null ? param.getDep_risq() : 25000;

        List<Risque> risques = risqueRepository.findBySeance(seance);
        log.info("[MouvementBancaire] Nombre de risques trouves pour la seance {} : {}", seance, risques.size());

        if (risques.isEmpty()) {
            log.warn("[MouvementBancaire] Aucun risque trouve pour la seance {} - pas de mouvement a generer", seance);
            historyService.saveHistory("mouvementBancaire_" + seance, seance);
            return RepeatStatus.FINISHED;
        }

        // Scoped delete avant recalcul (evite doublons si re-import)
        mouvementBancaireRepository.deleteBySeance(seance);
        provisionRepository.deleteByDateCalcul(seance);

        // ================= GROUP BY INTERMEDIAIRE =================
        Map<String, int[]> map = new HashMap<>();

        for (Risque r : risques) {

            String key = r.getCodeIntermediaire() + "|" + r.getIntermediaire();

            map.merge(
                    key,
                    new int[]{r.getRisquej(), r.getRisquej_1(), r.getRisqueSuspens()},
                    (oldVal, newVal) -> new int[]{
                            oldVal[0] + newVal[0],
                            oldVal[1] + newVal[1],
                            oldVal[2] + newVal[2]
                    }
            );
        }

        // ADC utilisés
        Set<Integer> provisionsUtilisees = new HashSet<>();

        // ================= TRAITEMENT PRINCIPAL =================
        for (Map.Entry<String, int[]> entry : map.entrySet()) {

            String[] parts = entry.getKey().split("\\|");
            String intercode = parts[0];
            String intercod = String.valueOf(Integer.parseInt(parts[0]));
            int idAdc = Integer.parseInt(intercode);
            String inter = parts[1];

            int totalSeance = entry.getValue()[0];
            int totalSeancePrecedent = entry.getValue()[1];
            int totalRsusp = entry.getValue()[2];
            int rmTotal = totalSeance + totalSeancePrecedent;
            int total = rmTotal + totalRsusp;

            log.info("[MouvementBancaire] Traitement intermediaire : code={} (intercod={}), libelle={}, totalSeance={}, totalSeancePrecedent={}, totalRsusp={}, total={}",
                    intercode, intercod, inter, totalSeance, totalSeancePrecedent, totalRsusp, total);

            provisionsUtilisees.add(idAdc);

            // ===== DERNIERE PROVISION AVANT LA SEANCE =====
            Provision prov = provisionRepository
                    .findTopByIdAdcAndDateCalculLessThanOrderByDateCalculDesc(idAdc, seance)
                    .orElse(null);

            if (prov == null) {
                log.warn("[MouvementBancaire] Aucune provision precedente trouvee pour idAdc={} avant seance={}. provisionPrecedente=0, risquePrecedent=0", idAdc, seance);
            } else {
                log.info("[MouvementBancaire] Provision precedente trouvee pour idAdc={} : dateCalcul={}, montantProvision={}, montantRisqueTotal={}",
                        idAdc, prov.getDateCalcul(), prov.getMontantProvision(), prov.getMontantRisqueTotal());
            }

            double provisionPrecedente = (prov != null) ? prov.getMontantProvision() : 0;
            double risquePrecedent = (prov != null) ? prov.getMontantRisqueTotal() : 0;

            ApportInitial apport = apportRepo
                    .findTopByCodeIntermAndSeanceLessThanEqualOrderBySeanceDesc(idAdc, seance)
                    .orElse(null);

            int apportPrecedent = 0;
            if (apport == null) {
                log.warn("[MouvementBancaire] ApportInitial absent pour codeInterm={} — création apport=0", idAdc);
                ApportInitial created = new ApportInitial();
                created.setSeance(seance);
                created.setCodeInterm(String.valueOf(idAdc));
                created.setIntermediaire(inter);
                created.setApportInitial(0);
                created.setApportInitialAjuste(0);
                apportRepo.save(created);
            } else {
                log.info("[MouvementBancaire] ApportInitial trouve pour codeInterm={} : seanceApport={}, apportInitialAjuste={}",
                        idAdc, apport.getSeance(), apport.getApportInitialAjuste());
                apportPrecedent = (int) apport.getApportInitialAjuste();
            }
            String interlibelle = (prov != null) ? prov.getlibelleCourtinter() : inter;

            if (prov == null) {
                log.warn("[MouvementBancaire] interlibelle utilise depuis le libelle intermediaire car prov est null : '{}'", interlibelle);
            }

            // ===== ANCIEN APPEL / RESTITUTION =====
            double ecartPrec = risquePrecedent - provisionPrecedente;

            double diffArrondiP = (provisionPrecedente != 0)
                    ? 100 * ((risquePrecedent / provisionPrecedente) - 1)
                    : 0;

            double diffPrec = Math.round(diffArrondiP * 1000.0) / 1000.0;

            int appelPrec = 0;
            int restitutionPrec = 0;

            if (ecartPrec > 0 && diffPrec >= seuilAppelPct) {
                appelPrec = (int) ecartPrec;
            } else if (ecartPrec < 0 && Math.abs(ecartPrec) >= seuilRestitution) {
                restitutionPrec = (int) Math.abs(ecartPrec);
            }

            // ===== NOUVELLE PROVISION =====
            double nouvelleProvision = provisionPrecedente + appelPrec - restitutionPrec;

            // ===== NOUVEAU APPEL / RESTITUTION =====
            double ecart = total - nouvelleProvision;

            double diffArrondi = (nouvelleProvision != 0)
                    ? 100 * ((total / nouvelleProvision) - 1)
                    : 0;


            double diff = Math.round(diffArrondi * 1000.0) / 1000.0;



            int appel = 0;
            int restitution = 0;

            if (ecart > 0 && diff >= seuilAppelPct) {
                appel = (int) ecart;
            } else if (ecart < 0 && Math.abs(ecart) >= seuilRestitution) {
                restitution = (int) Math.abs(ecart);
            }

            // ===== SAUVEGARDE APPEL MARGE =====
            MouvementBancaire a = new MouvementBancaire();

            a.setSeance(seance);
            a.setCodeIntermediaire(idAdc);
            a.setIntermediaire(inter);
            a.setTotalSeance(totalSeance);
            a.setTotalSeancePrecedent(totalSeancePrecedent);
            a.setTotalRsusp(totalRsusp);
            a.setTotal(total);
            a.setProvision((int) nouvelleProvision);
            a.setDifference(diff);
            a.setAppel(appel);
            a.setRestitution(restitution);
            a.setApportInitial(apportPrecedent);

            log.info("[MouvementBancaire] Sauvegarde MouvementBancaire : codeInter={}, appel={}, restitution={}, nouvelleProvision={}, diff={}",
                    idAdc, appel, restitution, nouvelleProvision, diff);
            mouvementBancaireRepository.save(a);
            log.info("[MouvementBancaire] MouvementBancaire sauvegarde avec succes pour codeInter={}", idAdc);

            // ===== SAUVEGARDE PROVISION =====
            Provision newProv = new Provision();

            newProv.setDateCalcul(seance);
            newProv.setIdAdc(idAdc);
            newProv.setMontantProvision(nouvelleProvision);
            newProv.setMontantApportInitial(apportPrecedent);
            newProv.setMontantRisqueTotal(total);
            newProv.setLibelleCourtinter(interlibelle);
            provisionRepository.save(newProv);
            log.info("[MouvementBancaire] Nouvelle provision sauvegardee pour idAdc={}, dateCalcul={}", idAdc, seance);
        }

        log.info("[MouvementBancaire] Traitement principal termine. {} intermediaires traites.", map.size());

        // ================= PROVISIONS NON UTILISEES =================

        // récupérer toutes les provisions passées
        List<Provision> anciennesProvisions = provisionRepository.findAll();

        // garder un seul enregistrement par ADC
        Map<Integer, Provision> lastProvisionMap = new HashMap<>();

        for (Provision p : anciennesProvisions) {

            if (!provisionsUtilisees.contains(p.getIdAdc())) {

                Provision last = provisionRepository
                        .findTopByIdAdcAndDateCalculLessThanOrderByDateCalculDesc(
                                p.getIdAdc(),
                                seance
                        )
                        .orElse(null);

                if (last != null) {
                    lastProvisionMap.put(p.getIdAdc(), last);
                }
            }
        }

        // insertion sans doublons
        for (Provision last : lastProvisionMap.values()) {

            Provision newProv = new Provision();

            newProv.setDateCalcul(seance);
            newProv.setIdAdc(last.getIdAdc());
            newProv.setMontantProvision(last.getMontantProvision());
            newProv.setMontantApportInitial(last.getMontantApportInitial());
            newProv.setMontantRisqueTotal(last.getMontantRisqueTotal());
            newProv.setLibelleCourtinter(last.getlibelleCourtinter());

            provisionRepository.save(newProv);
        }

        // ================= HISTORIQUE =================
        log.info("[MouvementBancaire] Sauvegarde historique pour seance={}", seance);
        historyService.saveHistory(
                "mouvementBancaire_" + seance,
                seance
        );

        log.info("[MouvementBancaire] === TASKLET TERMINE AVEC SUCCES pour seance={} ===", seance);
        return RepeatStatus.FINISHED;
    }
}