package com.fgm.gestion.controller;

import com.fgm.gestion.model.AppelRestitutionSem;
import com.fgm.gestion.service.AppelRestitutionSemService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/appel-restitution-sem")
public class AppelRestitutionSemController {

    private final AppelRestitutionSemService service;

    public AppelRestitutionSemController(AppelRestitutionSemService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public String generer(@RequestParam String seance) {

        LocalDate date = LocalDate.parse(seance, DateTimeFormatter.ofPattern("yyyyMMdd"));

        int count = service.generer(date);

        return "Généré : " + count;
    }

    @GetMapping
    public List<AppelRestitutionSem> getAll() {
        return service.getAll();
    }
}