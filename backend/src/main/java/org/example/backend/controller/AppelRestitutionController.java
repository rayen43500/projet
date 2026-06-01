package com.fgm.gestion.controller;

import com.fgm.gestion.model.AppelRestitutionParInter;
import com.fgm.gestion.service.AppelRestitutionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/appel-restitution")
public class AppelRestitutionController {

    private final AppelRestitutionService service;

    public AppelRestitutionController(AppelRestitutionService service) {
        this.service = service;
    }

    @PostMapping
    public AppelRestitutionParInter create(@RequestBody AppelRestitutionParInter obj) {
        return service.save(obj);
    }

    @GetMapping
    public List<AppelRestitutionParInter> getAll() {
        return service.getAll();
    }

    @GetMapping("/seance")
    public List<AppelRestitutionParInter> getBySeance(@RequestParam String date) {
        LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return service.getBySeance(d);
    }

    @DeleteMapping
    public void deleteAll() {
        service.deleteAll();
    }

    
    @PostMapping("/generate")
public String generate(@RequestParam String seance) {

    LocalDate date = LocalDate.parse(seance,
            DateTimeFormatter.ofPattern("yyyyMMdd"));

    service.genererAppelRestitution(date);

    return "Appel restitution généré";
}
}