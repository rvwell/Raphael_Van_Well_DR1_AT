package com.infnet.dr1.at.repository;

import com.infnet.dr1.at.model.Professor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProfessorRepository extends MongoRepository<Professor, String> {

    Optional<Professor> findByUsername(String username);

}
