package com.fgm.gestion.service;

import com.fgm.gestion.model.Parametrage;
import com.fgm.gestion.repository.ParametrageRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ParametrageService {

    private final ParametrageRepository repository;

    public ParametrageService(ParametrageRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> getAllParametres() {
       
        Optional<Parametrage> opt = repository.findAll().stream().findFirst();
        Parametrage p = opt.orElseGet(Parametrage::new); 

        Map<String, Object> params = new HashMap<>();
        params.put("seuil_var_1", p.getSeuil_var_1());
        params.put("seuil_var_2", p.getSeuil_var_2());
        params.put("seuil_var_3", p.getSeuil_var_3());
        params.put("seuil_dep_pro", p.getSeuil_dep_pro());
        params.put("dep_risq", p.getDep_risq());
        params.put("min_contr_init", p.getMin_contr_init());
        params.put("del_reg_liv", p.getDel_reg_liv());
        params.put("del_reg_DT", p.getDel_reg_DT());
        params.put("del_reg_DE", p.getDel_reg_DE());
        params.put("taux", p.getTaux());
params.put("benefice", p.getBenefice());

        return params;
    }

    public void update(double seuil_var_2, double dep_risq, int seuil_var_1, int del_reg_DE,
                   int seuil_var_3, int del_reg_DT, int del_reg_liv, int min_contr_init,
                   int seuil_dep_pro,
                   double taux, double benefice) {

        if (seuil_var_2 < 0 || dep_risq < 0 || seuil_var_1 < 0 || del_reg_DE < 0 ||
            seuil_var_3 < 0 || del_reg_DT < 0 || del_reg_liv < 0 || min_contr_init < 0 || seuil_dep_pro < 0
            || taux < 0 || benefice < 0 ) {
            throw new IllegalArgumentException("Les valeurs ne peuvent pas etre negatives");
        }

     
        Parametrage p = repository.findAll().stream().findFirst().orElse(new Parametrage());

        p.setSeuil_var_1(seuil_var_1);
        p.setSeuil_var_2(seuil_var_2);
        p.setSeuil_var_3(seuil_var_3);
        p.setSeuil_dep_pro(seuil_dep_pro);
        p.setDep_risq(dep_risq);
        p.setMin_contr_init(min_contr_init);
        p.setDel_reg_liv(del_reg_liv);
        p.setDel_reg_DT(del_reg_DT);
        p.setDel_reg_DE(del_reg_DE);
        p.setTaux(taux);
        p.setBenefice(benefice);
       

        repository.save(p); 
    }
}