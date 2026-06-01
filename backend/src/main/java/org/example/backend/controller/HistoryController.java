package com.fgm.gestion.controller;

import com.fgm.gestion.model.*;
import com.fgm.gestion.service.HistoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    //  Generer History pour une date
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam String date) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate d = LocalDate.parse(date, formatter);

            List<History> result = historyService.generateHistory(d);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  Get all
    @GetMapping("/all")
    public List<History> getAll() {
        return historyService.getAll();
    }

     @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelSeance(@RequestParam String seance) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(seance, formatter);

             historyService.deleteByDate(date);

            return ResponseEntity.ok(Map.of(
                    "message", "history  supprimées pour la séance " + seance
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}