package com.fgm.gestion.service;

import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.time.format.DateTimeFormatter;

@Service
public class SwiftService {

    private static final Logger log = LoggerFactory.getLogger(SwiftService.class);

    private final BanqueEtatRepository banqueEtatRepository;
    private final BanqueRepository banqueRepository;
    private final SwiftRepository swiftRepository;

    public SwiftService(
            BanqueEtatRepository banqueEtatRepository,
            BanqueRepository banqueRepository,
            SwiftRepository swiftRepository) {

        this.banqueEtatRepository = banqueEtatRepository;
        this.banqueRepository = banqueRepository;
        this.swiftRepository = swiftRepository;
    }

    public void deleteByDateseance(LocalDate dateseance) {
        swiftRepository.deleteByDateseance(dateseance);
    }

    public static String remplacerDerniereLettrePar_0(String input) {
        if (input == null || input.length() == 0) return "";
        return input.substring(0, input.length() - 4) + "TXRTS";
    }

    public static String supprimer4DernieresLettres(String input) {
        if (input == null || input.length() <= 4) return "";
        return input.substring(0, input.length() - 4);
    }

    // ================= GENERATION SWIFT =================
    public List<Swift> generateSwiftFromBanqueEtat(String seanceStr) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate seance = LocalDate.parse(seanceStr, formatter);

        log.info("Génération SWIFT pour séance {}", seance);

        // ── 1. Charger les BanqueEtat ──────────────────────────────────────────
        List<BanqueEtat> etats = banqueEtatRepository.findBySeance(seance);
        log.info("BanqueEtat trouvés pour {} : {}", seance, etats.size());

        if (etats.isEmpty()) {
            throw new IllegalStateException(
                    "Aucun état bancaire (BanqueEtat) trouvé pour la séance " + seance +
                            ". Veuillez d'abord lancer le traitement de la séance (bouton 'Lancer tout')."
            );
        }

        // ── 2. Charger la banque FGM (BVMT) et la banque centrale (BCT) ────────
        List<Banque> toutesLesBanques = banqueRepository.findAll();
        log.info("Banques en base : {}", toutesLesBanques.size());
        toutesLesBanques.forEach(b ->
                log.info("  Banque: code={} lCourBque={} bic={}", b.getcBque(), b.getlCourBque(), b.getBic()));

        Banque banqueFgm = toutesLesBanques.stream()
                .filter(b -> "BVMT".equalsIgnoreCase(b.getlCourBque()))
                .findFirst().orElse(null);

        Banque banquecentral = toutesLesBanques.stream()
                .filter(b -> "BCT".equalsIgnoreCase(b.getlCourBque()))
                .findFirst().orElse(null);

        if (banqueFgm == null) {
            throw new IllegalStateException(
                    "Banque FGM/BVMT introuvable en base (collection 'banque'). " +
                            "Vérifiez que la banque avec lCourBque='BVMT' existe."
            );
        }
        if (banquecentral == null) {
            throw new IllegalStateException(
                    "Banque Centrale (BCT) introuvable en base (collection 'banque'). " +
                            "Vérifiez que la banque avec lCourBque='BCT' existe."
            );
        }

        // ── 3. Calculer les soldes nets par BIC ────────────────────────────────
        Map<String, Integer> totalByBic = new HashMap<>();

        for (BanqueEtat be : etats) {
            List<Banque> matches = banqueRepository.findBycBque(be.getCode());
            if (matches.isEmpty()) {
                log.warn("Aucune banque trouvée pour code {} — BanqueEtat ignoré", be.getCode());
                continue;
            }
            Banque banque = matches.get(0);
            String bic = banque.getBic();
            if (bic == null || bic.isBlank()) {
                log.warn("BIC vide pour banque code {} — ignoré", be.getCode());
                continue;
            }
            totalByBic.merge(bic, be.getSolde(), Integer::sum);
        }

        if (totalByBic.isEmpty()) {
            throw new IllegalStateException(
                    "Aucune banque correspondante trouvée pour les codes dans BanqueEtat. " +
                            "Vérifiez la correspondance entre les codes BanqueEtat et la collection 'banque'."
            );
        }

        // ── 4. Générer les entrées Swift ──────────────────────────────────────
        Map<String, Boolean> alreadyAssigned = new HashMap<>();
        List<Swift> result = new ArrayList<>();
        int index = 0;

