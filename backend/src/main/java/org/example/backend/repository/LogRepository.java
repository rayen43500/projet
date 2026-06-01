
package com.fgm.gestion.repository;

import java.util.*;
import java.time.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.fgm.gestion.model.*;

public interface LogRepository extends MongoRepository<Log, String> {
}