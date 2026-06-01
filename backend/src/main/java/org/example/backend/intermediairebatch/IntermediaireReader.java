package com.fgm.gestion.intermediairebatch;

import com.fgm.gestion.model.Intermediaire;
import com.fgm.gestion.service.BvmtFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@StepScope
public class IntermediaireReader implements ItemReader<Intermediaire> {

    private static final Logger log = LoggerFactory.getLogger(IntermediaireReader.class);

    private List<Intermediaire> data;
    private int index = 0;

    @Value("#{jobParameters['filePath']}")
    private String filePath;

    @Override
    public Intermediaire read() {
        if (data == null) {
            log.info("[INTERMEDIAIRE-READER] Chargement du fichier: {}", filePath);
            try {
                Path path = Paths.get(filePath);
                data = BvmtFileParser.parseIntermediaires(path);
                log.info("[INTERMEDIAIRE-READER] {} enregistrements lus (fixe ou CSV ;)", data.size());
            } catch (Exception e) {
                log.error("[INTERMEDIAIRE-READER] Erreur: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        if (index < data.size()) {
            return data.get(index++);
        }
        return null;
    }
}
