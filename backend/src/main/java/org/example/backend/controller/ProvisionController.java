package com.fgm.gestion.controller;

import com.fgm.gestion.model.Provision;
import com.fgm.gestion.repository.ProvisionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/provisions")
public class ProvisionController {

    private final ProvisionRepository repo;

    public ProvisionController(ProvisionRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Provision>> all(@RequestParam(required = false) String date) {
        if (date != null && !date.isBlank()) {
            try {
                LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return ResponseEntity.ok(repo.findByDateCalcul(d));
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(repo.findAll());
    }
}
