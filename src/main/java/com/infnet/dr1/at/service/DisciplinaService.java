package com.infnet.dr1.at.service;

import com.infnet.dr1.at.model.Disciplina;
import com.infnet.dr1.at.repository.DisciplinaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;

    public DisciplinaService(DisciplinaRepository disciplinaRepository) {
        this.disciplinaRepository = disciplinaRepository;
    }

    public List<Disciplina> listarTodas() {
        return disciplinaRepository.findAll();
    }

    public Optional<Disciplina> buscarPorId(String id) {
        return disciplinaRepository.findById(id);
    }

    public Optional<Disciplina> buscarPorCodigo(String codigo) {
        return disciplinaRepository.findByCodigo(codigo);
    }

    public Disciplina salvar(Disciplina disciplina) {
        return disciplinaRepository.save(disciplina);
    }

    public void deletar(String id) {
        disciplinaRepository.deleteById(id);
    }
}
