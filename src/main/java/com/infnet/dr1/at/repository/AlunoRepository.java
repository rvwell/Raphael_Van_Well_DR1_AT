package com.infnet.dr1.at.repository;

import com.infnet.dr1.at.model.Aluno;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlunoRepository extends MongoRepository<Aluno, String> {
}
