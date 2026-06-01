package com.fgm.gestion.controller;

import com.fgm.gestion.model.Task;
import com.fgm.gestion.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin("*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET tasks by seance
    @GetMapping
    public List<Task> getTasks(@RequestParam String seance) {
        return taskService.getTasksBySeance(seance);
    }

    // UPDATE task by seance + code
    @PutMapping("/update")
    public Task updateTask(
            @RequestParam String seance,
            @RequestParam String code
    ) {
        return taskService.updateTaskBySeanceAndCode(seance, code);
    }

    // CREATE task
    @PostMapping
    public Task create(@RequestBody Task task) {
        return taskService.createTask(task);
    }
}