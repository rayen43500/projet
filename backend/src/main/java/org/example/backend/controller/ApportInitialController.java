package com.fgm.gestion.controller;

import com.fgm.gestion.model.ApportInitial;
import com.fgm.gestion.service.ApportInitialService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/apport-initial")
public class ApportInitialController {

    private final ApportInitialService service;

    public ApportInitialController(ApportInitialService service) {
        this.service = service;
    }

    @PostMapping
    public ApportInitial create(@RequestBody ApportInitial obj) {
        return service.save(obj);
    }

    @GetMapping
    public List<ApportInitial> getAll() {
        return service.getAll();
    }

    @GetMapping("/seance")
    public List<ApportInitial> getBySeance(@RequestParam String seance) {
        LocalDate date = LocalDate.parse(seance, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return service.getBySeance(date);
    }

    @DeleteMapping
    public void deleteAll() {
        service.deleteAll();
    }

    @DeleteMapping("/seance")
    public void deleteBySeance(@RequestParam String seance) {
        LocalDate date = LocalDate.parse(seance, DateTimeFormatter.ofPattern("yyyyMMdd"));
        service.deleteBySeance(date);
    }

    @PostMapping("/generate")
public String generate(
        @RequestParam String debut,
        @RequestParam String fin) {

    LocalDate d1 = LocalDate.parse(debut, DateTimeFormatter.ofPattern("yyyyMMdd"));
    LocalDate d2 = LocalDate.parse(fin, DateTimeFormatter.ofPattern("yyyyMMdd"));

    int count = service.generer(d1, d2);

    return "ApportInitial généré : " + count + " lignes";
}
}