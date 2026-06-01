package com.fgm.gestion.controller;

import com.fgm.gestion.model.*;
import com.fgm.gestion.service.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public Log create(@RequestBody Log log) {
        return logService.save(log);
    }
}