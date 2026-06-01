package com.fgm.gestion.controller;

import com.fgm.gestion.model.Parametrage;
import com.fgm.gestion.repository.ParametrageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parametrage")
public class ParametrageController {

    private final ParametrageRepository repo;

    public ParametrageController(ParametrageRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<Parametrage> get() {
        return repo.findAll().stream().findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Parametrage> update(@RequestBody Parametrage body) {
        Parametrage p = repo.findAll().stream().findFirst().orElse(new Parametrage());
        p.setSeuil_var_1(body.getSeuil_var_1());
        p.setSeuil_var_2(body.getSeuil_var_2());
        p.setSeuil_var_3(body.getSeuil_var_3());
        p.setSeuil_dep_pro(body.getSeuil_dep_pro());
        p.setDep_risq(body.getDep_risq());
        p.setMin_contr_init(body.getMin_contr_init());
        p.setDel_reg_liv(body.getDel_reg_liv());
        p.setDel_reg_DT(body.getDel_reg_DT());
        p.setDel_reg_DE(body.getDel_reg_DE());
        p.setTaux(body.getTaux());
        p.setBenefice(body.getBenefice());
        return ResponseEntity.ok(repo.save(p));
    }
}
