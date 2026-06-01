package com.fgm.gestion.controller;

import com.fgm.gestion.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * API REST alignée sur le client Angular FGM (séances ISO, positions agrégées, import session).
 */
@RestController
public class FgmApiController {

    private final SeanceService seanceService;
    private final FgmDashboardService dashboardService;
    private final FgmImportService importService;

    public FgmApiController(
            SeanceService seanceService,
            FgmDashboardService dashboardService,
            FgmImportService importService) {
        this.seanceService = seanceService;
        this.dashboardService = dashboardService;
        this.importService = importService;
    }

    @GetMapping("/api/seances")
    public ResponseEntity<?> listSeances() {
        try {
            return ResponseEntity.ok(dashboardService.listSeancesFrontend());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/api/seances/courante")
    public ResponseEntity<?> seanceCourante() {
        try {
            Map<String, Object> m = dashboardService.getSeanceCourante();
            if (m == null) {
                return ResponseEntity.ok(Collections.emptyMap());
            }
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/api/seances/{date}/stats")
    public ResponseEntity<?> seanceStats(@PathVariable String date) {
        try {
            return ResponseEntity.ok(dashboardService.getSeanceStats(date));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    /** Snapshot complet dashboard (positions, feuille marge, stats) — même format que POST /api/import/session */
    @GetMapping("/api/seances/{date}/dashboard")
    public ResponseEntity<?> dashboardSnapshot(@PathVariable String date) {
        try {
            var d = dashboardService.parseIso(date);
            Map<String, Object> snap = dashboardService.buildSessionImportSnapshot(d);
            snap.put("dateSeance", dashboardService.toIso(d));
            return ResponseEntity.ok(snap);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/api/seances/preparer")
    public ResponseEntity<?> preparer(@RequestBody Map<String, String> body) {
        try {
            String iso = body.get("dateSeance");
            if (iso == null || iso.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "dateSeance requis"));
            }
            var saved = seanceService.createSeanceFromIso(iso);
            return ResponseEntity.ok(Map.of(
                    "message", "Séance créée / préparée",
                    "seance", dashboardService.toIso(saved.getSeance())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/api/seances/{date}/cloturer")
    public ResponseEntity<?> cloturer(@PathVariable String date) {
        try {
            seanceService.cloturerFromIso(date);
            return ResponseEntity.ok(Map.of("message", "Séance clôturée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/api/seances/{date}/annuler")
    public ResponseEntity<?> annuler(@PathVariable String date, @RequestBody Map<String, String> body) {
        try {
            String motif = body != null ? body.getOrDefault("motif", "") : "";
            seanceService.annulerFromIso(date, motif);
            return ResponseEntity.ok(Map.of("message", "Séance annulée"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/api/seances/{date}/detecter-anomalies")
    public ResponseEntity<?> detecter(@PathVariable String date) {
        try {
            var d = dashboardService.parseIso(date);
            int n = dashboardService.countAnomalies(d);
            return ResponseEntity.ok(Map.of("nbAnomalies", n, "message", n == 0 ? "Aucune anomalie" : n + " position(s) à risque"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/api/positions")
    public ResponseEntity<?> positions(@RequestParam String dateSeance) {
        try {
            return ResponseEntity.ok(dashboardService.positionsForDateIso(dateSeance));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/api/intermediaires/me/positions")
    public ResponseEntity<?> myPositions(
            @RequestParam String date,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String code = extractInterCode(jwt);
            if (code == null) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Jeton sans code intermédiaire"));
            }
            return ResponseEntity.ok(dashboardService.positionsForIntermediaire(date, code));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/api/intermediaires")
    public ResponseEntity<?> intermediaires(@RequestParam(required = false) String dateSeance) {
        try {
            String iso = dateSeance;
            if (iso == null || iso.isBlank()) {
                Map<String, Object> cur = dashboardService.getSeanceCourante();
                if (cur == null) {
                    return ResponseEntity.ok(Collections.emptyList());
                }
                iso = (String) cur.get("dateSeance");
            }
            return ResponseEntity.ok(dashboardService.listIntermediairesForSeanceIso(iso));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/api/intermediaires/{code}")
    public ResponseEntity<?> intermediaireByCode(
            @PathVariable int code,
            @RequestParam(required = false) String dateSeance) {
        try {
            String iso = dateSeance;
            if (iso == null || iso.isBlank()) {
                Map<String, Object> cur = dashboardService.getSeanceCourante();
                iso = cur != null ? (String) cur.get("dateSeance") : null;
            }
            if (iso == null) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Aucune séance"));
            }
            return dashboardService.listIntermediairesForSeanceIso(iso).stream()
                    .filter(m -> code == ((Number) m.get("codeIntermediaire")).intValue())
                    .findFirst()
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ── Detect date from uploaded transactions file ──────────────────────────
    @PostMapping("/api/import/detect-date")
    public ResponseEntity<?> detectDateFromFile(
            @RequestParam("transactionsFile") MultipartFile transactionsFile,
            @RequestParam(value = "valeursFile", required = false) MultipartFile valeursFile) {
        try {
            if (transactionsFile == null || transactionsFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier manquant"));
            }
            String detectedDate = (valeursFile != null && !valeursFile.isEmpty())
                    ? importService.detectSeanceDate(transactionsFile, valeursFile)
                    : importService.detectDateFromTransactionsFile(transactionsFile);
            return ResponseEntity.ok(Map.of("dateSeance", detectedDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/api/import/session")
    public ResponseEntity<?> importSession(
            @RequestParam("transactionsFile") MultipartFile transactionsFile,
            @RequestParam("intermediairesFile") MultipartFile intermediairesFile,
            @RequestParam("valeursFile") MultipartFile valeursFile,
            @RequestParam(value = "dateSeance", required = false) String dateSeanceIso) {
        try {
            if (transactionsFile == null || transactionsFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier transactions manquant ou vide"));
            }
            if (intermediairesFile == null || intermediairesFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier intermédiaires manquant ou vide"));
            }
            if (valeursFile == null || valeursFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier valeurs manquant ou vide"));
            }
            // Auto-detect date from file if not provided or use file date always
            String fileDate = importService.detectSeanceDate(transactionsFile, valeursFile);
            // File date takes priority over manually prepared seance date
            String effectiveDate = (fileDate != null && !fileDate.isBlank()) ? fileDate : dateSeanceIso;
            if (effectiveDate == null || effectiveDate.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Impossible de détecter la date depuis le fichier"));
            }
            // Auto-create/update seance for the detected date if needed
            importService.ensureSeanceExists(effectiveDate);
            Map<String, Object> res = importService.importSession(
                    transactionsFile, intermediairesFile, valeursFile, effectiveDate);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            String m = e.getMessage();
            return ResponseEntity.badRequest().body(Map.of("erreur", m != null && !m.isBlank() ? m : "Erreur import"));
        }
    }

    @PostMapping("/api/import/{endpoint}")
    public ResponseEntity<?> importLegacy(
            @PathVariable String endpoint,
            @RequestParam("file") MultipartFile file,
            @RequestParam("dateSeance") String dateSeanceIso) {
        return ResponseEntity.badRequest().body(Map.of(
                "erreur", "Import séparé non branché sur ce backend — utilisez POST /api/import/session avec les 3 fichiers."
        ));
    }

    private static String extractInterCode(Jwt jwt) {
        if (jwt == null) return null;
        Object c = jwt.getClaim("intermediaire_code");
        if (c != null) return String.valueOf(c);
        c = jwt.getClaim("code_intermediaire");
        if (c != null) return String.valueOf(c);
        return null;
    }
}