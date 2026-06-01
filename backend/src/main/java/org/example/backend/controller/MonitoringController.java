package com.fgm.gestion.controller;

import com.fgm.gestion.model.Monitoring;
import com.fgm.gestion.service.MonitoringService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    //  Generer monitoring pour une date
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam String date) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate d = LocalDate.parse(date, formatter);

            List<Monitoring> result = monitoringService.generateMonitoring(d);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  Get all
    @GetMapping("/all")
    public List<Monitoring> getAll() {
        return monitoringService.getAll();
    }

     @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelSeance(@RequestParam String seance) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate date = LocalDate.parse(seance, formatter);

             monitoringService.deleteByDate(date);

            return ResponseEntity.ok(Map.of(
                    "message", "monitoring   supprimées pour la séance " + seance
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}