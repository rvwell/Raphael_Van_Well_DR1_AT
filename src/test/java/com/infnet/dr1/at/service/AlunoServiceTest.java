package com.infnet.dr1.at.service;

import com.infnet.dr1.at.model.Aluno;
import com.infnet.dr1.at.model.Disciplina;
import com.infnet.dr1.at.model.Nota;
import com.infnet.dr1.at.model.Professor;
import com.infnet.dr1.at.repository.AlunoRepository;
import com.infnet.dr1.at.repository.DisciplinaRepository;
import com.infnet.dr1.at.repository.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlunoServiceTest {

    private AlunoRepository alunoRepository;
    private DisciplinaRepository disciplinaRepository;
    private ProfessorRepository professorRepository;
    private AlunoService service;

    @BeforeEach
    void setUp() {
        alunoRepository = Mockito.mock(AlunoRepository.class);
        disciplinaRepository = Mockito.mock(DisciplinaRepository.class);
        professorRepository = Mockito.mock(ProfessorRepository.class);
        service = new AlunoService(alunoRepository, disciplinaRepository);
    }

    @Test
    void listarTodos_buscaTodosNoRepositorio() {
        when(alunoRepository.findAll()).thenReturn(Collections.singletonList(new Aluno()));
        assertEquals(1, service.listarTodos().size());
        verify(alunoRepository).findAll();
    }

    @Test
    void buscarPorId_retornaDoRepositorio() {
        Aluno a = new Aluno(); a.setId("a1");
        when(alunoRepository.findById("a1")).thenReturn(Optional.of(a));
        Optional<Aluno> result = service.buscarPorId("a1");
        assertTrue(result.isPresent());
        assertEquals("a1", result.get().getId());
        verify(alunoRepository).findById("a1");
    }

    @Test
    void salvar_delegaParaRepositorio() {
        Aluno input = new Aluno(); input.setNome("Joao");
        Aluno saved = new Aluno(); saved.setId("1"); saved.setNome("Joao");
        when(alunoRepository.save(input)).thenReturn(saved);
        Aluno result = service.salvar(input);
        assertEquals(saved, result);
        verify(alunoRepository).save(input);
    }

    @Test
    void deletar_deletaPorId() {
        service.deletar("x");
        verify(alunoRepository).deleteById("x");
    }

    @Test
    @DisplayName("alocarEmDisciplinasPorCodigo - erro quando codigos nulos ou vazios")
    void alocarEmDisciplinas_codigosInvalidos() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> service.alocarEmDisciplinasPorCodigo("a1", null));
        assertTrue(ex1.getMessage().contains("pelo menos um código"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> service.alocarEmDisciplinasPorCodigo("a1", Collections.emptyList()));
        assertTrue(ex2.getMessage().contains("pelo menos um código"));
    }

    @Test
    @DisplayName("alocarEmDisciplinasPorCodigo - retorna empty quando aluno nao existe")
    void alocarEmDisciplinas_alunoInexistente() {
        when(alunoRepository.findById("a1")).thenReturn(Optional.empty());
        Optional<Aluno> result = service.alocarEmDisciplinasPorCodigo("a1", List.of("D1"));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("alocarEmDisciplinasPorCodigo - erro quando alguma disciplina nao encontrada")
    void alocarEmDisciplinas_disciplinaFaltando() {
        Aluno a = new Aluno(); a.setId("a1");
        when(alunoRepository.findById("a1")).thenReturn(Optional.of(a));
        when(disciplinaRepository.findByCodigoIn(List.of("D1","D2")))
                .thenReturn(List.of(new Disciplina("1","POO","D1"))); // apenas uma encontrada

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.alocarEmDisciplinasPorCodigo("a1", List.of("D1","D2")));
        assertTrue(ex.getMessage().contains("não foram encontradas"));
    }

    @Test
    @DisplayName("alocarEmDisciplinasPorCodigo - sucesso, define disciplinaIds e salva")
    void alocarEmDisciplinas_sucesso() {
        Aluno a = new Aluno(); a.setId("a1");
        when(alunoRepository.findById("a1")).thenReturn(Optional.of(a));
        Disciplina d1 = new Disciplina("1","POO","D1");
        Disciplina d2 = new Disciplina("2","BD","D2");
        when(disciplinaRepository.findByCodigoIn(List.of("D1","D2")))
                .thenReturn(List.of(d1, d2));
        when(alunoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Aluno> result = service.alocarEmDisciplinasPorCodigo("a1", List.of("D1","D2"));

        assertTrue(result.isPresent());
        assertNotNull(result.get().getDisciplinaIds());
        assertEquals(Set.of("1","2"), result.get().getDisciplinaIds());
        verify(alunoRepository).save(result.get());
    }

    @Test
    @DisplayName("atribuirNota - valida nota (nula/fora do intervalo)")
    void atribuirNota_notaInvalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.atribuirNota("prof","a1","d1", -1.0));
        assertTrue(ex.getMessage().contains("Nota inválida"));
    }

    @Test
    @DisplayName("atribuirNota - disciplina nao encontrada")
    void atribuirNota_professorNaoEncontrado() {
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.atribuirNota("prof","a1","d1", 7.0));
        assertTrue(ex.getMessage().contains("Disciplina não encontrada"));
    }

    @Test
    @DisplayName("atribuirNota - disciplina nao encontrada")
    void atribuirNota_disciplinaNaoEncontrada() {
        when(professorRepository.findByUsername("prof"))
                .thenReturn(Optional.of(new Professor("p1", "prof", "pass", Set.of("ROLE_PROFESSOR"))));
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.atribuirNota("prof","a1","d1", 7.0));
        assertTrue(ex.getMessage().contains("Disciplina não encontrada"));
    }

    @Test
    @DisplayName("atribuirNota - professor nao responsavel pela disciplina")
    void atribuirNota_professorNaoResponsavel() {
        when(professorRepository.findByUsername("prof"))
                .thenReturn(Optional.of(new Professor("p1", "prof", "pass", Set.of("ROLE_PROFESSOR"))));
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","POO","C1")));
        Aluno a = new Aluno(); a.setId("a1"); a.setDisciplinaIds(Set.of("outra"));
        when(alunoRepository.findById("a1")).thenReturn(Optional.of(a));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.atribuirNota("prof","a1","d1", 7.0));
        assertTrue(ex.getMessage().contains("não está alocado"));
    }

    @Test
    @DisplayName("atribuirNota - aluno inexistente retorna Optional.empty()")
    void atribuirNota_alunoNaoEncontrado() {
        when(professorRepository.findByUsername("prof"))
                .thenReturn(Optional.of(new Professor("p1", "prof", "pass", Set.of("ROLE_PROFESSOR"))));
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","POO","C1")));
        when(alunoRepository.findById("a1")).thenReturn(Optional.empty());
        Optional<Aluno> result = service.atribuirNota("prof","a1","d1", 7.0);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("atribuirNota - aluno nao alocado na disciplina gera erro")
    void atribuirNota_alunoNaoAlocado() {
        when(professorRepository.findByUsername("prof"))
                .thenReturn(Optional.of(new Professor("p1", "prof", "pass", Set.of("ROLE_PROFESSOR"))));
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","POO","C1")));
        Aluno a = new Aluno(); a.setId("a1"); a.setDisciplinaIds(Set.of("outra"));
        when(alunoRepository.findById("a1")).thenReturn(Optional.of(a));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.atribuirNota("prof","a1","d1", 7.0));
        assertTrue(ex.getMessage().contains("não está alocado"));
    }

    @Test
    @DisplayName("atribuirNota - cria nova nota quando nao existe e salva")
    void atribuirNota_criaNotaNova() {
        when(professorRepository.findByUsername("prof"))
                .thenReturn(Optional.of(new Professor("p1", "prof", "pass", Set.of("ROLE_PROFESSOR"))));
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","POO","C1")));
        Aluno a = new Aluno(); a.setId("a1"); a.setDisciplinaIds(Set.of("d1")); a.setNotas(new ArrayList<>());
        when(alunoRepository.findById("a1")).thenReturn(Optional.of(a));
        when(alunoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Aluno> result = service.atribuirNota("prof","a1","d1", 8.5);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getNotas().size());
        Nota n = result.get().getNotas().get(0);
        assertEquals("d1", n.getDisciplinaId());
        assertEquals(8.5, n.getValor());
        verify(alunoRepository).save(result.get());
    }

    @Test
    @DisplayName("atribuirNota - atualiza nota existente")
    void atribuirNota_atualizaNotaExistente() {
        when(professorRepository.findByUsername("prof"))
                .thenReturn(Optional.of(new Professor("p1", "prof", "pass", Set.of("ROLE_PROFESSOR"))));
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","POO","C1")));
        Nota nota = new Nota("d1", 5.0);
        Aluno a = new Aluno(); a.setId("a1"); a.setDisciplinaIds(Set.of("d1")); a.setNotas(new ArrayList<>(List.of(nota)));
        when(alunoRepository.findById("a1")).thenReturn(Optional.of(a));
        when(alunoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Aluno> result = service.atribuirNota("prof","a1","d1", 9.0);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getNotas().size());
        assertEquals(9.0, result.get().getNotas().get(0).getValor());
        verify(alunoRepository).save(result.get());
    }

    @Test
    @DisplayName("listarAprovados - valida professor, disciplina e filtra corretamente")
    void listarAprovados_filtra() {
        when(professorRepository.findByUsername("prof"))
                .thenReturn(Optional.of(new Professor("p1", "prof", "pass", Set.of("ROLE_PROFESSOR"))));
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","POO","C1")));

        Aluno a1 = new Aluno(); a1.setId("a1"); a1.setDisciplinaIds(Set.of("d1")); a1.setNotas(List.of(new Nota("d1", 7.0)));
        Aluno a2 = new Aluno(); a2.setId("a2"); a2.setDisciplinaIds(Set.of("d1")); a2.setNotas(List.of(new Nota("d1", 6.9)));
        Aluno a3 = new Aluno(); a3.setId("a3"); a3.setDisciplinaIds(Set.of("d1")); a3.setNotas(List.of(new Nota("d1", 9.0)));
        when(alunoRepository.findAll()).thenReturn(List.of(a1,a2,a3));

        List<Aluno> aprovados = service.listarAprovados("prof","d1");

        assertEquals(Set.of("a1","a3"), new HashSet<>(aprovados.stream().map(Aluno::getId).toList()));
    }

    @Test
    @DisplayName("listarReprovados - valida professor, disciplina e filtra corretamente")
    void listarReprovados_filtra() {
        when(professorRepository.findByUsername("prof"))
                .thenReturn(Optional.of(new Professor("p1", "prof", "pass", Set.of("ROLE_PROFESSOR"))));
        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","POO","C1")));

        Aluno a1 = new Aluno(); a1.setId("a1"); a1.setDisciplinaIds(Set.of("d1")); a1.setNotas(List.of(new Nota("d1", 7.0)));
        Aluno a2 = new Aluno(); a2.setId("a2"); a2.setDisciplinaIds(Set.of("d1")); a2.setNotas(List.of(new Nota("d1", 6.9)));
        Aluno a3 = new Aluno(); a3.setId("a3"); a3.setDisciplinaIds(Set.of("d1")); a3.setNotas(List.of(new Nota("d1", 9.0)));
        when(alunoRepository.findAll()).thenReturn(List.of(a1,a2,a3));

        List<Aluno> reprovados = service.listarReprovados("prof","d1");

        assertEquals(Set.of("a2"), new HashSet<>(reprovados.stream().map(Aluno::getId).toList()));
    }

    @Test
    @DisplayName("listarAprovados - erro quando professor nao encontrado ou nao responsavel")
    void listarAprovados_erros() {

        when(disciplinaRepository.findById("d1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.listarAprovados("prof","d1"));

        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","n","c")));
        assertDoesNotThrow(() -> service.listarAprovados("prof","d1"));
    }

    @Test
    @DisplayName("listarReprovados - erro quando professor nao encontrado ou nao responsavel")
    void listarReprovados_erros() {

        when(disciplinaRepository.findById("d1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.listarReprovados("prof","d1"));

        when(disciplinaRepository.findById("d1")).thenReturn(Optional.of(new Disciplina("d1","n","c")));
        assertDoesNotThrow(() -> service.listarReprovados("prof","d1"));
    }
}
