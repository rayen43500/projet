package com.fgm.gestion.controller;

import org.springframework.http.ResponseEntity;
import com.fgm.gestion.model.Swift;
import com.fgm.gestion.repository.SwiftRepository;
import com.fgm.gestion.service.SwiftService;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;
import java.time.format.*;

@RestController
@RequestMapping("/api/swift")
@CrossOrigin("*")
public class SwiftController {

    private final SwiftRepository swiftRepository;
    private final SwiftService swiftService;

    public SwiftController(SwiftRepository swiftRepository, SwiftService swiftService) {
        this.swiftRepository = swiftRepository;
        this.swiftService = swiftService;
    }

    // ===== CREATE =====
    @PostMapping
    public Swift create(@RequestBody Swift swift) {
        return swiftRepository.save(swift);
    }

    // ===== GET ALL =====
    @GetMapping
    public List<Swift> getAll() {
        return swiftRepository.findAll();
    }

    // ===== GET BY ID =====
    @GetMapping("/{id}")
    public Swift getById(@PathVariable String id) {
        return swiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Swift not found"));
    }

    // ===== DELETE =====
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        swiftRepository.deleteById(id);
    }

    // ===== GENERATE — wrapped with proper error handling so exceptions return 400 not 500 =====
    @PostMapping("/generate/{seance}")
    public ResponseEntity<?> generate(@PathVariable String seance) {
        try {
            List<Swift> result = swiftService.generateSwiftFromBanqueEtat(seance);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            // Business rule violations (missing data, missing banks, etc.)
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur lors de la génération SWIFT",
                    "details", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/download/{seance}")
    public ResponseEntity<?> download(@PathVariable String seance) {
        try {
            byte[] xml = swiftService.generateXml(seance);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=swift.xml")
                    .header("Content-Type", "application/xml")
                    .body(xml);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Erreur génération XML"
            ));
        }
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelSeance(@RequestParam String seance) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate dateseance = LocalDate.parse(seance, formatter);
            swiftService.deleteByDateseance(dateseance);
            return ResponseEntity.ok(Map.of(
                    "message", "swift supprimées pour la séance " + seance
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}