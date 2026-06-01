package com.fgm.gestion.service;

import java.time.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class BanqueEtatService {

    private final BanqueEtatRepository banqueEtatRepository;
    private final MouvementBancaireRepository mouvementBancaireRepository;
    private final IntermediaireRepository intermediaireRepository;
    private final BanqueRepository banqueRepository;
    private final JourFerieRepository jourFerieRepository;
    private final ApportInitialRepository apportInitialRepository;
    private final HistoryService historyService;
    private final IntermediaireLookupService intermediaireLookup;

    public BanqueEtatService(
            BanqueEtatRepository banqueEtatRepository,
            MouvementBancaireRepository mouvementBancaireRepository,
            IntermediaireRepository intermediaireRepository,
            BanqueRepository banqueRepository,
            JourFerieRepository jourFerieRepository,
            ApportInitialRepository apportInitialRepository,
            HistoryService historyService,
            IntermediaireLookupService intermediaireLookup) {

        this.banqueEtatRepository = banqueEtatRepository;
        this.mouvementBancaireRepository = mouvementBancaireRepository;
        this.intermediaireRepository = intermediaireRepository;
        this.banqueRepository = banqueRepository;
        this.jourFerieRepository = jourFerieRepository;
        this.apportInitialRepository = apportInitialRepository;
        this.historyService = historyService;
        this.intermediaireLookup = intermediaireLookup;
    }

    private static final Logger log = LoggerFactory.getLogger(BanqueEtatService.class);

    private boolean isFerie(LocalDate date) {
        return jourFerieRepository.existsByJour(date);
    }

    private LocalDate getNextWorkingDay(LocalDate date) {
        LocalDate next = date.plusDays(1);

        while (isFerie(next) || isWeekend(next)) {
            next = next.plusDays(1);
        }

        return next;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private String normalizeCode(String code) {
        if (code == null) return null;
        return code.replaceFirst("^0+", "");
    }

    // ================= GENERATION =================
    public List<BanqueEtat> generateFromMouvementBancaire(String seanceStr) {

        log.info("=== START GENERATE BANQUE ETAT ===");
        // Conversion de la date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate seance = LocalDate.parse(seanceStr, formatter);
        // Récupération des mouvements bancaires de la séance
        List<MouvementBancaire> appels = mouvementBancaireRepository.findBySeance(seance);
        log.info("Nombre MouvementBancaire: {}", appels.size());

        // On récupère la dernière date existante dans la collection ApportInitial
        Optional<ApportInitial> lastApportOpt = apportInitialRepository.findAll()
                .stream()
                .max(Comparator.comparing(ApportInitial::getSeance));

        LocalDate lastApportDate = lastApportOpt
                .map(ApportInitial::getSeance)
                .orElse(null);
        // Vérifie si la séance actuelle est la dernière
        boolean isLastSeance = seance.equals(lastApportDate);

        log.info("Derniere date ApportInitial: {}", lastApportDate);
        log.info("Is last seance: {}", isLastSeance);
        // Map pour regrouper les résultats par banque
        Map<Integer, BanqueEtat> map = new HashMap<>();

        // Set pour éviter d’ajouter plusieurs fois les ApportInitial
        Set<String> processedInterm = new HashSet<>();

        // ================= TRAITEMENT =================
        for (MouvementBancaire a : appels) {

            String codeNormalise = normalizeCode(String.valueOf(a.getCodeIntermediaire()));

            // Recherche de l'intermédiaire
            Intermediaire inter = intermediaireLookup.findForSeance(
                    Integer.parseInt(codeNormalise), a.getSeance());

            if (inter == null) {
                log.warn("Intermediaire NON trouvé: {}", codeNormalise);
                continue;
            }
            // Recherche de la banque associée
            int codeBanque = inter.getCodeBanque();

            List<Banque> banques = banqueRepository.findBycBque(codeBanque);

            if (banques == null || banques.isEmpty()) {
                log.warn("Banque NON trouvée: {}", codeBanque);
                continue;
            }

            Banque banque = banques.get(0);
            // Récupération ou création de l'objet BanqueEtat
            BanqueEtat be = map.getOrDefault(codeBanque, new BanqueEtat());

            if (!map.containsKey(codeBanque)) {

                be.setBanque(banque.getlCourBque());
                be.setCode(codeBanque);
                be.setType(inter.getTypeBanque());
                be.setSeance(seance);
                be.setSeanceValeur(getNextWorkingDay(seance));
                be.setDebitNbr(0);
                be.setMontantDebit(0);
                be.setCreditNbr(0);
                be.setMontantCredit(0);
                be.setSolde(0);
            }

            // Mouvement normal
            if (a.getAppel() > 0) {
                be.setDebitNbr(be.getDebitNbr() + 1);
                be.setMontantDebit(be.getMontantDebit() + a.getAppel());
            }

            if (a.getRestitution() > 0) {
                be.setCreditNbr(be.getCreditNbr() + 1);
                be.setMontantCredit(be.getMontantCredit() + a.getRestitution());
            }

            //  Ajout ApportInitial si dernière séance
            if (isLastSeance && !processedInterm.contains(codeNormalise)) {

                processedInterm.add(codeNormalise);

                Optional<ApportInitial> apportOpt =
                        apportInitialRepository
                                .findTopByCodeIntermAndSeanceLessThanEqualOrderBySeanceDesc(
                                        Integer.parseInt(codeNormalise), seance
                                );

                if (apportOpt.isPresent()) {

                    ApportInitial ap = apportOpt.get();
                    // Appel de contribution → Débit
                    if (ap.getAppelContrib() > 0) {
                        be.setDebitNbr(be.getDebitNbr() + 1);
                        be.setMontantDebit(be.getMontantDebit() + ap.getAppelContrib());

                        log.info("Ajout AppelContrib {} pour {}",
                                ap.getAppelContrib(), codeNormalise);
                    }
                    // Restitution → Crédit
                    if (ap.getRestitution() > 0) {
                        be.setCreditNbr(be.getCreditNbr() + 1);
                        be.setMontantCredit(be.getMontantCredit() + ap.getRestitution());

                        log.info("Ajout Restitution {} pour {}",
                                ap.getRestitution(), codeNormalise);
                    }
                }
            }
            // Calcul du solde
            be.setSolde(be.getMontantCredit() - be.getMontantDebit());

            map.put(codeBanque, be);
        }

        //  TOTAL GLOBAL
        int totalDebitAll = map.values().stream()
                .mapToInt(BanqueEtat::getMontantDebit)
                .sum();

        int totalCreditAll = map.values().stream()
                .mapToInt(BanqueEtat::getMontantCredit)
                .sum();

        //  FGM / BVMT (fonds) — recherche sur la séance courante pour éviter doublons multi-dates
        BanqueEtat fgm = null;

        Intermediaire fgmInter = intermediaireRepository.findByDateImport(seance).stream()
                .filter(i -> {
                    String c = i.getLibelleCourt() != null ? i.getLibelleCourt().toUpperCase() : "";
                    String l = i.getLibelleLong() != null ? i.getLibelleLong().toUpperCase() : "";
                    return c.contains("FGM") || l.contains("FGM") || c.contains("BVMT") || l.contains("BVMT");
                })
                .findFirst()
                .orElse(null);

        if (fgmInter != null) {

            int codeBanqueFgm = fgmInter.getCodeBanque();

            List<Banque> banquesFgm = banqueRepository.findBycBque(codeBanqueFgm);

            Banque banqueFgm = (banquesFgm != null && !banquesFgm.isEmpty())
                    ? banquesFgm.get(0)
                    : null;

            fgm = new BanqueEtat();

            fgm.setBanque(banqueFgm != null ? banqueFgm.getlCourBque() : "FGM");
            fgm.setCode(codeBanqueFgm);
            fgm.setType(fgmInter.getTypeBanque());
            fgm.setSeance(seance);
            fgm.setSeanceValeur(getNextWorkingDay(seance));
            // Inversion Débit/Crédit pour FGM
            fgm.setMontantDebit(totalCreditAll);
            fgm.setMontantCredit(totalDebitAll);

            fgm.setDebitNbr(1);
            fgm.setCreditNbr(1);

            fgm.setSolde(totalCreditAll - totalDebitAll);

            log.info("FGM ajouté séparément");
        }

        // ================= FILTRAGE FINAL =================
        List<BanqueEtat> result = new ArrayList<>();

        for (BanqueEtat be : map.values()) {
            if (be.getMontantDebit() != 0 || be.getMontantCredit() != 0) {
                result.add(be);
            }
        }

        if (fgm != null && (fgm.getMontantDebit() != 0 || fgm.getMontantCredit() != 0)) {
            result.add(fgm);
        }

        log.info("Nombre banques après filtrage: {}", result.size());

        // ================= SAVE =================
        // Scoped delete: uniquement pour cette seance
        banqueEtatRepository.deleteBySeance(seance);
        result = banqueEtatRepository.saveAll(result);

        log.info("Nombre BanqueEtat sauvegardés: {}", result.size());
        log.info("=== END GENERATE BANQUE ETAT ===");

        // ================= HISTORIQUE =================
        historyService.saveHistory("banquecentral_" + seance, seance);

        return result;
    }

    public void deleteBySeance(LocalDate seance) {
        banqueEtatRepository.deleteBySeance(seance);
    }

    public List<BanqueEtat> getBySeance(LocalDate seance) {
        return banqueEtatRepository.findBySeance(seance);
    }

// ============================================================
// PDF BANQUE ETAT STYLE BCT
// ============================================================

    public byte[] generatePdfBySeance(LocalDate seance) {

        log.info("=== GENERATION PDF BANQUE ETAT ===");

        List<BanqueEtat> list = banqueEtatRepository.findBySeance(seance);

        if (list.isEmpty()) {
            throw new RuntimeException(
                    "Aucune donnée pour cette séance"
            );
        }

        try {

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            PdfWriter writer =
                    new PdfWriter(out);

            PdfDocument pdf =
                    new PdfDocument(writer);

            Document document =
                    new Document(pdf, PageSize.A4);

            document.setMargins(20, 30, 20, 30);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // =========================================================
            // HEADER
            // =========================================================

            Paragraph title1 = new Paragraph(
                    "BOURSE DES VALEURS MOBILIERES DE TUNIS"
            )
                    .setBold()
                    .setFontSize(13)
                    .setTextAlignment(TextAlignment.LEFT);

            Paragraph title2 = new Paragraph(
                    "FONDS DE GARANTIE DE MARCHE"
            )
                    .setBold()
                    .setFontSize(12)
                    .setMarginTop(-5);

            Paragraph desc = new Paragraph(
                    "Géré par la BVMT pour le compte de L'AIB en vertu de la\n" +
                            "convention de transfert du mandat d'administration du FGM"
            )
                    .setFontSize(10)
                    .setMarginTop(-5);

            document.add(title1);
            document.add(title2);
            document.add(desc);

            // =========================================================
            // DATE
            // =========================================================

            Paragraph tunisDate = new Paragraph(
                    "Tunis le:  " + seance.format(formatter)
            )
                    .setBold()
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(20);

            document.add(tunisDate);

            // =========================================================
            // DESTINATION
            // =========================================================

            Paragraph banqueTitle = new Paragraph(
                    "Banque Centrale de Tunisie\n" +
                            "Rue Hédi Nouira -1001- Tunis - Tunisie"
            )
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(50);

            document.add(banqueTitle);

            // =========================================================
            // TEXTE
            // =========================================================

            Paragraph p1 = new Paragraph("Messieurs,")
                    .setFontSize(12)
                    .setMarginTop(20);

            Paragraph p2 = new Paragraph(
                    "Nous vous prions de vouloir porter au débit et au crédit " +
                            "des comptes figurant sur le tableau les montants en Dinars " +
                            "suivants, en date valeur du  "
                            + seance.format(formatter)
            )
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginTop(10);

            document.add(p1);
            document.add(p2);

            document.add(new Paragraph("\n"));

            // =========================================================
            // TABLE
            // =========================================================

            float[] widths = {
                    100F, // banque
                    70F,  // type
                    70F,  // code
                    75F,  // debit nbr
                    130F, // debit montant
                    75F,  // credit nbr
                    130F, // credit montant
                    120F  // solde
            };

            Table table = new Table(widths);

            table.setWidth(
                    UnitValue.createPercentValue(100)
            );

            // =========================================================
            // HEADER LIGNE 1
            // =========================================================

            table.addHeaderCell(
                    createHeader("Banque", 2, 1)
            );

            table.addHeaderCell(
                    createHeader("Type", 2, 1)
            );

            table.addHeaderCell(
                    createHeader("Code", 2, 1)
            );

            table.addHeaderCell(
                    createHeader("Débit", 1, 2)
            );

            table.addHeaderCell(
                    createHeader("Crédit", 1, 2)
            );

            table.addHeaderCell(
                    createHeader("Solde", 2, 1)
            );

            // =========================================================
            // HEADER LIGNE 2
            // =========================================================

            table.addHeaderCell(
                    createHeader("Nbr", 1, 1)
            );

            table.addHeaderCell(
                    createHeader("Montant", 1, 1)
            );

            table.addHeaderCell(
                    createHeader("Nbr", 1, 1)
            );

            table.addHeaderCell(
                    createHeader("Montant", 1, 1)
            );

            // =========================================================
            // DATA
            // =========================================================

            int totalDebitNbr = 0;
            int totalDebitMontant = 0;

            int totalCreditNbr = 0;
            int totalCreditMontant = 0;

            int totalSolde = 0;

            for (BanqueEtat b : list) {

                table.addCell(
                        createBody(
                                b.getBanque(),
                                TextAlignment.LEFT
                        )
                );

                table.addCell(
                        createBody(
                                String.valueOf(b.getType()),
                                TextAlignment.CENTER
                        )
                );

                table.addCell(
                        createBody(
                                String.valueOf(b.getCode()),
                                TextAlignment.CENTER
                        )
                );

                table.addCell(
                        createBody(
                                String.valueOf(b.getDebitNbr()),
                                TextAlignment.CENTER
                        )
                );

                table.addCell(
                        createBody(
                                String.format("%,d",
                                        b.getMontantDebit()),
                                TextAlignment.RIGHT
                        )
                );

                table.addCell(
                        createBody(
                                String.valueOf(b.getCreditNbr()),
                                TextAlignment.CENTER
                        )
                );

                table.addCell(
                        createBody(
                                String.format("%,d",
                                        b.getMontantCredit()),
                                TextAlignment.RIGHT
                        )
                );

                table.addCell(
                        createBody(
                                String.format("%,d",
                                        b.getSolde()),
                                TextAlignment.RIGHT
                        )
                );

                totalDebitNbr += b.getDebitNbr();
                totalDebitMontant += b.getMontantDebit();

                totalCreditNbr += b.getCreditNbr();
                totalCreditMontant += b.getMontantCredit();

                totalSolde += b.getSolde();
            }

            // =========================================================
            // TOTAL
            // =========================================================

            Cell totalCell = new Cell(1, 3)
                    .add(
                            new Paragraph("Total")
                                    .setBold()
                    )
                    .setBackgroundColor(
                            new com.itextpdf.kernel.colors.DeviceRgb(
                                    173,
                                    200,
                                    226
                            )
                    )
                    .setTextAlignment(TextAlignment.CENTER);

            table.addCell(totalCell);

            table.addCell(
                    createTotalCell(
                            String.valueOf(totalDebitNbr)
                    )
            );

            table.addCell(
                    createTotalCell(
                            String.format("%,d",
                                    totalDebitMontant)
                    )
            );

            table.addCell(
                    createTotalCell(
                            String.valueOf(totalCreditNbr)
                    )
            );

            table.addCell(
                    createTotalCell(
                            String.format("%,d",
                                    totalCreditMontant)
                    )
            );

            table.addCell(
                    createTotalCell(
                            String.format("%,d",
                                    totalSolde)
                    )
            );

            document.add(table);

            // =========================================================
            // FOOTER SIGNATURE
            // =========================================================

            Paragraph footer = new Paragraph(
                    "Cachet et Signature"
            )
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(80);

            document.add(footer);

            document.close();

            log.info("PDF généré avec succès");

            return out.toByteArray();

        } catch (Exception e) {

            log.error(
                    "Erreur génération PDF BanqueEtat",
                    e
            );

            throw new RuntimeException(
                    "Erreur génération PDF : "
                            + e.getMessage(),
                    e
            );
        }
    }

// ============================================================
// HEADER CELL
// ============================================================

    private Cell createHeader(
            String text,
            int rowspan,
            int colspan
    ) {

        return new Cell(rowspan, colspan)
                .add(
                        new Paragraph(text)
                                .setBold()
                                .setFontSize(10)
                )
                .setBackgroundColor(
                        new com.itextpdf.kernel.colors.DeviceRgb(
                                173,
                                200,
                                226
                        )
                )
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(
                        VerticalAlignment.MIDDLE
                )
                .setPadding(5)
                .setBorder(
                        new SolidBorder(
                                ColorConstants.BLACK,
                                1
                        )
                );
    }

// ============================================================
// BODY CELL
// ============================================================

    private Cell createBody(
            String text,
            TextAlignment alignment
    ) {

        return new Cell()
                .add(
                        new Paragraph(
                                text != null ? text : ""
                        ).setFontSize(10)
                )
                .setTextAlignment(alignment)
                .setVerticalAlignment(
                        VerticalAlignment.MIDDLE
                )
                .setPadding(4)
                .setBorder(
                        new SolidBorder(
                                ColorConstants.BLACK,
                                0.8f
                        )
                );
    }

// ============================================================
// TOTAL CELL
// ============================================================

    private Cell createTotalCell(String text) {

        return new Cell()
                .add(
                        new Paragraph(text)
                                .setBold()
                                .setFontSize(10)
                )
                .setBackgroundColor(
                        new com.itextpdf.kernel.colors.DeviceRgb(
                                173,
                                200,
                                226
                        )
                )
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5)
                .setBorder(
                        new SolidBorder(
                                ColorConstants.BLACK,
                                1
                        )
                );
    }
}