package com.fgm.gestion.controller;

import com.fgm.gestion.model.Placement;
import com.fgm.gestion.service.PlacementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/placements")
public class PlacementController {

    private final PlacementService service;

    public PlacementController(PlacementService service) {
        this.service = service;
    }

    // GET ALL
    @GetMapping
    public List<Placement> getAll() {
        return service.getAll();
    }

    // GET BY DATE (dd/MM/yyyy)
    @GetMapping("/date/{date}")
    public List<Placement> getByDate(@PathVariable String date) {
        return service.getByDate(date);
    }

    // GET BY INTERMEDIAIRE
    @GetMapping("/inter/{name}")
    public List<Placement> getByInter(@PathVariable String name) {
        return service.getByIntermediaire(name);
    }

    // CREATE
    @PostMapping
    public Placement save(@RequestBody Placement p) {
        return service.save(p);
    }

    @PutMapping("/{id}")
public Placement update(@PathVariable String id, @RequestBody Placement p) {
    return service.update(id, p);
}

@PutMapping("/bulk")
public List<Placement> updateBulk(@RequestBody List<Placement> placements) {
    return service.updateBulk(placements);
}
}