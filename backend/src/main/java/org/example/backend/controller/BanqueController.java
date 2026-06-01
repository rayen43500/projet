package com.fgm.gestion.controller;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.fgm.gestion.model.Banque;
import com.fgm.gestion.service.BanqueService;

@RestController
@RequestMapping("/api/banque")
public class BanqueController {

    private final BanqueService banqueService;

    public BanqueController(BanqueService banqueService) {
        this.banqueService = banqueService;
    }

    @PostMapping
    public Banque create(@RequestBody Banque banque) {
        return banqueService.save(banque);
    }

    @PostMapping("/bulk")
    public List<Banque> createAll(@RequestBody List<Banque> banques) {
        return banqueService.saveAll(banques);
    }

    @GetMapping
    public List<Banque> getAll() {
        return banqueService.getAll();
    }

    //  nouveau endpoint
    @GetMapping("/byCode")
    public List<Banque> getByCode(@RequestParam int code) {
        return banqueService.findBycBque(code);
    }

      // DELETE ALL
    @DeleteMapping
    public String deleteAll() {
        banqueService.deleteAll();
        return "Toutes les banque supprimées";
    }

    @PostMapping("/upload")
public String uploadFile(@RequestParam("file") MultipartFile file) {
    try {
        ObjectMapper mapper = new ObjectMapper();

        // fichier JSON contenant une liste de banques
        List<Banque> banques = Arrays.asList(
            mapper.readValue(file.getInputStream(), Banque[].class)
        );

        banqueService.saveAll(banques);

        return "Fichier JSON enregistré avec succès ";

    } catch (Exception e) {
        e.printStackTrace();
        return "Erreur lors de l'upload ";
    }
}

// AJOUT OU MODIFICATION
@PostMapping("/addOrUpdate")
public Banque addOrUpdate(@RequestBody Banque banque) {
    return banqueService.addOrUpdate(banque);
}

@DeleteMapping("/{code}")
public String delete(@PathVariable int code) {
    banqueService.deleteByCode(code);
    return "Banque supprimée";
}


}