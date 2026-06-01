package com.fgm.gestion.service;

import com.fgm.gestion.model.Task;
import com.fgm.gestion.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // GET tasks
    public List<Task> getTasksBySeance(String seance) {

        List<Task> tasks = taskRepository.findBySeance(seance);

        // auto init si vide
        if (tasks.isEmpty()) {
            tasks = List.of(
                    createTask(seance, "PREP", "preparer seance", "Urgent"),
                    createTask(seance, "SWIFT", "Génération SWIFT", "Urgent"),
                    createTask(seance, "ETAT", "Afficher les états", "Normal")
            );
            taskRepository.saveAll(tasks);
        }

        return tasks;
    }

    // UPDATE TASK (0 → 1)
    public Task updateTaskBySeanceAndCode(String seance, String code) {

        Task task = taskRepository.findBySeanceAndCode(seance, code)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(1);
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    // CREATE TASK
    public Task createTask(Task task) {
        task.setStatus(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    // helper
    private Task createTask(String seance, String code, String name, String priority) {
        Task t = new Task();
        t.setSeance(seance);
        t.setCode(code);
        t.setName(name);
        t.setPriority(priority);
        t.setStatus(0);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        return t;
    }
}