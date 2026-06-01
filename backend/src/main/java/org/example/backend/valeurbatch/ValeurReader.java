package com.fgm.gestion.valeurbatch;

import com.fgm.gestion.model.Valeur;
import com.fgm.gestion.service.BvmtFileParser;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@StepScope
public class ValeurReader implements ItemReader<Valeur> {

    private List<Valeur> data;
    private int index = 0;

    @Value("#{jobParameters['filePath']}")
    private String filePath;

    @Value("#{jobParameters['seanceCompact']}")
    private String seanceCompact;

    @Override
    public Valeur read() {
        if (data == null) {
            data = loadFile();
        }
        if (index < data.size()) {
            return data.get(index++);
        }
        return null;
    }

    private List<Valeur> loadFile() {
        try {
            Path path = Paths.get(filePath);
            LocalDate seanceFromJob = null;
            if (seanceCompact != null && !seanceCompact.isBlank()) {
                seanceFromJob = LocalDate.parse(seanceCompact, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            return BvmtFileParser.parseValeurs(path, seanceFromJob);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
