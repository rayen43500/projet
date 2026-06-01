package com.fgm.gestion.repository;

import com.fgm.gestion.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findBySeance(String seance);

    Optional<Task> findBySeanceAndCode(String seance, String code);
}