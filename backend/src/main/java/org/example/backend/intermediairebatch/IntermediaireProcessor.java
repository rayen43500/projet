package com.fgm.gestion.intermediairebatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.fgm.gestion.model.Intermediaire;


@Component
public class IntermediaireProcessor implements ItemProcessor<Intermediaire, Intermediaire> {

    private static final Logger log = LoggerFactory.getLogger(IntermediaireProcessor.class);

    @Override
    public Intermediaire process(Intermediaire item) {
        log.debug("[INTERMEDIAIRE-PROCESSOR] Traitement codeIntermediaire={}, libelleCourt={}",
                item.getCodeIntermediaire(), item.getLibelleCourt());
        return item;
    }
}