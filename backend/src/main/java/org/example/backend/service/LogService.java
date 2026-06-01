package com.fgm.gestion.service;
import java.time.*;
import org.springframework.stereotype.Service;
import java.util.List;
import com.fgm.gestion.model.*;
import com.fgm.gestion.repository.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public Log save(Log log) {
        log.setDate(LocalDate.now());
        return logRepository.save(log);
    }
}