package com.infnet.dr1.at.service;

import com.infnet.dr1.at.model.Aluno;
import com.infnet.dr1.at.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    public List<Aluno> listarTodos() {
        return alunoRepository.findAll();
    }

    public Optional<Aluno> buscarPorId(String id) {
        return alunoRepository.findById(id);
    }

    public Aluno salvar(Aluno aluno) {
        // Adicionar validações de negócio aqui (ex: CPF, e-mail já existem?)
        return alunoRepository.save(aluno);
    }

    public void deletar(String id) {
        alunoRepository.deleteById(id);
    }
}
