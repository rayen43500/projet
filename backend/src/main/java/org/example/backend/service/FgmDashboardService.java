package com.fgm.gestion.service;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agrège les données Mongo vers le format attendu par le frontend Angular FGM.
 *
 * Règles du document explicatif appliquées :
 *   - nbIntermedActifs = nb d'IB distincts ayant réalisé des transactions (pas le total du fichier)
 *   - typeRisque DEFAUT_TITRES si PNT="-" (doit livrer)
 *   - typeRisque DEFAUT_ESPECES si PNE="-" (doit payer net)
 *   - RM global = Σ risqueJ + Σ risqueJ_1 sur toutes les positions
 */
@Service
public class FgmDashboardService {

    private static final DateTimeFormatter ISO     = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SeanceService              seanceService;
    private final TransactionService         transactionService;
    private final IntermediaireService       intermediaireService;
    private final PositionnetteService       positionnetteService;
    private final RisqueService              risqueService;
    private final MouvementBancaireRepository mouvementBancaireRepo;
    private final ProvisionRepository        provisionRepo;
    private final AppelRestitutionRepository appelRepo;

    public FgmDashboardService(
            SeanceService seanceService,
            TransactionService transactionService,
            IntermediaireService intermediaireService,
            PositionnetteService positionnetteService,
            RisqueService risqueService,
            MouvementBancaireRepository mouvementBancaireRepo,
            ProvisionRepository provisionRepo,
            AppelRestitutionRepository appelRepo) {
        this.seanceService         = seanceService;
        this.transactionService    = transactionService;
        this.intermediaireService  = intermediaireService;
        this.positionnetteService  = positionnetteService;
        this.risqueService         = risqueService;
        this.mouvementBancaireRepo = mouvementBancaireRepo;
        this.provisionRepo         = provisionRepo;
        this.appelRepo             = appelRepo;
    }

    // ── Parsers ──────────────────────────────────────────────────────────────
    public LocalDate parseIso(String iso)  { return LocalDate.parse(iso, ISO); }
    public String    toIso(LocalDate d)    { return d.format(ISO); }
    public String    toCompact(LocalDate d){ return d.format(COMPACT); }

    // ── countAnomalies (appelé par FgmApiController) ─────────────────────────
    public int countAnomalies(LocalDate seanceDate) {
        return (int) risqueService.getBySeance(seanceDate).stream()
                .filter(r -> r.getRisquej() > 0 || r.getRisquej_1() > 0)
                .count();
    }

    // ── Séances ───────────────────────────────────────────────────────────────
    public List<Map<String, Object>> listSeancesFrontend() {
        List<Seance> raw = seanceService.getAllSeances().stream()
                .sorted(Comparator.comparing(Seance::getSeance).reversed())
                .collect(Collectors.toList());
        LocalDate maxDate = raw.isEmpty() ? null : raw.get(0).getSeance();
        return raw.stream().map(s -> toSeanceMap(s, maxDate)).collect(Collectors.toList());
    }

    public Map<String, Object> getSeanceCourante() {
        List<Seance> all = seanceService.getAllSeances().stream()
                .sorted(Comparator.comparing(Seance::getSeance).reversed())
                .collect(Collectors.toList());
        if (all.isEmpty()) return null;
        LocalDate maxDate = all.get(0).getSeance();
        Seance pick = all.stream()
                .filter(s -> "OUVERTE".equals(effectiveStatut(s, maxDate)))
                .findFirst().orElse(all.get(0));
        return toSeanceMap(pick, maxDate);
    }

    public Map<String, Object> getSeanceStats(String isoDate) {
        LocalDate d = parseIso(isoDate);
        List<Transaction> tx = transactionService.findBySeance(d);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dateSeance",    toIso(d));
        m.put("nbTransactions", tx.size());
        m.put("volumeTND",     tx.stream().mapToDouble(Transaction::getVolume).sum());
        return m;
    }