        for (BanqueEtat be : etats) {
            List<Banque> matches = banqueRepository.findBycBque(be.getCode());
            if (matches.isEmpty()) continue;
            Banque banque = matches.get(0);

            String bic = banque.getBic();
            if (bic == null || bic.isBlank()) continue;

            // Skip if seanceValeur is null
            if (be.getSeanceValeur() == null) {
                log.warn("seanceValeur null pour BanqueEtat code={} — ignoré", be.getCode());
                continue;
            }

            Swift s = new Swift();
            s.setDatecreation(LocalDateTime.now());
            s.setDatevaleur(be.getSeanceValeur());
            s.setDateseance(seance);
            s.setNbretat(etats.size());

            // Solde net : seulement sur la 1ère occurrence du BIC
            if (!alreadyAssigned.containsKey(bic)) {
                s.setSoldenette(totalByBic.getOrDefault(bic, 0));
                alreadyAssigned.put(bic, true);
            } else {
                s.setSoldenette(0);
            }

            s.setBICfgmbanque(banqueFgm.getBic());
            s.setBICfgmbankopt(banqueFgm.getBic());
            s.setBICbanquecentral(banquecentral.getBic());
            s.setNumcptbanquecentral(banquecentral.getNumCpt());

            s.setBICbanquecred(bic);
            s.setNumcptbanquecred(banque.getNumCpt());

            String datePart = be.getSeanceValeur()
                    .format(DateTimeFormatter.ofPattern("yyMMdd"));

            // Prtry defaults to 85 in the model, so use the default value directly
            String baseId  = "SE85" + datePart + String.format("%04d", 0) + "G";
            String instrId = "SE85" + datePart + String.format("%04d", index) + "G";

            s.setMsgId(baseId);
            s.setBizMsgIdr(baseId);
            s.setCdtId(baseId);
            s.setInstrId(instrId);

            s.setPacs("pacs.010.001.02");
            s.setBizSvc("RT");
            s.setBtchBookg(false);
            s.setPrty("0004");
            s.setTotal(1000000);
            s.setCcy("TND");
            s.setPrtry(85);
            s.setCtgyPurp("DDBVM");

            result.add(s);
            index++;
            log.info("  Swift #{} : BIC={} solde={}", index, bic, s.getSoldenette());
        }

        if (result.isEmpty()) {
            throw new IllegalStateException(
                    "Aucun message SWIFT généré. Vérifiez les codes banque et les données BanqueEtat."
            );
        }

