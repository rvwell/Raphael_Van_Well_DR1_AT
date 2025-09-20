package com.infnet.dr1.at.repository;

import com.infnet.dr1.at.model.Disciplina;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DisciplinaRepository extends MongoRepository<Disciplina, String> {
    Optional<Disciplina> findByCodigo(String codigo);
    List<Disciplina> findByCodigoIn(List<String> codigos);
}
