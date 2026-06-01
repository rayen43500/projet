package com.fgm.gestion.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FgmImportService {

    private static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO     = DateTimeFormatter.ISO_LOCAL_DATE;

    private final GlobalBatchOrchestrationService orchestrationService;
    private final FgmDashboardService             dashboardService;
    private final SeanceService                   seanceService;
    private final BvmtImportValidator             importValidator;

    public FgmImportService(
            GlobalBatchOrchestrationService orchestrationService,
            FgmDashboardService dashboardService,
            SeanceService seanceService,
            BvmtImportValidator importValidator) {
        this.orchestrationService = orchestrationService;
        this.dashboardService     = dashboardService;
        this.seanceService        = seanceService;
        this.importValidator      = importValidator;
    }

    public String detectDateFromTransactionsFile(MultipartFile file) throws Exception {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
        String compact = BvmtFileParser.detectDateFromTransactions(lines);
        if (compact != null) {
            return LocalDate.parse(compact, COMPACT).format(ISO);
        }
        throw new RuntimeException(
                "Impossible de détecter la date dans le fichier transactions. " +
                        "Formats acceptés : clearing (02 COB) ou texte fixe BVMT.");
    }

    /** Date de séance : priorité au fichier valeurs (26/02/26), sinon transactions */
    public String detectSeanceDate(MultipartFile transactionsFile, MultipartFile valeursFile) throws Exception {
        String compact = BvmtFileParser.detectSeanceCompact(transactionsFile, valeursFile);
        if (compact == null) {
            throw new RuntimeException("Impossible de détecter la date de séance (valeurs ou transactions)");
        }
        return LocalDate.parse(compact, COMPACT).format(ISO);
    }

    public void ensureSeanceExists(String isoDate) {
        seanceService.ensureSeanceExists(isoDate);
    }

    public Map<String, Object> importSession(
            MultipartFile transactionsFile,
            MultipartFile intermediairesFile,
            MultipartFile valeursFile,
            String dateSeanceIso) throws Exception {

        BvmtImportValidator.ValidationResult validation = importValidator.validate(
                transactionsFile, intermediairesFile, valeursFile, dateSeanceIso);

        if (!validation.valid()) {
            String msg = String.join("; ", validation.errors());
            throw new RuntimeException("Validation échouée : " + msg);
        }

        LocalDate d       = dashboardService.parseIso(dateSeanceIso);
        String    compact = dashboardService.toCompact(d);

        orchestrationService.runAll(transactionsFile, intermediairesFile, valeursFile, compact);

        if (!validation.warnings().isEmpty()) {
            seanceService.findOneBySeance(d).ifPresent(s -> {
                s.setAnomalies(new ArrayList<>(validation.warnings()));
                seanceService.saveSeance(s);
            });
        }

        Map<String, Object> snapshot = dashboardService.buildSessionImportSnapshot(d);
        snapshot.put("dateSeance", dateSeanceIso);
        if (!validation.warnings().isEmpty()) {
            snapshot.put("avertissements", validation.warnings());
        }
        return snapshot;
    }

    private static String safe(String s, int start, int end) {
        if (s.length() < end) return "";
        return s.substring(start, end).trim();
    }
}