        // Supprimer uniquement les Swift de cette séance, pas tous
        swiftRepository.deleteByDateseance(seance);
        List<Swift> saved = swiftRepository.saveAll(result);
        log.info("SWIFT générés et sauvegardés : {}", saved.size());
        return saved;
    }

    // ================= XML =================
    public byte[] generateXml(String seanceStr) {

        DateTimeFormatter inputFormatter   = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter creDtFormatter   = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'H:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);
        DateTimeFormatter creDtTmFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate seance = LocalDate.parse(seanceStr, inputFormatter);
        List<Swift> swifts = swiftRepository.findAll();

        if (swifts.isEmpty()) {
            throw new IllegalStateException("Aucun Swift trouvé — générez d'abord les messages SWIFT.");
        }

        StringBuilder xml = new StringBuilder();
        int ctrlSum = (int) swifts.stream()
                .mapToDouble(s -> Math.abs(s.getSoldenette()))
                .sum();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<DataPDU xmlns=\"urn:cma:stp:xsd:stp.1.0\">");
        xml.append("<Body>");

        Swift first = swifts.get(0);
        String fromBic  = first.getBICfgmbanque()    != null ? first.getBICfgmbanque()    : "";
        String toBic    = first.getBICbanquecentral() != null ? first.getBICbanquecentral() : "";
        String creAt    = first.getDatecreation()     != null
                ? first.getDatecreation().atZone(ZoneOffset.UTC).format(creDtFormatter)
                : LocalDateTime.now().atZone(ZoneOffset.UTC).format(creDtFormatter);

        xml.append("<AppHdr xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\">");
        xml.append("<Fr><FIId><FinInstnId><Othr><Id>").append(fromBic).append("</Id></Othr></FinInstnId></FIId></Fr>");
        xml.append("<To><FIId><FinInstnId><Othr><Id>").append(remplacerDerniereLettrePar_0(toBic)).append("</Id></Othr></FinInstnId></FIId></To>");
        xml.append("<BizMsgIdr>").append(first.getBizMsgIdr() != null ? first.getBizMsgIdr() : "").append("</BizMsgIdr>");
        xml.append("<MsgDefIdr>").append(first.getPacs()).append("</MsgDefIdr>");
        xml.append("<BizSvc>").append(first.getBizSvc()).append("</BizSvc>");
        xml.append("<CreDt>").append(creAt).append("</CreDt>");
        xml.append("<Prty>").append(first.getPrty()).append("</Prty>");
        xml.append("</AppHdr>");

        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.010.001.02\">");
        xml.append("<FIDrctDbt>");
        xml.append("<GrpHdr>");
        xml.append("<MsgId>").append(first.getMsgId() != null ? first.getMsgId() : "").append("</MsgId>");
        xml.append("<CreDtTm>").append(seance.atTime(23, 0, 0).format(creDtTmFormatter)).append("</CreDtTm>");
        xml.append("<NbOfTxs>").append(swifts.size()).append("</NbOfTxs>");
        xml.append("<CtrlSum>").append(ctrlSum).append("</CtrlSum>");
        xml.append("</GrpHdr>");

        for (Swift s : swifts) {
            String credBic     = s.getBICbanquecred()     != null ? s.getBICbanquecred()     : "";
            String centBic     = s.getBICbanquecentral()  != null ? s.getBICbanquecentral()  : "";
            String fgmBic      = s.getBICfgmbanque()      != null ? s.getBICfgmbanque()      : "";
            String centCpt     = s.getNumcptbanquecentral()!= null ? s.getNumcptbanquecentral(): "";
            String credCpt     = s.getNumcptbanquecred()  != null ? s.getNumcptbanquecred()  : "";
            String valDate     = s.getDatevaleur()        != null
                    ? s.getDatevaleur().format(dateOnlyFormatter) : "";

            xml.append("<CdtInstr>");
            xml.append("<CdtId>").append(s.getCdtId() != null ? s.getCdtId() : "").append("</CdtId>");
            xml.append("<BtchBookg>").append(s.isBtchBookg()).append("</BtchBookg>");
            xml.append("<TtlIntrBkSttlmAmt Ccy=\"TND\">").append(Math.abs(s.getSoldenette())).append("</TtlIntrBkSttlmAmt>");
            xml.append("<IntrBkSttlmDt>").append(valDate).append("</IntrBkSttlmDt>");
            xml.append("<InstgAgt><FinInstnId><ClrSysMmbId><MmbId>").append(supprimer4DernieresLettres(fgmBic)).append("</MmbId></ClrSysMmbId></FinInstnId></InstgAgt>");
            xml.append("<InstdAgt><FinInstnId><BICFI>").append(credBic).append("</BICFI></FinInstnId></InstdAgt>");
            xml.append("<Cdtr><FinInstnId><BICFI>").append(centBic).append("</BICFI></FinInstnId></Cdtr>");
            xml.append("<CdtrAcct><Id><Othr><Id>").append(centCpt).append("</Id></Othr></Id></CdtrAcct>");
            xml.append("<DrctDbtTxInf>");
            xml.append("<PmtId>");
            xml.append("<InstrId>").append(s.getInstrId() != null ? s.getInstrId() : "").append("</InstrId>");
            xml.append("<EndToEndId>").append(s.getCdtId() != null ? s.getCdtId() : "").append("</EndToEndId>");
            xml.append("<TxId>").append(s.getInstrId() != null ? s.getInstrId() : "").append("</TxId>");
            xml.append("</PmtId>");
            xml.append("<PmtTpInf>");
            xml.append("<LclInstrm><Prtry>").append(s.getPrtry()).append("</Prtry></LclInstrm>");
            xml.append("<CtgyPurp><Prtry>").append(s.getCtgyPurp()).append("</Prtry></CtgyPurp>");
            xml.append("</PmtTpInf>");
            xml.append("<IntrBkSttlmAmt Ccy=\"TND\">").append(Math.abs(s.getSoldenette())).append("</IntrBkSttlmAmt>");
            xml.append("<Dbtr><FinInstnId><BICFI>").append(credBic).append("</BICFI></FinInstnId></Dbtr>");
            xml.append("<DbtrAcct><Id><Othr><Id>").append(credCpt).append("</Id></Othr></Id></DbtrAcct>");
            xml.append("<InstrForDbtrAgt>INSTRUCTION</InstrForDbtrAgt>");
            xml.append("</DrctDbtTxInf>");
            xml.append("</CdtInstr>");
        }

        xml.append("</FIDrctDbt>");
        xml.append("</Document>");
        xml.append("</Body>");
        xml.append("</DataPDU>");

        return xml.toString().getBytes();
    }
}