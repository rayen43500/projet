package com.fgm.gestion.transactionbatch;

import com.fgm.gestion.service.BvmtFileParser;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
public class TransactionReader implements ItemReader<DataLine> {

    private List<DataLine> data;
    private int index = 0;

    @Value("#{jobParameters['filePath']}")
    private String filePath;

    @Override
    public DataLine read() {
        if (data == null) {
            data = loadFile();
        }
        if (index < data.size()) {
            return data.get(index++);
        }
        return null;
    }

    private List<DataLine> loadFile() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
            List<BvmtFileParser.TxRow> rows = BvmtFileParser.parseAllTransactions(lines);
            List<DataLine> list = new ArrayList<>();
            for (BvmtFileParser.TxRow r : rows) {
                list.add(new DataLine(
                        r.seanceCompact(),
                        r.codeValeur(),
                        r.libelleVendeur(),
                        r.libelleAcheteur(),
                        r.libelleValeur(),
                        r.quantite(),
                        r.prixTotal(),
                        r.codeVendeur(),
                        r.codeAcheteur(),
                        r.cours()));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
