package com.fgm.gestion.controller;

import com.fgm.gestion.model.Tmm;
import com.fgm.gestion.repository.TmmRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tmm")
public class TmmController {

    private final TmmRepository repo;

    public TmmController(TmmRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Tmm>> all() {
        return ResponseEntity.ok(repo.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tmm> save(@RequestBody Tmm tmm) {
        return ResponseEntity.ok(repo.save(tmm));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
