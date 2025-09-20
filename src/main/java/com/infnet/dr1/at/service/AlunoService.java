package com.infnet.dr1.at.service;

import com.infnet.dr1.at.model.Aluno;
import com.infnet.dr1.at.model.Disciplina;
import com.infnet.dr1.at.model.Nota;
import com.infnet.dr1.at.model.Professor;
import com.infnet.dr1.at.repository.AlunoRepository;
import com.infnet.dr1.at.repository.DisciplinaRepository;
import com.infnet.dr1.at.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorRepository professorRepository;

    public AlunoService(AlunoRepository alunoRepository, DisciplinaRepository disciplinaRepository, ProfessorRepository professorRepository) {
        this.alunoRepository = alunoRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.professorRepository = professorRepository;
    }

    public List<Aluno> listarTodos() {
        return alunoRepository.findAll();
    }

    public Optional<Aluno> buscarPorId(String id) {
        return alunoRepository.findById(id);
    }

    public Aluno salvar(Aluno aluno) {
        return alunoRepository.save(aluno);
    }

    public void deletar(String id) {
        alunoRepository.deleteById(id);
    }

    public Optional<Aluno> alocarEmDisciplinasPorCodigo(String alunoId, List<String> codigos) {
        if (codigos == null || codigos.isEmpty()) {
            throw new IllegalArgumentException("É necessário informar pelo menos um código de disciplina.");
        }
        Optional<Aluno> optAluno = alunoRepository.findById(alunoId);
        if (optAluno.isEmpty()) return Optional.empty();
        Aluno aluno = optAluno.get();
        List<Disciplina> disciplinas = disciplinaRepository.findByCodigoIn(codigos);
        if (disciplinas.size() != new HashSet<>(codigos).size()) {
            throw new IllegalArgumentException("Uma ou mais disciplinas informadas não foram encontradas.");
        }
        Set<String> ids = disciplinas.stream().map(Disciplina::getId).collect(Collectors.toSet());
        aluno.setDisciplinaIds(ids);
        return Optional.of(alunoRepository.save(aluno));
    }

    public Optional<Aluno> atribuirNota(String professorUsername, String alunoId, String disciplinaId, Double nota) {
        if (nota == null || nota < 0 || nota > 10) {
            throw new IllegalArgumentException("Nota inválida. Informe um valor entre 0 e 10.");
        }
        Professor professor = professorRepository.findByUsername(professorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado."));

        Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada."));
        if (disciplina.getProfessorId() == null || !disciplina.getProfessorId().equals(professor.getId())) {
            throw new SecurityException("Você não está cadastrado como responsável por esta disciplina.");
        }

        Optional<Aluno> optAluno = alunoRepository.findById(alunoId);
        if (optAluno.isEmpty()) return Optional.empty();
        Aluno aluno = optAluno.get();
        if (aluno.getDisciplinaIds() == null || !aluno.getDisciplinaIds().contains(disciplinaId)) {
            throw new IllegalArgumentException("Aluno não está alocado nesta disciplina.");
        }

        if (aluno.getNotas() == null) {
            aluno.setNotas(new ArrayList<>());
        }
        aluno.getNotas().stream()
                .filter(n -> disciplinaId.equals(n.getDisciplinaId()))
                .findFirst()
                .ifPresentOrElse(
                        n -> n.setValor(nota),
                        () -> aluno.getNotas().add(new Nota(disciplinaId, nota))
                );
        return Optional.of(alunoRepository.save(aluno));
    }

    public List<Aluno> listarAprovados(String professorUsername, String disciplinaId) {
        Professor professor = professorRepository.findByUsername(professorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado."));
        Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada."));
        if (disciplina.getProfessorId() == null || !disciplina.getProfessorId().equals(professor.getId())) {
            throw new SecurityException("Você não está cadastrado como responsável por esta disciplina.");
        }
        List<Aluno> todos = alunoRepository.findAll();
        return todos.stream()
                .filter(a -> a.getDisciplinaIds() != null && a.getDisciplinaIds().contains(disciplinaId))
                .filter(a -> a.getNotas() != null)
                .filter(a -> a.getNotas().stream()
                        .filter(n -> disciplinaId.equals(n.getDisciplinaId()))
                        .anyMatch(n -> n.getValor() != null && n.getValor() >= 7.0))
                .collect(Collectors.toList());
    }

    public List<Aluno> listarReprovados(String professorUsername, String disciplinaId) {
        Professor professor = professorRepository.findByUsername(professorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado."));
        Disciplina disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada."));
        if (disciplina.getProfessorId() == null || !disciplina.getProfessorId().equals(professor.getId())) {
            throw new SecurityException("Você não está cadastrado como responsável por esta disciplina.");
        }
        List<Aluno> todos = alunoRepository.findAll();
        return todos.stream()
                .filter(a -> a.getDisciplinaIds() != null && a.getDisciplinaIds().contains(disciplinaId))
                .filter(a -> a.getNotas() != null)
                .filter(a -> a.getNotas().stream()
                        .filter(n -> disciplinaId.equals(n.getDisciplinaId()))
                        .anyMatch(n -> n.getValor() != null && n.getValor() < 7.0))
                .collect(Collectors.toList());
    }
}