    // ── Intermédiaires ────────────────────────────────────────────────────────
    public List<Map<String, Object>> listIntermediairesForSeanceIso(String isoDate) {
        LocalDate d = parseIso(isoDate);
        return intermediaireService.findForSeance(d).stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("codeIntermediaire",         i.getCodeIntermediaire());
            m.put("libelleCourtIntermediaire", nullToEmpty(i.getLibelleCourt()));
            m.put("libelleLongIntermediaire",  nullToEmpty(i.getLibelleLong()));
            m.put("numeroCompteIntermediaire", nullToEmpty(i.getNumeroCompte()));
            m.put("adresseIntermediaire",      nullToEmpty(i.getAdresse()));
            m.put("codeBanque",  String.valueOf(i.getCodeBanque()));
            m.put("typeBanque",  String.valueOf(i.getTypeBanque()));
            m.put("dateDernierImport", i.getDateImport() != null ? i.getDateImport().format(ISO) : null);
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> positionsForDateIso(String isoDate) {
        return positionsForSeance(parseIso(isoDate));
    }

    public List<Map<String, Object>> positionsForIntermediaire(String isoDate, String codeInter) {
        String code = codeInter == null ? "" : codeInter.trim();
        return positionsForDateIso(isoDate).stream()
                .filter(p -> code.equals(String.valueOf(p.get("codeIntermediaire"))))
                .collect(Collectors.toList());
    }

    // ── Snapshot post-import ──────────────────────────────────────────────────
    public Map<String, Object> buildSessionImportSnapshot(LocalDate seanceDate) {
        List<Map<String, Object>> positions = positionsForSeance(seanceDate);
        List<Map<String, Object>> feuille   = buildFeuilleAppelMarge(seanceDate);

        List<Transaction> txList = transactionService.findBySeance(seanceDate);

        // transactionsByIntermed : real per-slot distribution using txIndex
        final int N_SLOTS = 14;
        final int totalTx = txList.size();
        Map<String, Map<String, Object>> txByInter = new LinkedHashMap<>();
        for (int ti = 0; ti < txList.size(); ti++) {
            Transaction t = txList.get(ti);
            int slot = totalTx > 0 ? Math.min(N_SLOTS - 1, ti * N_SLOTS / totalTx) : 0;
            addTxCount(txByInter, String.valueOf(t.getCodeIntermediaireAcheteur()), t.getLibelleIntermediaireAcheteur(), slot, N_SLOTS);
            addTxCount(txByInter, String.valueOf(t.getCodeIntermediaireVendeur()),  t.getLibelleIntermediaireVendeur(),  slot, N_SLOTS);
        }

        // R_val (RM) = Σ risqueJ + Σ risqueJ_1 ; RS = Σ risqueSuspens
        List<Risque> risques = risqueService.getBySeance(seanceDate);
        long rmGlobal = risques.stream()
                .mapToLong(r -> (long) r.getRisquej() + r.getRisquej_1())
                .sum();
        long totalRsusp = risques.stream().mapToLong(Risque::getRisqueSuspens).sum();
        long totalRval = rmGlobal;

        List<Map<String, Object>> alertes = buildAlertes(positions, seanceDate);

        double totalProvision = provisionRepo.findByDateCalcul(seanceDate).stream()
                .mapToDouble(Provision::getMontantProvision).sum();

        long nbPositionsRisque = positions.stream()
                .filter(p -> !"AUCUN".equals(p.get("typeRisque"))).count();
        long nbDefaillants = feuille.stream()
                .filter(f -> Boolean.TRUE.equals(f.get("defaillant"))).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("nbTrades",          txList.size());
        stats.put("nbPositions",       positions.size());
        stats.put("nbPositionsRisque", nbPositionsRisque);
        stats.put("rmGlobal",          rmGlobal);
        stats.put("totalRval",         totalRval);
        stats.put("totalRsusp",        totalRsusp);
        stats.put("totalProvision",    Math.round(totalProvision));
        stats.put("nbDefaillants",     nbDefaillants);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("dateSeance",              toIso(seanceDate));
        root.put("positionsARisque",        positions);
        root.put("feuilleAppelMarge",       feuille);
        root.put("statistiques",            stats);
        root.put("alertes",                 alertes);
        root.put("transactionsByIntermed",  txByInter);
        return root;
    }

    private List<Map<String, Object>> buildAlertes(List<Map<String, Object>> positions, LocalDate seanceDate) {
        String ts = seanceDate.format(ISO);
        List<Map<String, Object>> alertes = new ArrayList<>();
        for (Map<String, Object> p : positions) {
            String typeRisque = String.valueOf(p.get("typeRisque"));
            if ("AUCUN".equals(typeRisque)) continue;
            int risqueJ = p.get("risqueJ") instanceof Number n ? n.intValue() : 0;
            int rs = p.get("risqueSuspens") instanceof Number n ? n.intValue() : 0;
            if (risqueJ <= 0 && rs <= 0) continue;

            Map<String, Object> a = new LinkedHashMap<>();
            a.put("intermediaire", nullToEmpty(String.valueOf(p.get("nomIntermediaire"))));
            a.put("isin",          nullToEmpty(String.valueOf(p.get("isin"))));
            a.put("valeur",        nullToEmpty(String.valueOf(p.get("libelleValeur"))));
            a.put("risqueJ",       risqueJ);
            a.put("type",          typeRisque);
            a.put("message",       "DEFAUT_TITRES".equals(typeRisque)
                    ? "Suspens titres — risque livraison"
                    : "Suspens espèces — risque paiement net");
            a.put("timestamp",     ts);
            alertes.add(a);
        }
        return alertes;
    }

    // ── Positions avec risque ─────────────────────────────────────────────────
    private List<Map<String, Object>> positionsForSeance(LocalDate seanceDate) {
        List<Positionnette> pn = positionnetteService.getBySeance(seanceDate);
        List<Risque>        rq = risqueService.getBySeance(seanceDate);

        Map<String, Risque> riskByKey = new HashMap<>();
        for (Risque r : rq)
            riskByKey.put(rkey(r.getCodeIntermediaire(), r.getValeur()), r);

        return pn.stream().map(p -> {
            Risque r      = riskByKey.get(rkey(p.getCodeIntermediaire(), p.getValeur()));
            int risqueJ   = r != null ? r.getRisquej()   : 0;
            int risqueJ1  = r != null ? r.getRisquej_1() : 0;
            int rs        = r != null ? r.getRisqueSuspens() : 0;
            int rm        = risqueJ + risqueJ1;

            // Type de risque selon le document explicatif :
            // DEFAUT_TITRES  : PNT="-" (doit livrer des titres)
            // DEFAUT_ESPECES : PNE="-" (doit payer net en espèces)
            // Les deux peuvent coexister sur des valeurs différentes
            String typeRisque = "AUCUN";
            if ("-".equals(p.getPnt()))       typeRisque = "DEFAUT_TITRES";
            else if ("-".equals(p.getPne()))  typeRisque = "DEFAUT_ESPECES";

            // PNT signé pour le frontend : négatif = livraison (risque titres)
            double pntSigne = "-".equals(p.getPnt()) ? -p.getQuantiteNette() : p.getQuantiteNette();
            // PNE signé pour le frontend : négatif = paiement net (risque espèces)
            double pneSigne = "-".equals(p.getPne()) ? -p.getMontantNette()  : p.getMontantNette();

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",               p.getId());
            m.put("codeIntermediaire", p.getCodeIntermediaire());
            m.put("nomIntermediaire", nullToEmpty(p.getIntermediaire()));
            m.put("codeIsin",         "");
            m.put("isin",             "");
            m.put("libelleValeur",    nullToEmpty(p.getValeur()));
            m.put("coursCloture",     p.getCloture());
            m.put("cours",            p.getCloture());
            m.put("pntSign",          p.getPnt());
            m.put("pneSign",          p.getPne());
            m.put("pnt",              pntSigne);
            m.put("pne",              pneSigne);
            m.put("quantiteAchetee",  p.getQuantiteAchete());
            m.put("quantiteVendue",   p.getQuantiteVendu());
            m.put("quantiteNette",    p.getQuantiteNette());
            m.put("montantNette",     p.getMontantNette());
            m.put("risqueJ",          risqueJ);
            m.put("risqueJ1",         risqueJ1);
            m.put("risqueSuspens",    rs);
            m.put("rVal",             rm);
            m.put("rSusp",            rs);
            m.put("rm",               rm);
            m.put("typeRisque",       typeRisque);
            m.put("statut",           "AUCUN".equals(typeRisque) ? "NORMAL" : "CRITICAL");
            return m;
        }).collect(Collectors.toList());
    }

    // ── Feuille appel de marge ────────────────────────────────────────────────
    private List<Map<String, Object>> buildFeuilleAppelMarge(LocalDate seanceDate) {
        // Priorité : MouvementBancaire (déjà calculé par le batch)
        List<MouvementBancaire> mvts = mouvementBancaireRepo.findBySeance(seanceDate);
        if (!mvts.isEmpty()) {
            return mvts.stream().map(mb -> {
                int rmInter = mb.getTotalSeance() + mb.getTotalSeancePrecedent();
                int rVal = rmInter;
                int rSusp = mb.getTotalRsusp();
                int totalInter = mb.getTotal() > 0 ? mb.getTotal() : rmInter + rSusp;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("codeIntermediaire", mb.getCodeIntermediaire());
                m.put("nomIntermediaire",  mb.getIntermediaire());
                m.put("rmSeanceJ",         mb.getTotalSeance());
                m.put("rmSeanceJ1",        mb.getTotalSeancePrecedent());
                m.put("rVal",              rVal);
                m.put("rSusp",             rSusp);
                m.put("total",             totalInter);
                m.put("provision",         mb.getProvision());
                m.put("difference",        mb.getDifference());
                m.put("appel",             mb.getAppel());
                m.put("restitution",       mb.getRestitution());
                m.put("apportInitial",     mb.getApportInitial());
                m.put("defaillant",        mb.getAppel() > 0);
                return m;
            }).collect(Collectors.toList());
        }
        // Fallback : AppelRestitution
        return appelRepo.findByDateSeance(seanceDate).stream().map(a -> {
            double rVal = a.getRisque();
            double prov = a.getProvision();
            double diff = rVal - prov;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("nomIntermediaire", nullToEmpty(a.getIntermediaire()));
            m.put("rVal",       rVal);
            m.put("rSusp",      0);
            m.put("total",      rVal);
            m.put("provision",  prov);
            m.put("difference", Math.round(diff * 100.0) / 100.0);
            m.put("appel",      diff > 0 ? (int) diff : 0);
            m.put("restitution",diff < 0 ? (int) Math.abs(diff) : 0);
            m.put("defaillant", diff > 0);
            return m;
        }).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void addTxCount(Map<String, Map<String, Object>> map, String code, String nom, int slot, int nSlots) {
        if (code == null || code.isBlank()) return;
        String k = code.trim();
        map.computeIfAbsent(k, x -> {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("code", parseSafe(k));
            o.put("nom",  nom != null && !nom.isBlank() ? nom : ("#" + k));
            o.put("nbTransactions", 0);
            // Initialize slots array
            java.util.List<Integer> slots = new java.util.ArrayList<>();
            for (int i = 0; i < nSlots; i++) slots.add(0);
            o.put("slots", slots);
            return o;
        });
        map.get(k).merge("nbTransactions", 1, (a, b) -> ((Integer) a) + ((Integer) b));
        // Increment slot count
        @SuppressWarnings("unchecked")
        java.util.List<Integer> slots = (java.util.List<Integer>) map.get(k).get("slots");
        if (slots != null && slot < slots.size()) {
            slots.set(slot, slots.get(slot) + 1);
        }
    }

    private Map<String, Object> toSeanceMap(Seance s, LocalDate maxDate) {
        LocalDate d = s.getSeance();
        List<Transaction> tx = transactionService.findBySeance(d);
        double volume = tx.stream().mapToDouble(Transaction::getVolume).sum();

        // nbIntermediaires = nb d'IB ACTIFS ayant réalisé des transactions (pas total fichier)
        Set<String> actifs = new HashSet<>();
        for (Transaction t : tx) {
            if (t.getCodeIntermediaireAcheteur() != 0)
                actifs.add(String.valueOf(t.getCodeIntermediaireAcheteur()));
            if (t.getCodeIntermediaireVendeur() != 0)
                actifs.add(String.valueOf(t.getCodeIntermediaireVendeur()));
        }
        int nbInter = actifs.size();
        // Si pas encore de transactions (séance préparée), utiliser le fichier intermédiaires
        if (nbInter == 0) {
            nbInter = intermediaireService.findForSeance(d).size();
        }

        String statut = effectiveStatut(s, maxDate);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               s.getId());
        m.put("dateSeance",       toIso(d));
        m.put("seance",           d);
        m.put("statut",           statut);
        m.put("heureOuverture",   "08:30");
        m.put("heureCloture",     "15:00");
        m.put("nbIntermediaires", nbInter);
        m.put("nbTransactions",   tx.size());
        m.put("volumeTND",        volume);
        m.put("motifAnnulation",  s.getMotifAnnulation());
        m.put("anomalies",        s.getAnomalies() != null ? s.getAnomalies() : new ArrayList<>());
        m.put("createdAt",        "");
        m.put("updatedAt",        "");
        return m;
    }

    private String effectiveStatut(Seance s, LocalDate maxDate) {
        if (s.getStatut() != null && !s.getStatut().isBlank()) return s.getStatut();
        return maxDate != null && maxDate.equals(s.getSeance()) ? "OUVERTE" : "CLOTUREE";
    }

    private static String rkey(int c, String v) {
        return c + "|" + (v != null ? v.trim() : "");
    }

    public static String nullToEmpty(String v)  { return v == null ? "" : v; }
    public static int    parseSafe(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}