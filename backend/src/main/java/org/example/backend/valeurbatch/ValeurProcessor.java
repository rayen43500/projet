package com.fgm.gestion.valeurbatch;

import com.fgm.gestion.model.Valeur;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ValeurProcessor implements ItemProcessor<Valeur, Valeur> {

    @Override
    public Valeur process(Valeur item) {
        return item;
    }
}